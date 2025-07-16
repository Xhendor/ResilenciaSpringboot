package com.example.vehiculosapi.health;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class HealthInitializer implements CommandLineRunner {

    private final AppHealthIndicator healthIndicator;

    public HealthInitializer(AppHealthIndicator healthIndicator) {
        this.healthIndicator = healthIndicator;
    }

    @Override
    public void run(String... args) {
        // Por defecto, la aplicación inicia como lista (ready) y viva (live)
        healthIndicator.setReady(true);
        healthIndicator.setLive(true);
    }
}
