package com.midnight.rpc.demo.consumer;

import com.midnight.rpc.core.test.TestZkServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@Slf4j
@SpringBootTest(classes = {RpcDemoConsumerApplication.class})
class RpcDemoConsumerApplicationTests {
    private static ApplicationContext context;
    private static TestZkServer zkServer = new TestZkServer();

    @BeforeAll
    public static void init() {
        log.info("============================");
        log.info("============================");
        log.info("============================");
        zkServer.start();
        context = SpringApplication.run(RpcDemoConsumerApplication.class,
                "--server.port=8094", "--midnightrpc.zkServer=localhost:2182",
                "--logging.level.com.midnight.rpc=info");
    }

    @Test
    void contextLoads() {
        System.out.println("===> RpcDemoConsumerApplicationTests");
    }

    @AfterAll
    public static void destroy() {
        SpringApplication.exit(context, () -> 1);
        zkServer.stop();
    }

}
