package com.midnight.rpc.core.provider;

import com.midnight.rpc.core.annotation.RpcProvider;
import com.midnight.rpc.core.api.RpcRequest;
import com.midnight.rpc.core.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
public class ProviderBootstrap implements ApplicationContextAware {
    // 变量名称保持一致就可以不用写抽象方法。
    private ApplicationContext applicationContext;
    private Map<String, Object> skeletons = new HashMap<>();

    // 在bean的初始化过程中，保存起来
    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(RpcProvider.class);
        providers.forEach((x, y) -> System.out.println(x));

        providers.values().forEach(x -> genInterface(x));
    }

    private void genInterface(Object x) {
        //  假设只有一个接口
        Class<?> inter = x.getClass().getInterfaces()[0];

        skeletons.put(inter.getCanonicalName(), x);
    }

    public RpcResponse invoke(RpcRequest request) {
        RpcResponse rpcResponse = new RpcResponse();

        Object bean = skeletons.get(request.getService());
        Method method = findMethod(bean.getClass(), request.getMethod());
        try {
            Object res = method.invoke(bean, request.getArgs());
            rpcResponse.setStatus(true);
            rpcResponse.setData(res);
            return rpcResponse;
        } catch (InvocationTargetException e) {
            // 处理异常
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }
        return rpcResponse;
    }

    private Method findMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            // 重载方法，这就会有问题(方法名称可以带上方法签名name@Long@Integer)
            if (method.getName().equals(methodName))
                return method;
        }
        return null;
    }
}
