package com.midnight.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "midnightrpc.app")
public class AppConfigProperties {
    private String id = "app1";
    private String namespace = "public";
    private String env = "dev";
}
