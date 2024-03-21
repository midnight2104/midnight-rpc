package com.midnight.rpc.demo.provider;

import com.midnight.rpc.core.api.RpcRequest;
import com.midnight.rpc.core.api.RpcResponse;
import com.midnight.rpc.core.provider.ProviderBootstrap;
import com.midnight.rpc.core.provider.ProviderConfig;
import com.midnight.rpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class RpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcDemoProviderApplication.class, args);
    }

    @Autowired
    private ProviderInvoker providerInvoker;

    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

}
