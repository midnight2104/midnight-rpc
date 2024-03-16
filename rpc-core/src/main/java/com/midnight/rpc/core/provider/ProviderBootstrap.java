package com.midnight.rpc.core.provider;

import com.midnight.rpc.core.annotation.RpcProvider;
import com.midnight.rpc.core.api.RpcRequest;
import com.midnight.rpc.core.api.RpcResponse;
import com.midnight.rpc.core.meta.ProviderMeta;
import com.midnight.rpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
public class ProviderBootstrap implements ApplicationContextAware {
    // 变量名称保持一致就可以不用写抽象方法。
    private ApplicationContext applicationContext;
    private MultiValueMap<String, ProviderMeta> skeletons = new LinkedMultiValueMap<>();

    // 在bean的初始化过程中，保存起来
    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(RpcProvider.class);
        providers.forEach((x, y) -> System.out.println(x));

        providers.values().forEach(x -> genInterface(x));
    }

    private void genInterface(Object x) {
        // 实现多个接口
        Arrays.stream(x.getClass().getInterfaces()).forEach(
                inter -> {
                    Method[] methods = inter.getMethods();
                    for (Method method : methods) {
                        if (MethodUtils.checkLocalMethod(method)) {
                            continue;
                        }
                        createProvider(inter, x, method);

                    }
                }
        );

    }

    private void createProvider(Class<?> inter, Object x, Method method) {
        ProviderMeta meta = new ProviderMeta();
        meta.setMethod(method);
        meta.setMethodSign(MethodUtils.methodSign(method));
        meta.setServiceImpl(x);

        skeletons.add(inter.getCanonicalName(), meta);
    }

    public RpcResponse invoke(RpcRequest request) {
        RpcResponse rpcResponse = new RpcResponse();

        List<ProviderMeta> metas = skeletons.get(request.getService());
        // 使用方法签名查询提供者元信息
        ProviderMeta meta = findProviderMeta(metas, request.getMethodSign());
        try {
            Method method = meta.getMethod();
            Object res = method.invoke(meta.getServiceImpl(), request.getArgs());
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

    private ProviderMeta findProviderMeta(List<ProviderMeta> metas, String methodSign) {
        return metas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign))
                .findFirst()
                .orElse(null);
    }

}
