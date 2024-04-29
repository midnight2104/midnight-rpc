package com.midnight.rpc.core.registry.mid;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MidnightHealthChecker {
    ScheduledExecutorService providerExecutor = null;
    ScheduledExecutorService consumerExecutor = null;

    public void start() {
        log.info(" ====>>>> [MidnightRegistry] : start with health checker.");
        providerExecutor = Executors.newScheduledThreadPool(1);
        consumerExecutor = Executors.newScheduledThreadPool(1);
    }

    public void stop() {
        log.info(" ====>>>> [MidnightRegistry] : stop with health checker.");
        gracefulShutdown(providerExecutor);
        gracefulShutdown(consumerExecutor);
    }

    public void consumerCheck(Callback callback) {
        consumerExecutor.scheduleAtFixedRate(() -> {
            try {
                callback.call();

            } catch (Exception e) {
                log.error("consumer check:", e);
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    public void providerCheck(Callback callback) {
        providerExecutor.scheduleAtFixedRate(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error("provider check:", e);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void gracefulShutdown(ScheduledExecutorService executor) {
        executor.shutdown();

        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("executor shutdown ", e);
        }
    }


    public interface Callback {
        void call() throws Exception;
    }
}
