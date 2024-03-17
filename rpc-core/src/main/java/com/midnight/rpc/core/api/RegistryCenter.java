package com.midnight.rpc.core.api;

import java.util.List;

/**
 * 注册中心
 */
public interface RegistryCenter {
    void start();

    void stop();


    // provider侧
    void register(String service, String instance);

    void unregister(String service, String instance);

    // consumer侧
    List<String> fetchAll(String service);
    // void subscribe();

    /**
     * 静态的注册中心，为后续动态做准备
     */
    class StaticRegistryCenter implements RegistryCenter {

        private List<String> providers;

        public StaticRegistryCenter(List<String> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void register(String service, String instance) {

        }

        @Override
        public void unregister(String service, String instance) {

        }

        @Override
        public List<String> fetchAll(String service) {
            return providers;
        }
    }
}
