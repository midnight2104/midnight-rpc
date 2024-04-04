package com.midnight.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "midnightrpc.consumer")
public class ConsumerConfigProperties {
    private int retries = 1;
    private int timeout = 1000;
    private int faultLimit = 3;
    private int halfOpenInitialDelay = 10_000;
    private int halfOpenDelay = 60_000;

}
