package com.midnight.rpc.core.consumer;

import com.midnight.rpc.core.api.RegistryCenter;
import com.midnight.rpc.core.api.RpcContext;
import com.midnight.rpc.core.meta.InstanceMeta;
import com.midnight.rpc.core.meta.ServiceMeta;
import com.midnight.rpc.core.util.MethodUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    private ApplicationContext applicationContext;
    private Environment environment;

    private Map<String, Object> stubs = new HashMap<>();

    public void start() {
        RpcContext rpcContext = applicationContext.getBean(RpcContext.class);
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);

        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);

            List<Field> fields = MethodUtils.findConsumerField(bean.getClass());
            fields.forEach(f -> {

                try {
                    Class<?> service = f.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = stubs.get(serviceName);
                    if (consumer == null) {
                        consumer = createFromRegistry(service, rpcContext, rc);
                    }
                    f.setAccessible(true);

                    // 给空字段赋值代理对象
                    f.set(bean, consumer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    private Object createFromRegistry(Class<?> service, RpcContext context, RegistryCenter rc) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(context.param("app.id"))
                .name(service.getCanonicalName())
                .namespace(context.param("app.namespace"))
                .env(context.param("app.env"))
                .build();
        List<InstanceMeta> providers = rc.fetchAll(serviceMeta);

        rc.subscribe(serviceMeta, event -> {
            providers.clear();
            providers.addAll(event.getData());
        });

        return createConsumer(service, context, providers);
    }

    private Object createConsumer(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                new RpcInvocationHandler(service, context, providers));
    }


}
