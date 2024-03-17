package com.midnight.rpc.core.api;

import java.util.List;

/**
 * Router路由用于预筛选
 * Dubbo有这样的设计，SpringCloud没有
 *
 * @param <T>
 */
public interface Router<T> {

    List<T> route(List<T> providers);

    Router Default = p -> p;

}
