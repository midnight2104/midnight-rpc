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
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class RpcInvocationHandler implements InvocationHandler {
    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    private Class<?> service;

    public RpcInvocationHandler(Class<?> service) {
        this.service = service;
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

        // 发起远程调用
        RpcResponse rpcResponse = post(request);
        if (Boolean.TRUE.equals(rpcResponse.getStatus())) {
            Object data = rpcResponse.getData();
            Class<?> type = method.getReturnType();
            System.out.println("method.getReturnType() = " + type);
            if (data instanceof JSONObject jsonResult) {
                if (Map.class.isAssignableFrom(type)) {
                    Map resultMap = new HashMap();
                    Type genericReturnType = method.getGenericReturnType();
                    System.out.println(genericReturnType);
                    if (genericReturnType instanceof ParameterizedType parameterizedType) {
                        Class<?> keyType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
                        Class<?> valueType = (Class<?>)parameterizedType.getActualTypeArguments()[1];
                        System.out.println("keyType  : " + keyType);
                        System.out.println("valueType: " + valueType);
                        jsonResult.entrySet().stream().forEach(
                                e -> {
                                    Object key = TypeUtils.cast(e.getKey(), keyType);
                                    Object value = TypeUtils.cast(e.getValue(), valueType);
                                    resultMap.put(key, value);
                                }
                        );
                    }
                    return resultMap;
                }
                return jsonResult.toJavaObject(type);
            } else if (data instanceof JSONArray jsonArray) {
                Object[] array = jsonArray.toArray();
                if (type.isArray()) {
                    Class<?> componentType = type.getComponentType();
                    Object resultArray = Array.newInstance(componentType, array.length);
                    for (int i = 0; i < array.length; i++) {
                        Array.set(resultArray, i, array[i]);
                    }
                    return resultArray;
                } else if (List.class.isAssignableFrom(type)) {
                    List<Object> resultList = new ArrayList<>(array.length);
                    Type genericReturnType = method.getGenericReturnType();
                    System.out.println(genericReturnType);
                    if (genericReturnType instanceof ParameterizedType parameterizedType) {
                        Type actualType = parameterizedType.getActualTypeArguments()[0];
                        System.out.println(actualType);
                        for (Object obj : array) {
                            resultList.add(TypeUtils.cast(obj, (Class<?>) actualType));
                        }
                    } else {
                        resultList.addAll(Arrays.asList(array));
                    }
                    return resultList;
                } else {
                    return null;
                }
            } else {
                return TypeUtils.cast(data, type);
            }
        } else {
            Exception ex = rpcResponse.getEx();
            //ex.printStackTrace();
            throw new RuntimeException(ex);
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
