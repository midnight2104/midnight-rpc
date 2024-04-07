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

    public String param(String key) {
        return parameters.get(key);
    }

    /**
     * 跨线程传参
     */
    public static ThreadLocal<Map<String, String>> CONTEXT_PARAMETERS = ThreadLocal.withInitial(HashMap::new);


    public static void setContextParameter(String key, String value) {
        CONTEXT_PARAMETERS.get().put(key, value);
    }

    public static String getContextParameter(String key) {
        return CONTEXT_PARAMETERS.get().get(key);
    }

}
