package com.midnight.rpc.core.provider;

import com.midnight.rpc.core.api.RpcContext;
import com.midnight.rpc.core.api.RpcException;
import com.midnight.rpc.core.api.RpcRequest;
import com.midnight.rpc.core.api.RpcResponse;
import com.midnight.rpc.core.config.ProviderConfigProperties;
import com.midnight.rpc.core.governance.SlidingTimeWindow;
import com.midnight.rpc.core.meta.ProviderMeta;
import com.midnight.rpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.midnight.rpc.core.api.RpcException.ExceedLimitEx;

@Slf4j
public class ProviderInvoker {

    private final MultiValueMap<String, ProviderMeta> skeletons;
    private final ProviderConfigProperties providerProperties;
    private final Map<String, SlidingTimeWindow> windows = new HashMap<>();

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeletons = providerBootstrap.getSkeletons();
        this.providerProperties = providerBootstrap.getProviderProperties();
    }

    public RpcResponse<Object> invoke(RpcRequest request) {
        log.debug(" ===> ProviderInvoker.invoke(request:{})", request);
        if (!request.getParams().isEmpty()) {
            request.getParams().forEach(RpcContext::setContextParameter);
        }

        RpcResponse<Object> rpcResponse = new RpcResponse<>();
        String service = request.getService();

        // 限流
        isTrafficControl(service);

        List<ProviderMeta> metas = skeletons.get(service);
        // 使用方法签名查询提供者元信息
        ProviderMeta meta = findProviderMeta(metas, request.getMethodSign());
        try {
            Method method = meta.getMethod();

            // 参数类型转换
            Object[] args = processArgs(request.getArgs(),
                    method.getParameterTypes(),
                    method.getGenericParameterTypes());

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
        } finally {
            // 防止内存泄漏和上下文污染
            RpcContext.CONTEXT_PARAMETERS.get().clear();
        }
        return rpcResponse;
    }

    private void isTrafficControl(String service) {
        int trafficControl = Integer.parseInt(providerProperties.getMetas().getOrDefault("tc", "20"));
        log.debug(" ===>> trafficControl:{} for {}", trafficControl, service);
        synchronized (windows) {
            SlidingTimeWindow window = windows.computeIfAbsent(service, k -> new SlidingTimeWindow());
            if (window.calcSum() > trafficControl) {
                log.debug(String.valueOf(window));
                throw new RpcException("service " + service + " invoked in 30s/[" +
                        window.getSum() + "] larger than tpsLimit = " + trafficControl, ExceedLimitEx);
            }

            window.record(System.currentTimeMillis());
            log.debug("service {} in window with {}", service, window.getSum());
        }
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes, Type[] genericParameterTypes) {
        if (args == null || args.length == 0) return args;
        Object[] actuals = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actuals[i] = TypeUtils.castGeneric(args[i], parameterTypes[i], genericParameterTypes[i]);
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
