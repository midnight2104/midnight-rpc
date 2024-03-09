package com.midnight.rpc.core.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderConfig {

    @Bean
    public ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }
}
