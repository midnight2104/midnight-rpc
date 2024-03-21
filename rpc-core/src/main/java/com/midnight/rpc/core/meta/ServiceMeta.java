package com.midnight.rpc.core.meta;

import lombok.Builder;
import lombok.Data;

/**
 * 描述服务元数据
 */
@Builder
@Data
public class ServiceMeta {
    private String app;
    private String namespace;
    private String env;
    private String name;
//    private String version;

    public String toPath() {
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }
}
