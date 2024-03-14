package com.midnight.rpc.core.util;

public class MethodUtils {

    public static boolean checkLocalMethod(String method){
        // 本地方法不代理
        return "toString".equals(method) ||
                "hashCode".equals(method) ||
                "notifyAll".equals(method) ||
                "equals".equals(method) ||
                "wait".equals(method) ||
                "getClass".equals(method) ||
                "notify".equals(method);
    }
}
