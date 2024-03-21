package com.midnight.rpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 描述服务实例的云数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstanceMeta {
    private String schema;
    private String host;
    private Integer port;
    private String context;
    /**
     * online or offline
     */
    private boolean status;

    private Map<String, String> parameters;

    public InstanceMeta(String schema, String host, Integer port, String context) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public String toURL() {
        return String.format("%s://%s:%d/%s", schema, host, port, context);
    }

    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta("http", host, port, "");
    }
}
