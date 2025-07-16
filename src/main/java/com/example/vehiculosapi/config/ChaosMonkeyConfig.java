package com.example.vehiculosapi.config;

import de.codecentric.spring.boot.chaos.monkey.configuration.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@EnableConfigurationProperties(ChaosMonkeyProperties.class)
@ConditionalOnProperty(prefix = "chaos.monkey", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChaosMonkeyConfig {

    @Bean
    @ConditionalOnMissingBean
    public AssaultProperties assaultProperties() {
        AssaultProperties properties = new AssaultProperties();
        // Configuración por defecto
        properties.setLevel(3);
        properties.setDeterministic(true);
        properties.setLatencyActive(true);
        properties.setLatencyRangeStart(1000);
        properties.setLatencyRangeEnd(3000);
        properties.setExceptionsActive(true);
        properties.setMemoryActive(true);
        properties.setMemoryMillisecondsWaitNextIncrease(15000);
        properties.getException().setArguments(
                java.util.Collections.singletonList(
                        new AssaultException.ExceptionArgument("java.lang.RuntimeException", "Simulated exception")
                )
        );
        return properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public WatcherProperties watcherProperties() {
        WatcherProperties properties = new WatcherProperties();
        properties.setController(true);
        properties.setRestController(true);
        properties.setService(true);
        properties.setRepository(true);
        properties.setComponent(true);

        return properties;
    }

    @Bean
    public ChaosMonkeySettings chaosMonkeySettings(
            ChaosMonkeyProperties chaosMonkeyProperties,
            AssaultProperties assaultProperties,
            WatcherProperties watcherProperties) {
        return new ChaosMonkeySettings(chaosMonkeyProperties, assaultProperties, watcherProperties);
    }


}
