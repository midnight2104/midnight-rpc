package com.midnight.rpc.core.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.midnight.rpc.core.api.RpcRequest;
import com.midnight.rpc.core.api.RpcResponse;
import com.midnight.rpc.core.util.MethodUtils;
import com.midnight.rpc.core.util.TypeUtils;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class RpcInvocationHandler implements InvocationHandler {
    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    private Class<?> service;

    public RpcInvocationHandler(Class<?> service) {
        this.service = service;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) {
        // 本地方法不走远程调用
        if (MethodUtils.checkLocalMethod(method.getName())) {
            return null;
        }

        // 封装请求参数
        RpcRequest request = new RpcRequest();
        request.setService(service.getCanonicalName());
        request.setMethodSign(MethodUtils.methodSign(method));
        request.setArgs(args);

        // 发起远程调用
        RpcResponse response = post(request);
        if (Boolean.TRUE.equals(response.getStatus())) {
            Object data = response.getData();
            // 处理返回值的参数类型转换
            if (data instanceof JSONObject jsonResult) {
                return jsonResult.toJavaObject(method.getReturnType());
            } else if (data instanceof JSONArray jsonArray) {
                Object[] array = jsonArray.toArray();
                Class<?> componentType = method.getReturnType().getComponentType();
                Object resArray = Array.newInstance(componentType, array.length);
                for (int i = 0; i < array.length; i++) {
                    Array.set(resArray, i, array[i]);
                }
                return resArray;
            } else {
                // 其他类型转换
                return TypeUtils.cast(data, method.getReturnType());
            }
        } else {
            throw new RuntimeException(response.getEx());
        }
    }


    // 通过 OkHttp 发起http请求
    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .readTimeout(600, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();

    private RpcResponse post(RpcRequest rpcRequest) {
        String reqJson = JSON.toJSONString(rpcRequest);

        Request request = new Request.Builder()
                .url("http://localhost:8080/invoke")    // 远程调用，现在还是本地，后续引入注册中心
                .post(RequestBody.create(reqJson, JSONTYPE))
                .build();
        try {
            String respJson = Objects.requireNonNull(client.newCall(request).execute().body()).string();
            return JSON.parseObject(respJson, RpcResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
