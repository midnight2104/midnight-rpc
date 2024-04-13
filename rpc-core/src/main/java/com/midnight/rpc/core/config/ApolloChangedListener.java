package com.midnight.rpc.core.config;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Apollo config changed listener
 */
@Data
@Slf4j
public class ApolloChangedListener implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @ApolloConfigChangeListener({"rpc-demo-provider.yaml", "application"})
    private void changedHandler(ConfigChangeEvent changeEvent) {
        for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            log.info("Found change - {}", change.toString());
        }

        // 更新相应bean的赋值，主要是存在@ConfigurationProperties注解的bena
        this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
    }
}
