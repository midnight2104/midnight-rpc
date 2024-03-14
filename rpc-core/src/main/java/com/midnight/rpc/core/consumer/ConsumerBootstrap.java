package com.midnight.rpc.core.consumer;

import com.midnight.rpc.core.annotation.RpcConsumer;
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
                        // 应该放到stubs，避免重复生成代理对象
                        consumer = createConsumer(service);
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

    private Object createConsumer(Class<?> service) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                new RpcInvocationHandler(service));
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
