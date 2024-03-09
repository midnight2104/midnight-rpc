package com.midnight.rpc.core.api;

import lombok.Data;

@Data
public class RpcRequest {
    /**
     * 接口
     */
    private String service;

    /**
     * 方法名
     */
    private String method;

    /**
     * 参数
     */
    private Object[] args;
}
