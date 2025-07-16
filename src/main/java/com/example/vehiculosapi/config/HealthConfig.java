package com.example.vehiculosapi.config;

import com.example.vehiculosapi.health.AppHealthIndicator;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.boot.actuate.availability.LivenessStateHealthIndicator;
import org.springframework.boot.actuate.availability.ReadinessStateHealthIndicator;

import javax.sql.DataSource;

@Configuration
public class HealthConfig {

    @Bean
    public HealthIndicator dbHealthIndicator(DataSource dataSource) {
        DataSourceHealthIndicator indicator = new DataSourceHealthIndicator(dataSource);
        indicator.setQuery("SELECT 1 FROM DUAL");
        return indicator;
    }

    @Bean
    public LivenessStateHealthIndicator livenessStateHealthIndicator(org.springframework.boot.availability.ApplicationAvailability applicationAvailability) {
        return new LivenessStateHealthIndicator(applicationAvailability);
    }

    @Bean
    public ReadinessStateHealthIndicator readinessStateHealthIndicator(org.springframework.boot.availability.ApplicationAvailability applicationAvailability) {
        return new ReadinessStateHealthIndicator(applicationAvailability);
    }

    @Bean
    public HealthIndicator customHealthIndicator(AppHealthIndicator appHealth) {
        return () -> {
            if (!appHealth.isLive()) {
                return Health.down()
                    .withDetail("error", "Application is not live")
                    .build();
            }
            if (!appHealth.isReady()) {
                return Health.status("OUT_OF_SERVICE")
                    .withDetail("info", "Application is not ready to accept traffic")
                    .build();
            }
            return Health.up().build();
        };
    }
}
