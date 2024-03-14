package com.midnight.rpc.demo.provider;

import com.midnight.rpc.core.annotation.RpcProvider;
import com.midnight.rpc.demo.api.Order;
import com.midnight.rpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

@Component
@RpcProvider
public class OrderServiceImpl implements OrderService {
    @Override
    public Order findById(Integer id) {
        if (id == 404) {
            throw new RuntimeException("404 exception");
        }

        return new Order(id.longValue(), 666.6f);
    }
}