package com.midnight.rpc.demo.provider;

import com.midnight.rpc.core.test.TestZkServer;
import com.midnight.rpc.demo.provider.RpcDemoProviderApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = {RpcDemoProviderApplication.class})
class RpcDemoProviderApplicationTests {
    private static TestZkServer zkServer = new TestZkServer();

    @BeforeAll
    public static void init() {
        log.info("============================");
        log.info("============================");
        log.info("============================");
        zkServer.start();

    }

    @Test
    void contextLoads() {
        System.out.println("===> RpcDemoProviderApplicationTests");
    }

    @AfterAll
    public static void destroy() {
        zkServer.stop();
    }

}
