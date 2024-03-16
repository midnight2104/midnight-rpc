package com.rpc.consumer.rpcdemoconsumer;

import com.midnight.rpc.core.annotation.RpcConsumer;
import com.midnight.rpc.core.consumer.ConsumerConfig;
import com.midnight.rpc.demo.api.Order;
import com.midnight.rpc.demo.api.OrderService;
import com.midnight.rpc.demo.api.User;
import com.midnight.rpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import({ConsumerConfig.class})
public class RpcDemoConsumerApplication {

    @Autowired
    ApplicationContext context;

    @RpcConsumer
    UserService userService;

    @RpcConsumer
    OrderService orderService;

    @Autowired
    Demo2 demo2;

    public static void main(String[] args) {
        SpringApplication.run(RpcDemoConsumerApplication.class, args);
    }


    @Bean
    public ApplicationRunner consumerRunner() {
        return x -> {
            // 类对象的远程调用
            User user = userService.findById(19);
            System.out.println("类对象的远程调用 result userService.findById(19) = " + user);

            User user2 = userService.findById(19, "hello");
            System.out.println("重载方法 findById  = " + user2);

        };
    }
}
