package com.midnight.rpc.core.consumer;

import com.midnight.rpc.core.api.*;
import com.midnight.rpc.core.consumer.http.OkHttpInvoker;
import com.midnight.rpc.core.meta.InstanceMeta;
import com.midnight.rpc.core.util.MethodUtils;
import com.midnight.rpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.List;

@Slf4j
public class RpcInvocationHandler implements InvocationHandler {

    private Class<?> service;
    private RpcContext context;
    private List<InstanceMeta> providers;
    private OkHttpInvoker httpInvoker;

    public RpcInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
        int timeout = Integer.parseInt(context.getParameters()
                .getOrDefault("app.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 本地方法不走远程调用
        if (MethodUtils.checkLocalMethod(method.getName())) {
            return null;
        }

        // 封装请求参数
        RpcRequest request = new RpcRequest();
        request.setService(service.getCanonicalName());
        request.setMethodSign(MethodUtils.methodSign(method));
        request.setArgs(args);


        // 异常超时重试
        int retry = Integer.parseInt(context.getParameters()
                .getOrDefault("app.retry", "1"));
        while (retry-- > 0) {
            log.info("===> retry: " + retry);
            try {
                return handleInvoke(method, request);
            } catch (Exception ex) {
                if (!(ex.getCause() instanceof SocketTimeoutException)) {
                    throw ex;
                }
            }
        }

        return null;

    }

    private Object handleInvoke(Method method, RpcRequest request) {
        // 前置过滤器
        for (Filter filter : context.getFilters()) {
            Object preResult = filter.preFilter(request);
            if (preResult != null) {
                log.debug(filter.getClass().getName() + " ===> preFilter: " + preResult);
                return preResult;
            }
        }

        // 负载均衡
        List<InstanceMeta> instances = context.getRouter().route(providers);
        InstanceMeta instance = context.getLoadBalancer().choose(instances);
        log.info("===> loadBalancer choose():" + instance);

        // 发起远程调用
        RpcResponse rpcResponse = httpInvoker.post(request, instance.toURL());

        // 处理结果
        Object result = castReturnResult(method, rpcResponse);


        // 后置过滤器
        for (Filter filter : context.getFilters()) {
            Object postResult = filter.postFilter(request, rpcResponse, result);
            if (postResult != null) {
                return postResult;
            }
        }

        return result;
    }


    private Object castReturnResult(Method method, RpcResponse rpcResponse) {
        if (Boolean.TRUE.equals(rpcResponse.getStatus())) {
            Object data = rpcResponse.getData();
            return TypeUtils.castMethodResult(method, data);
        } else {
            Exception ex = rpcResponse.getEx();
            if (ex instanceof RpcException rpcex) {
                throw rpcex;
            } else {
                throw new RpcException(ex, RpcException.UNKNOWN);
            }
        }
    }

}
