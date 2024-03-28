package com.midnight.rpc.core.consumer;

import com.midnight.rpc.core.api.Filter;
import com.midnight.rpc.core.api.LoadBalancer;
import com.midnight.rpc.core.api.RegistryCenter;
import com.midnight.rpc.core.api.Router;
import com.midnight.rpc.core.cluster.RoundRobinLoadBalancer;
import com.midnight.rpc.core.filter.CacheFilter;
import com.midnight.rpc.core.filter.MockFilter;
import com.midnight.rpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;


@Configuration
public class ConsumerConfig {
    @Value("${midnightrpc.providers}")
    private String servers;

    @Bean
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerBootstrapRunner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> consumerBootstrap.start();
    }

    @Bean
    public LoadBalancer loadBalancer() {
        return new RoundRobinLoadBalancer();
    }

    @Bean
    public Router router() {
        return Router.Default;
    }


    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumerRc() {
        return new ZkRegistryCenter();
    }


    @Bean
    public Filter defaultFilter() {
        return Filter.Default;
    }

//    @Bean
//    public Filter filter1() {
//        return new CacheFilter();
//    }

//     cache 和 mock 二选一
//    @Bean
//    public Filter filter2() {
//        return new MockFilter();
//    }
}
