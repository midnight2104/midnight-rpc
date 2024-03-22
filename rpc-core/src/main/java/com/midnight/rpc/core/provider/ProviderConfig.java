package com.midnight.rpc.core.provider;

import com.midnight.rpc.core.api.RegistryCenter;
import com.midnight.rpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ProviderConfig {

    @Bean
    public ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
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
