package com.midnight.rpc.core.filter;

import com.midnight.rpc.core.api.Filter;
import com.midnight.rpc.core.api.RpcRequest;
import com.midnight.rpc.core.api.RpcResponse;
import com.midnight.rpc.core.util.MethodUtils;
import com.midnight.rpc.core.util.MockUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * mock过滤器
 */
public class MockFilter implements Filter {

    @SneakyThrows
    @Override
    public Object preFilter(RpcRequest request) {
        Class<?> service = Class.forName(request.getService());
        Method method = findMethod(service, request.getMethodSign());
        Class<?> clazz = method.getReturnType();
        return MockUtils.mock(clazz);
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        return null;
    }

    private Method findMethod(Class service, String methodSign) {
        return Arrays.stream(service.getMethods())
                .filter(method -> !MethodUtils.checkLocalMethod(method))
                .filter(method -> methodSign.equals(MethodUtils.methodSign(method)))
                .findFirst()
                .orElse(null);
    }
}
