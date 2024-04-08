package com.midnight.rpc.core.transport;

import com.midnight.rpc.core.api.RpcRequest;
import com.midnight.rpc.core.api.RpcResponse;
import com.midnight.rpc.core.provider.ProviderInvoker;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目对外暴露的端口
 */
@RestController
public class SpringBootTransport {

    @Resource
    private ProviderInvoker invoker;

    @RequestMapping("/midnight-rpc")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        return invoker.invoke(request);
    }
}
