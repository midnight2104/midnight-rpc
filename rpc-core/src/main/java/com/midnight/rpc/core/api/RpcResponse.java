package com.midnight.rpc.core.api;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse<T> {
    /**
     * 状态
     */
    private Boolean status;

    /**
     * 响应结果
     */
    private T data;
}
