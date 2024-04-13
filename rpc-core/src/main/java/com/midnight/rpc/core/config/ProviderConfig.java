package com.midnight.rpc.core.config;

import com.midnight.rpc.core.api.RegistryCenter;
import com.midnight.rpc.core.provider.ProviderBootstrap;
import com.midnight.rpc.core.provider.ProviderInvoker;
import com.midnight.rpc.core.registry.zk.ZkRegistryCenter;
import com.midnight.rpc.core.transport.SpringBootTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Configuration
@Import({AppConfigProperties.class, ProviderConfigProperties.class, SpringBootTransport.class})
public class ProviderConfig {
    @Value("${server.port:8080}")
    private String port;


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    public ApolloChangedListener consumerApolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Bean
    ProviderBootstrap providerBootstrap(@Autowired AppConfigProperties ap,
                                        @Autowired ProviderConfigProperties pp) {
        return new ProviderBootstrap(port, ap, pp);
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    /**
     * Spring 的所有bean加载完成，项目启动成功才进行服务注册，
     * 防止启动过程中就注册，然后启动失败了，已经注册的可能会被调用
     *
     * @param providerBootstrap
     * @return
     */
    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrapRunner(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> providerBootstrap.start();
    }


    @Bean
    public RegistryCenter providerRc() {
        return new ZkRegistryCenter();
    }
}
