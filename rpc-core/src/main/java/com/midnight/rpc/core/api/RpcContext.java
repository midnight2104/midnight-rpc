package com.midnight.rpc.core.api;

import com.midnight.rpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RpcContext {
    private List<Filter> filters;
    private LoadBalancer<InstanceMeta> loadBalancer;
    private Router<InstanceMeta> router;

    private Map<String, String> parameters = new HashMap<>();
}
