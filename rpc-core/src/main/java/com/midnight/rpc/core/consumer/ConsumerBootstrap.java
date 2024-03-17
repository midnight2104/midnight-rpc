package com.midnight.rpc.core.consumer;

import com.midnight.rpc.core.annotation.RpcConsumer;
import com.midnight.rpc.core.api.LoadBalancer;
import com.midnight.rpc.core.api.RegistryCenter;
import com.midnight.rpc.core.api.Router;
import com.midnight.rpc.core.api.RpcContext;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ConsumerBootstrap implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Map<String, Object> stubs = new HashMap<>();

    public void start() {
        Router router = applicationContext.getBean(Router.class);
        LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);

        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);


        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);

            List<Field> fields = findConsumerField(bean.getClass());
            fields.forEach(f -> {

                try {
                    Class<?> service = f.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = stubs.get(serviceName);
                    if (consumer == null) {
                        consumer = createFromRegistry(service, context, rc);
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
        String serviceName = service.getCanonicalName();
        List<String> providers = rc.fetchAll(serviceName);
        return createConsumer(service, context, providers);
    }

    private Object createConsumer(Class<?> service, RpcContext context, List<String> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                new RpcInvocationHandler(service, context, providers));
    }

    private List<Field> findConsumerField(Class<?> aClass) {
        List<Field> res = new ArrayList<>();

        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(RpcConsumer.class)) {
                    res.add(f);
                }
            }
            // bean有可能被CGLIB增强了
            aClass = aClass.getSuperclass();
        }

        return res;
    }
}
