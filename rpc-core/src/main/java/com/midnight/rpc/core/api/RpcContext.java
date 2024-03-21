package com.midnight.rpc.core.api;

import com.midnight.rpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.List;

@Data
public class RpcContext {
    private List<Filter> filters;
    private LoadBalancer<InstanceMeta> loadBalancer;
    private Router<InstanceMeta> router;
}
