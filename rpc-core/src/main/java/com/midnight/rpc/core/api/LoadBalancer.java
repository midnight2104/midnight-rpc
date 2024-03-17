package com.midnight.rpc.core.api;

import java.util.List;

public interface LoadBalancer<T> {

    T choose(List<T> providers);

    /**
     * 默认的负载均衡算法：取第一个
     */
    LoadBalancer Default = p -> (p == null || p.size() == 0) ? null : p.get(0);
}
