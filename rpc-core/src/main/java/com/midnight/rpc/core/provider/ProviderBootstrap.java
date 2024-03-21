package com.midnight.rpc.core.provider;

import com.midnight.rpc.core.annotation.RpcProvider;
import com.midnight.rpc.core.api.RegistryCenter;
import com.midnight.rpc.core.meta.InstanceMeta;
import com.midnight.rpc.core.meta.ProviderMeta;
import com.midnight.rpc.core.meta.ServiceMeta;
import com.midnight.rpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

@Data
public class ProviderBootstrap implements ApplicationContextAware {
    // 变量名称保持一致就可以不用写抽象方法。
    private ApplicationContext applicationContext;

    private MultiValueMap<String, ProviderMeta> skeletons = new LinkedMultiValueMap<>();

    private InstanceMeta instance;
    private RegistryCenter rc;

    @Value("${server.port}")
    private String port;


    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;

    // 在bean的初始化过程中，保存起来
    @PostConstruct
    public void init() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(RpcProvider.class);
        rc = applicationContext.getBean(RegistryCenter.class);

        providers.values().forEach(x -> genInterface(x));
    }


    @SneakyThrows
    public void start() {
        String ip = InetAddress.getLocalHost().getHostAddress();
        instance = InstanceMeta.http(ip, Integer.valueOf(port));
        rc.start();
        // 服务端provider在启动过程中完成注册
        skeletons.keySet().forEach(this::registerService);
    }

    /***
     * 服务销毁时，取消注册
     */
    @PreDestroy
    public void stop() {
        skeletons.keySet().forEach(this::unregisterService);
        rc.stop();
    }

    private void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder().namespace(namespace).name(service).env(env).app(app).build();
        rc.register(serviceMeta, instance);
    }

    private void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder().namespace(namespace).name(service).env(env).app(app).build();
        rc.unregister(serviceMeta, instance);
    }

    private void genInterface(Object impl) {
        // 实现多个接口
        Arrays.stream(impl.getClass().getInterfaces()).forEach(
                service -> Arrays.stream(service.getMethods())
                        .filter(method -> !MethodUtils.checkLocalMethod(method))
                        .forEach(method -> {
                            createProvider(service, impl, method);
                        })
        );

    }

    private void createProvider(Class<?> inter, Object x, Method method) {
        ProviderMeta meta = new ProviderMeta();
        meta.setMethod(method);
        meta.setMethodSign(MethodUtils.methodSign(method));
        meta.setServiceImpl(x);

        skeletons.add(inter.getCanonicalName(), meta);
    }


}
