package com.midnight.rpc.core.test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.utils.CloseableUtils;

@Slf4j
public class TestZkServer {

    private TestingCluster cluster;

    @SneakyThrows
    public void start() {
        InstanceSpec instanceSpec = new InstanceSpec(null, 2182, -1, -1,
                true, -1, -1, -1);
        cluster = new TestingCluster(instanceSpec);
        log.info("Testing Zookeeper is starting...");
        cluster.start();
        cluster.getServers().forEach(s -> log.info(String.valueOf(s.getInstanceSpecs())));
        log.info("Testing Zookeeper is started...");

    }

    @SneakyThrows
    public void stop() {
        log.info("Testing Zookeeper is stopping...");
        cluster.stop();
        CloseableUtils.closeQuietly(cluster);
        log.info("Testing Zookeeper is stopped...");

    }
}
