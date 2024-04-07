package com.midnight.rpc.core.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 类型转换工具类。
 */

@Slf4j
public class TypeUtils {

    public static Object cast(Object origin, Class<?> type) {
        log.debug("cast: origin = " + origin);
        log.debug("cast: type = " + type);
        if (origin == null) return null;
        Class<?> aClass = origin.getClass();
        if (type.isAssignableFrom(aClass)) {
            log.debug(" ======> assignable {} -> {}", aClass, type);
            return origin;
        }

        if (type.isArray()) {
            return getResultArray(origin, type);
        }

        if (origin instanceof HashMap map) {
            log.debug(" ======> map -> " + type);
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(type);
        }

        if (origin instanceof JSONObject jsonObject) {
            log.debug(" ======> JSONObject -> " + type);
            return jsonObject.toJavaObject(type);
        }

        log.debug(" ======> Primitive types.");
        if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return Integer.valueOf(origin.toString());
        } else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return Long.valueOf(origin.toString());
        } else if (type.equals(Float.class) || type.equals(Float.TYPE)) {
            return Float.valueOf(origin.toString());
        } else if (type.equals(Double.class) || type.equals(Double.TYPE)) {
            return Double.valueOf(origin.toString());
        } else if (type.equals(Byte.class) || type.equals(Byte.TYPE)) {
            return Byte.valueOf(origin.toString());
        } else if (type.equals(Short.class) || type.equals(Short.TYPE)) {
            return Short.valueOf(origin.toString());
        } else if (type.equals(Character.class) || type.equals(Character.TYPE)) {
            return Character.valueOf(origin.toString().charAt(0));
        } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            return Boolean.valueOf(origin.toString());
        }
        return null;
    }

    @NotNull
    private static Object getResultArray(Object origin, Class<?> type) {
        if (origin instanceof List list) {
            origin = list.toArray();
        }
        log.debug(" ======> list/[] -> []/" + type);
        int length = Array.getLength(origin);
        Class<?> componentType = type.getComponentType();
        log.debug(" ======> [] componentType : " + componentType);
        Object resultArray = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            if (componentType.isPrimitive() || componentType.getPackageName().startsWith("java")) {
                Array.set(resultArray, i, Array.get(origin, i));
            } else {
                Object castObject = cast(Array.get(origin, i), componentType);
                Array.set(resultArray, i, castObject);
            }
        }
        return resultArray;
    }

    public static Object castMethodResult(Method method, Object data) {
        log.debug("castMethodResult: method = " + method);
        log.debug("castMethodResult: data = " + data);
        Class<?> type = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        return castGeneric(data, type, genericReturnType);
    }

    public static Object castGeneric(Object data, Class<?> type, Type genericReturnType) {
        log.debug("castGeneric: data = " + data);
        log.debug("castGeneric: method.getReturnType() = " + type);
        log.debug("castGeneric: method.getGenericReturnType() = " + genericReturnType);
        // data是map的情况包括两种，一种是HashMap，一种是JSONObject
        if (data instanceof Map map) {
            // 目标类型是 Map，此时data可能是map也可能是JO
            if (Map.class.isAssignableFrom(type)) {
                return getMap(genericReturnType, map);
            }
            // 此时是Pojo，且数据是JO
            if (data instanceof JSONObject jsonObject) {
                log.debug(" ======> JSONObject -> Pojo");
                return jsonObject.toJavaObject(type);
                // 此时是Pojo类型，数据是Map
            } else if (!Map.class.isAssignableFrom(type)) {
                log.debug(" ======> map -> Pojo");
                return new JSONObject(map).toJavaObject(type);
            } else {
                log.debug(" ======> map -> ?");
                return data;
            }
        } else if (data instanceof List list) {
            return getList(type, genericReturnType, list);
        } else {
            return cast(data, type);
        }
    }

    @Nullable
    private static Object getList(Class<?> type, Type genericReturnType, List list) {
        Object[] array = list.toArray();
        if (type.isArray()) {
            return getArray(type, array);
        } else if (List.class.isAssignableFrom(type)) {
            return getObjectList(genericReturnType, array);
        } else {
            return null;
        }
    }

    @NotNull
    private static List<Object> getObjectList(Type genericReturnType, Object[] array) {
        log.debug(" ======> list -> list");
        List<Object> resultList = new ArrayList<>(array.length);
        log.debug(genericReturnType.toString());
        if (genericReturnType instanceof ParameterizedType parameterizedType) {
            Type actualType = parameterizedType.getActualTypeArguments()[0];
            log.debug(actualType.toString());
            for (Object o : array) {
                resultList.add(cast(o, (Class<?>) actualType));
            }
        } else {
            resultList.addAll(Arrays.asList(array));
        }
        return resultList;
    }

    @NotNull
    private static Object getArray(Class<?> type, Object[] array) {
        log.debug(" ======> list -> []");
        Class<?> componentType = type.getComponentType();
        Object resultArray = Array.newInstance(componentType, array.length);
        for (int i = 0; i < array.length; i++) {
            if (componentType.isPrimitive() || componentType.getPackageName().startsWith("java")) {
                Array.set(resultArray, i, array[i]);
            } else {
                Object castObject = cast(array[i], componentType);
                Array.set(resultArray, i, castObject);
            }
        }
        return resultArray;
    }

    @NotNull
    private static Map getMap(Type genericReturnType, Map map) {
        log.debug(" ======> map -> map");
        Map resultMap = new HashMap();
        log.debug(genericReturnType.toString());
        if (genericReturnType instanceof ParameterizedType parameterizedType) {
            Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
            log.debug("keyType  : " + keyType);
            log.debug("valueType: " + valueType);
            map.forEach(
                    (k, v) -> {
                        Object key = cast(k, keyType);
                        Object value = cast(v, valueType);
                        resultMap.put(key, value);
                    }
            );
        }
        return resultMap;
    }
}
