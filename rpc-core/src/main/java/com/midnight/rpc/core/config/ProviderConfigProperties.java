package com.midnight.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "midnightrpc.provider")
public class ProviderConfigProperties {
    private Map<String, String> metas = new HashMap<>();

}
