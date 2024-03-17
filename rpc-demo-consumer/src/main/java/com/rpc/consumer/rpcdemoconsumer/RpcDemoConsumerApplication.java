package com.rpc.consumer.rpcdemoconsumer;

import com.midnight.rpc.core.annotation.RpcConsumer;
import com.midnight.rpc.core.consumer.ConsumerConfig;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/")
    public User findBy(int id) {
        return userService.findById(id);
    }

    @Bean
    public ApplicationRunner consumerRunner() {
        return x -> {

         //   System.out.println(" userService.getId(10f) = " + userService.getId(10f));

            System.out.println(" userService.getId(new User(100,\"Midnight\")) = " +
                    userService.getId(new User(100, "Midnight")));

            User user = userService.findById(1);
            System.out.println("RPC result userService.findById(1) = " + user);

            User user1 = userService.findById(1, "Midnight");
            System.out.println("RPC result userService.findById(1, \"Midnight\") = " + user1);

            System.out.println(userService.getName());

            System.out.println(userService.getName(123));

            System.out.println(userService.toString());

            System.out.println(userService.getId(11));

            System.out.println(userService.getName());

            System.out.println(" ===> userService.getLongIds()");
            for (long id : userService.getLongIds()) {
                System.out.println(id);
            }

            System.out.println(" ===> userService.getLongIds()");
            for (long id : userService.getIds(new int[]{4, 5, 6})) {
                System.out.println(id);
            }

        };
    }
}
