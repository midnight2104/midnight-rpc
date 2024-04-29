package com.midnight.rpc.core.registry.mid;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.midnight.rpc.core.api.RegistryCenter;
import com.midnight.rpc.core.consumer.HttpInvoker;
import com.midnight.rpc.core.meta.InstanceMeta;
import com.midnight.rpc.core.meta.ServiceMeta;
import com.midnight.rpc.core.registry.ChangedListener;
import com.midnight.rpc.core.registry.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MidnightRegistryCenter implements RegistryCenter {
    private static final String REG_PATH = "/reg";
    private static final String UNREG_PATH = "/unreg";
    private static final String FINDALL_PATH = "/findAll";
    private static final String VERSION_PATH = "/version";
    private static final String RENEWS_PATH = "/renews";

    @Value("${midnightregistry.servers}")
    private String servers;

    private Map<String, Long> VERSIONS = new HashMap<>();
    private MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();
    private MidnightHealthChecker healthChecker = new MidnightHealthChecker();

    @Override
    public void start() {
        log.info(" ====>>>> [MidnightRegistry] : start with server : {}", servers);
        healthChecker.start();
        providerCheck();
    }

    @Override
    public void stop() {
        log.info(" ====>>>> [MidnightRegistry] : stop with server : {}", servers);
        healthChecker.stop();
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>>> [MidnightRegistry] : register instance {} for {}", instance, service);

        HttpInvoker.httpPost(JSON.toJSONString(instance),
                regPath(service), InstanceMeta.class);
        log.info(" ====>>>> [MidnightRegistry] : registered {}", instance);

        RENEWS.add(instance, service);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>>> [MidnightRegistry] : unregister instance {} for {}", instance, service);

        HttpInvoker.httpPost(JSON.toJSONString(instance),
                unregPath(service), InstanceMeta.class);
        log.info(" ====>>>> [MidnightRegistry] : unregistered {}", instance);

        RENEWS.remove(instance, service);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ====>>>> [MidnightRegistry] : find all instances for {}", service);

        List<InstanceMeta> instances = HttpInvoker.httpGet(findAllPath(service), new TypeReference<List<InstanceMeta>>() {
        });
        log.info(" ====>>>> [MidnightRegistry] : findAll = {}", instances);

        return instances;
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        healthChecker.consumerCheck(() -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            Long newVersion = HttpInvoker.httpGet(versionPath(service), Long.class);
            log.info(" ====>>>> [MidnightRegistry] : version = {}, newVersion = {}", version, newVersion);

            if (newVersion > version) {
                List<InstanceMeta> instances = fetchAll(service);
                listener.fire(new Event(instances));
                VERSIONS.put(service.toPath(), newVersion);
            }

        });
    }

    public void providerCheck() {
        healthChecker.providerCheck(() -> RENEWS.keySet().forEach(
                instance -> {
                    Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance),
                            renewsPath(RENEWS.get(instance)), Long.class);
                    log.info(" ====>>>> [MidnightRegistry] : renew instance {} at {}", instance, timestamp);
                }
        ));
    }


    private String renewsPath(List<ServiceMeta> serviceList) {
        return path(RENEWS_PATH, serviceList);
    }

    private String regPath(ServiceMeta service) {
        return path(REG_PATH, service);
    }

    private String unregPath(ServiceMeta service) {
        return path(UNREG_PATH, service);
    }

    private String findAllPath(ServiceMeta service) {
        return path(FINDALL_PATH, service);
    }

    private String versionPath(ServiceMeta service) {
        return path(VERSION_PATH, service);
    }

    private String path(String context, ServiceMeta service) {
        return servers + context + "?service=" + service.toPath();
    }


    private String path(String context, List<ServiceMeta> serviceList) {
        StringBuilder sb = new StringBuilder();
        for (ServiceMeta service : serviceList) {
            sb.append(service.toPath()).append(",");
        }
        String services = sb.toString();
        if (services.endsWith(",")) {
            services = services.substring(0, services.length() - 1);
        }
        return servers + context + "?services=" + services;
    }

}
