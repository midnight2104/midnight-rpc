package com.rpc.consumer.rpcdemoconsumer;


import com.midnight.rpc.core.annotation.RpcConsumer;
import com.midnight.rpc.demo.api.User;
import com.midnight.rpc.demo.api.UserService;
import org.springframework.stereotype.Component;


@Component
public class Demo2 {

    @RpcConsumer
    UserService userService2;

    public void test() {
        User user = userService2.findById(100);
        System.out.println("嵌套注入 " + user);
    }

}
