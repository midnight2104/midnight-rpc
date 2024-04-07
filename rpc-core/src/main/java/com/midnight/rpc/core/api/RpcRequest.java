package com.midnight.rpc.core.api;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RpcRequest {
    /**
     * 接口
     */
    private String service;

    /**
     * 方法名
     */
    private String methodSign;

    /**
     * 参数
     */
    private Object[] args;

    //跨调用方传递参数
    private Map<String, String> params = new HashMap<>();
}
