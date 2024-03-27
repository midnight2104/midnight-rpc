package com.midnight.rpc.core.provider;

import com.midnight.rpc.core.api.RpcException;
import com.midnight.rpc.core.api.RpcRequest;
import com.midnight.rpc.core.api.RpcResponse;
import com.midnight.rpc.core.meta.ProviderMeta;
import com.midnight.rpc.core.util.TypeUtils;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeletons;

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeletons = providerBootstrap.getSkeletons();
    }

    public RpcResponse<Object> invoke(RpcRequest request) {
        RpcResponse<Object> rpcResponse = new RpcResponse<>();

        List<ProviderMeta> metas = skeletons.get(request.getService());
        // 使用方法签名查询提供者元信息
        ProviderMeta meta = findProviderMeta(metas, request.getMethodSign());
        try {
            Method method = meta.getMethod();

            // 参数类型转换
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes());

            // 反射
            Object res = method.invoke(meta.getServiceImpl(), args);
            rpcResponse.setStatus(true);
            rpcResponse.setData(res);
            return rpcResponse;
        } catch (InvocationTargetException e) {
            // 处理异常
            rpcResponse.setEx(new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            rpcResponse.setEx(new RpcException(e.getMessage()));
        }
        return rpcResponse;
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
        if (args == null || args.length == 0) return args;
        Object[] actuals = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actuals[i] = TypeUtils.cast(args[i], parameterTypes[i]);
        }
        return actuals;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> metas, String methodSign) {
        return metas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign))
                .findFirst()
                .orElse(null);
    }

}
