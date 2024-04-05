package com.midnight.rpc.core.consumer;

import com.midnight.rpc.core.api.*;
import com.midnight.rpc.core.consumer.http.OkHttpInvoker;
import com.midnight.rpc.core.governance.SlidingTimeWindow;
import com.midnight.rpc.core.meta.InstanceMeta;
import com.midnight.rpc.core.util.MethodUtils;
import com.midnight.rpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcInvocationHandler implements InvocationHandler {

    private final Class<?> service;
    private final RpcContext context;
    private final List<InstanceMeta> providers;
    private final List<InstanceMeta> isolatedProviders = new ArrayList<>();
    private final List<InstanceMeta> halfOpenProviders = new ArrayList<>();
    private final Map<String, SlidingTimeWindow> windows = new HashMap<>();
    private final OkHttpInvoker httpInvoker;
    private ScheduledExecutorService executor;


    public RpcInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
        int timeout = Integer.parseInt(context.getParameters()
                .getOrDefault("app.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);

        // 一个定时线程池
        this.executor = Executors.newScheduledThreadPool(1);
        int halfOpenInitialDelay = Integer.parseInt(context.getParameters()
                .getOrDefault("consumer.halfOpenInitialDelay", "10000"));
        int halfOpenDelay = Integer.parseInt(context.getParameters()
                .getOrDefault("consumer.halfOpenDelay", "60000"));
        // 10秒后启动，每隔60s执行一次halfOpen
        this.executor.scheduleWithFixedDelay(this::halfOpen,
                halfOpenInitialDelay, halfOpenDelay, TimeUnit.MILLISECONDS);
    }

    private void halfOpen() {
        log.debug("===> half open isolated providers: " + isolatedProviders);
        halfOpenProviders.clear();
        halfOpenProviders.addAll(isolatedProviders);
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

        InstanceMeta instance;
        synchronized (halfOpenProviders) {
            if (CollectionUtils.isEmpty(halfOpenProviders)) {
                // 负载均衡
                List<InstanceMeta> instances = context.getRouter().route(providers);
                instance = context.getLoadBalancer().choose(instances);
                log.debug("===> loadBalancer choose():" + instance);
            } else {
                // 探活
                instance = halfOpenProviders.remove(0);
                log.debug("check alive instance ==> {}", instance);
            }
        }

        RpcResponse rpcResponse;
        Object result;
        String url = instance.toURL();
        int faultLimit = Integer.parseInt(context.getParameters()
                .getOrDefault("consumer.faultLimit", "3"));
        try {
            // 发起远程调用
            rpcResponse = httpInvoker.post(request, url);
            // 处理结果
            result = castReturnResult(method, rpcResponse);
        } catch (Exception e) {
            // 故障的统计规则和隔离
            // 每一次的异常记录一次，统计30s的异常数
            synchronized (windows) {
                // 一个实例对应一个滑动窗口
                SlidingTimeWindow window = windows.computeIfAbsent(url, k -> new SlidingTimeWindow());
                window.record(System.currentTimeMillis());

                log.debug("instance {}  in window with {}", url, window.getSum());
                log.debug("===> faultLimit is {}", faultLimit);
                // 30s内异常次数超过指定阈值faultLimit，就隔离当前实例
                if (window.getSum() >= faultLimit) {
                    // 故障实例隔离
                    isolate(instance);
                }

            }
            throw e;
        }

        // 故障实例恢复
        synchronized (providers) {
            if (!providers.contains(instance)) {
                isolatedProviders.remove(instance);
                providers.add(instance);
                log.debug("instance {} is recovered, isolatedProviders={}, providers={}",
                        instance, isolatedProviders, providers);
            }
        }


        // 后置过滤器
        for (Filter filter : context.getFilters()) {
            Object postResult = filter.postFilter(request, rpcResponse, result);
            if (postResult != null) {
                return postResult;
            }
        }

        return result;
    }

    private void isolate(InstanceMeta instance) {
        log.debug("===> isolate  instance: " + instance);

        providers.remove(instance);
        log.debug("===> providers  = {} ", providers);

        isolatedProviders.add(instance);
        log.debug("===> isolated providers  = {} ", isolatedProviders);
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
