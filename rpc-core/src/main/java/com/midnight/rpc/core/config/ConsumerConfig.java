package com.midnight.rpc.core.config;

import com.midnight.rpc.core.api.*;
import com.midnight.rpc.core.cluster.GrayRouter;
import com.midnight.rpc.core.cluster.RoundRobinLoadBalancer;
import com.midnight.rpc.core.consumer.ConsumerBootstrap;
import com.midnight.rpc.core.filter.ParameterFilter;
import com.midnight.rpc.core.meta.InstanceMeta;
import com.midnight.rpc.core.registry.zk.ZkRegistryCenter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;


@Slf4j
@Configuration
@Import({AppConfigProperties.class, ConsumerConfigProperties.class})
public class ConsumerConfig {
    @Resource
    private AppConfigProperties appConfigProperties;

    @Resource
    private ConsumerConfigProperties consumerConfigProperties;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    public ApolloChangedListener consumerApolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Bean
    public ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerBootstrapRunner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> consumerBootstrap.start();
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        return new RoundRobinLoadBalancer<>();
    }

    @Bean
    public Router<InstanceMeta> router() {
        return new GrayRouter(consumerConfigProperties.getGrayRatio());
    }


    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumerRc() {
        return new ZkRegistryCenter();
    }


    @Bean
    public Filter defaultFilter() {
        return new ParameterFilter();
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

    @Bean
    public RpcContext createContext(@Autowired Router router,
                                    @Autowired LoadBalancer loadBalancer,
                                    @Autowired List<Filter> filters) {

        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);

        context.getParameters().put("app.id", appConfigProperties.getId());
        context.getParameters().put("app.namespace", appConfigProperties.getNamespace());
        context.getParameters().put("app.env", appConfigProperties.getEnv());

        context.getParameters().put("consumer.retries", String.valueOf(consumerConfigProperties.getRetries()));
        context.getParameters().put("consumer.timeout", String.valueOf(consumerConfigProperties.getTimeout()));
        context.getParameters().put("consumer.faultLimit", String.valueOf(consumerConfigProperties.getFaultLimit()));
        context.getParameters().put("consumer.halfOpenInitialDelay", String.valueOf(consumerConfigProperties.getHalfOpenInitialDelay()));
        context.getParameters().put("consumer.halfOpenDelay", String.valueOf(consumerConfigProperties.getHalfOpenDelay()));

        return context;
    }

}
