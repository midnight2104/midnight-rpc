package com.midnight.rpc.core.registry;

import com.midnight.rpc.core.api.RegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * 基于ZooKeeper实现的注册中心
 */
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {
    private CuratorFramework client = null;

    @Override
    public void start() {
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .namespace("midnightrpc")
                .retryPolicy(policy)
                .build();
        log.info("==> zk client starting.");
        // 不要忘记启动了
        client.start();
    }

    @Override
    public void stop() {
        log.info("==> zk client stopped.");
        client.close();
    }

    @Override
    public void register(String service, String instance) {
        String servicePath = "/" + service;
        try {
            // 创建持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }

            // 创建实例的临时节点
            String instancePath = servicePath + "/" + instance;
            log.info("===> register to zk :" + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregister(String service, String instance) {
        String servicePath = "/" + service;
        try {
            // 服务路径不存在直接返回
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }

            String instancePath = servicePath + "/" + instance;
            log.info("===> unregister to zk :" + instancePath);
            // quietly删除：没有实例也不要报错
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> fetchAll(String service) {
        String servicePath = "/" + service;
        try {
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info("===> fetchAll from zk : " + servicePath);
            nodes.forEach(System.out::println);
            return nodes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribe(String service, ChangedListener listener) throws Exception {
        TreeCache cache = TreeCache.newBuilder(client, "/" + service)
                .setCacheData(true)
                .setMaxDepth(2)
                .build();
        cache.getListenable()
                .addListener((curator, event) -> {
                    // 有节点发生变动这里就会执行
                    log.info("zk subscribe event:   " + event);
                    List<String> nodes = fetchAll(service);
                    listener.fire(new Event(nodes));
                });

        // 不要忘记启动和关闭
        cache.start();
    }
}
