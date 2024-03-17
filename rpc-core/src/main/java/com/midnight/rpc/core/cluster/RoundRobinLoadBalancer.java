package com.midnight.rpc.core.cluster;

import com.midnight.rpc.core.api.LoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮训的负载均衡器
 *
 * @param <T>
 */
public class RoundRobinLoadBalancer<T> implements LoadBalancer<T> {
    private AtomicInteger index = new AtomicInteger(0);

    @Override
    public T choose(List<T> providers) {
        if (providers == null || providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        // & 是防止超出int的上限
        return providers.get((index.getAndIncrement() & 0x7fffffff) % providers.size());
    }
}
