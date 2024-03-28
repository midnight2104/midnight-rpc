package com.midnight.rpc.core.api;

import lombok.Data;

@Data
public class RpcException extends RuntimeException {

    //X: 技术类异常
    //Y: 业务类异常
    //Z: 未知异常
    public static final String SOCKET_TIMEOUT = "X001" + "-" + "http_invoke_timeout";
    public static final String STOCK_NOT_ENOUGH = "Y001" + "-" + "http_invoke_timeout";
    public static final String UNKNOWN = "Z001" + "-" + "unknown";

    private String errcode;

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }
    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(Throwable cause, String errcode) {
        super(cause);
        this.errcode = errcode;
    }

}
