package com.midnight.rpc.core.util;

import com.midnight.rpc.core.annotation.RpcConsumer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodUtils {

    public static boolean checkLocalMethod(String method) {
        // 本地方法不代理
        return "toString".equals(method) ||
                "hashCode".equals(method) ||
                "notifyAll".equals(method) ||
                "equals".equals(method) ||
                "wait".equals(method) ||
                "getClass".equals(method) ||
                "notify".equals(method);
    }

    public static boolean checkLocalMethod(Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }

    public static String methodSign(Method method) {
        StringBuilder sb = new StringBuilder(method.getName());
        sb.append("@").append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes()).forEach(
                p -> sb.append("_").append(p.getCanonicalName()));

        return sb.toString();
    }

    public static List<Field> findConsumerField(Class<?> aClass) {
        List<Field> res = new ArrayList<>();

        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(RpcConsumer.class)) {
                    res.add(f);
                }
            }
            // bean有可能被CGLIB增强了
            aClass = aClass.getSuperclass();
        }

        return res;
    }
}
