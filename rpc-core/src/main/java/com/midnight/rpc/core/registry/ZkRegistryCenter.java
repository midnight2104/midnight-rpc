package com.midnight.rpc.core.registry;

import com.midnight.rpc.core.api.RegistryCenter;
import com.midnight.rpc.core.meta.InstanceMeta;
import com.midnight.rpc.core.meta.ServiceMeta;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于ZooKeeper实现的注册中心
 */
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {
    private CuratorFramework client = null;
    @Value("${midnightrpc.zkServer}")
    String servers;

    @Value("${midnightrpc.zkRoot}")
    String root;

    @Override
    public void start() {
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(servers)
                .namespace(root)
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
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 创建持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }

            // 创建实例的临时节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info("===> register to zk :" + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 服务路径不存在直接返回
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }

            String instancePath = servicePath + "/" + instance.toPath();
            log.info("===> unregister to zk :" + instancePath);
            // quietly删除：没有实例也不要报错
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info("===> fetchAll from zk : " + servicePath);
            return mapInstance(nodes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        TreeCache cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true)
                .setMaxDepth(2)
                .build();
        cache.getListenable()
                .addListener((curator, event) -> {
                    // 有节点发生变动这里就会执行
                    log.info("zk subscribe event:   " + event);
                    List<InstanceMeta> nodes = fetchAll(service);
                    listener.fire(new Event(nodes));
                });

        // 不要忘记启动和关闭
        cache.start();
    }

    private List<InstanceMeta> mapInstance(List<String> nodes) {
        return nodes.stream()
                .map(x -> {
                    String[] split = x.split("_");
                    return InstanceMeta.http(split[0], Integer.valueOf(split[1]));
                })
                .collect(Collectors.toList());
    }

}
