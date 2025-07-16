package com.example.vehiculosapi.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "API para gestionar el estado de salud de la aplicación")
public class HealthCheckController {

    private final AppHealthIndicator healthIndicator;

    public HealthCheckController(AppHealthIndicator healthIndicator) {
        this.healthIndicator = healthIndicator;
    }

    @PostMapping("/ready/{status}")
    @Operation(summary = "Establecer el estado de readiness")
    public ResponseEntity<String> setReadyStatus(@PathVariable boolean status) {
        healthIndicator.setReady(status);
        return ResponseEntity.ok("Readiness state set to: " + status);
    }

    @PostMapping("/live/{status}")
    @Operation(summary = "Establecer el estado de liveness")
    public ResponseEntity<String> setLiveStatus(@PathVariable boolean status) {
        healthIndicator.setLive(status);
        return ResponseEntity.ok("Liveness state set to: " + status);
    }

    @GetMapping("/status")
    @Operation(summary = "Obtener el estado actual de salud")
    public ResponseEntity<HealthStatus> getHealthStatus() {
        HealthStatus status = new HealthStatus(
            healthIndicator.isLive(),
            healthIndicator.isReady()
        );
        return ResponseEntity.ok(status);
    }

    public record HealthStatus(boolean live, boolean ready) {}
}
