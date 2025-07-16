package com.example.vehiculosapi.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Configuration
@EnableScheduling
public class MetricsConfig {

    private final AtomicInteger activeVehicles = new AtomicInteger(0);
    private final MeterRegistry meterRegistry;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Bean
    public Timer vehicleOperationTimer() {
        return Timer.builder("vehiculo.operacion.tiempo")
                .description("Tiempo de operaciones de vehículos")
                .tags("tipo", "operacion")
                .register(meterRegistry);
    }

    @Bean
    public AtomicInteger activeVehiclesGauge() {
        return meterRegistry.gauge("vehiculos.activos", activeVehicles);
    }

    @Scheduled(fixedRate = 30000) // Actualiza cada 30 segundos
    public void updateActiveVehicles() {
        // Aquí podrías implementar la lógica para obtener el número real de vehículos activos
        // Por ahora, solo incrementamos el contador para demostración
        activeVehicles.set((activeVehicles.get() + 1) % 100);
    }

    public <T> T recordOperationTime(Supplier<T> operation, String operationName) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return operation.get();
        } finally {
            sample.stop(meterRegistry.timer("vehiculo.operacion.tiempo", "operacion", operationName));
        }
    }
    
    public void recordOperationTime(Runnable operation, String operationName) {
        recordOperationTime(() -> {
            operation.run();
            return null;
        }, operationName);
    }
}
