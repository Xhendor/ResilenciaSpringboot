package com.example.vehiculosapi.chaos;

import de.codecentric.spring.boot.chaos.monkey.configuration.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/api/chaos-monkey")
@Tag(name = "Chaos Monkey", description = "API para gestionar Chaos Monkey")
public class ChaosMonkeyController {

    private final ChaosMonkeySettings chaosMonkeySettings;

    public ChaosMonkeyController(ChaosMonkeySettings chaosMonkeySettings) {
        this.chaosMonkeySettings = chaosMonkeySettings;
    }

    @GetMapping("/status")
    @Operation(summary = "Obtener el estado actual de Chaos Monkey")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", chaosMonkeySettings.getChaosMonkeyProperties().isEnabled());
        
        // Obtener configuración de asaltos
        Map<String, Object> assaults = getAssaultsConfig();

        status.put("assaults", assaults);
        return ResponseEntity.ok(status);
    }

    private Map<String, Object> getAssaultsConfig() {
        AssaultProperties assaultProperties = chaosMonkeySettings.getAssaultProperties();
        Map<String, Object> assaults = new HashMap<>();
        assaults.put("level", assaultProperties.getLevel());
        assaults.put("latencyActive", assaultProperties.isLatencyActive());
        assaults.put("latencyRangeStart", assaultProperties.getLatencyRangeStart());
        assaults.put("latencyRangeEnd", assaultProperties.getLatencyRangeEnd());
        assaults.put("exceptionsActive", assaultProperties.isExceptionsActive());
        
        // Manejo de excepciones
        if (assaultProperties.getException() != null) {
            assaults.put("exceptionType", assaultProperties.getException().getType());
            if (assaultProperties.getException().getArguments() != null && 
                !assaultProperties.getException().getArguments().isEmpty()) {
                assaults.put("exceptionMessage", 
                    assaultProperties.getException().getArguments().get(0).getValue());
            } else {
                assaults.put("exceptionMessage", "");
            }
        } else {
            assaults.put("exceptionType", "");
            assaults.put("exceptionMessage", "");
        }
        
        assaults.put("memoryActive", assaultProperties.isMemoryActive());
        assaults.put("memoryMillisecondsWaitNextIncrease",
            assaultProperties.getMemoryMillisecondsWaitNextIncrease());
        return assaults;
    }

    @PostMapping("/enable")
    @Operation(summary = "Habilitar Chaos Monkey")
    public ResponseEntity<Map<String, Object>> enable() {
        chaosMonkeySettings.getChaosMonkeyProperties().setEnabled(true);
        return getStatus();
    }

    @PostMapping("/disable")
    @Operation(summary = "Deshabilitar Chaos Monkey")
    public ResponseEntity<Map<String, Object>> disable() {
        chaosMonkeySettings.getChaosMonkeyProperties().setEnabled(false);
        return getStatus();
    }

    @GetMapping("/assaults")
    @Operation(summary = "Obtener configuración de asaltos actual")
    public ResponseEntity<Map<String, Object>> getAssaults() {
        Map<String, Object> response =getAssaultsConfig();
            
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assaults")
    @Operation(summary = "Actualizar configuración de asaltos")
    public ResponseEntity<Map<String, Object>> updateAssaults(@RequestBody Map<String, Object> config) {
        AssaultProperties properties = chaosMonkeySettings.getAssaultProperties();
        
        // Actualizar con nuevos valores
        if (config.containsKey("level")) {
            properties.setLevel(((Number) config.get("level")).intValue());
        }
        if (config.containsKey("latencyActive")) {
            properties.setLatencyActive((Boolean) config.get("latencyActive"));
        }
        if (config.containsKey("latencyRangeStart")) {
            properties.setLatencyRangeStart(((Number) config.get("latencyRangeStart")).intValue());
        }
        if (config.containsKey("latencyRangeEnd")) {
            properties.setLatencyRangeEnd(((Number) config.get("latencyRangeEnd")).intValue());
        }
        if (config.containsKey("exceptionsActive")) {
            properties.setExceptionsActive((Boolean) config.get("exceptionsActive"));
        }
        if (config.containsKey("exceptionType") || config.containsKey("exceptionMessage")) {
            String exceptionType = config.containsKey("exceptionType") ? 
                (String) config.get("exceptionType") : 
                (properties.getException() != null ? properties.getException().getType() : "");
                
            String exceptionMessage = config.containsKey("exceptionMessage") ? 
                (String) config.get("exceptionMessage") : "";
            
            // Crear una nueva configuración de excepción
            AssaultException exception = new AssaultException();
            exception.setType(exceptionType);
            
            if (!exceptionMessage.isEmpty()) {
                AssaultException.ExceptionArgument arg = new AssaultException.ExceptionArgument();
                arg.setValue(exceptionMessage);
                exception.setArguments(Collections.singletonList(arg));
            } else if (properties.getException() != null) {
                exception.setArguments(properties.getException().getArguments());
            }
            
            properties.setException(exception);
        }
        if (config.containsKey("memoryActive")) {
            properties.setMemoryActive((Boolean) config.get("memoryActive"));
        }
        if (config.containsKey("memoryMillisecondsWaitNextIncrease")) {
            properties.setMemoryMillisecondsWaitNextIncrease(
                ((Number) config.get("memoryMillisecondsWaitNextIncrease")).intValue()
            );
        }
        
        return getAssaults();
    }

    @PostMapping("/assaults/latency")
    @Operation(summary = "Configurar asaltos de latencia")
    public ResponseEntity<Map<String, Object>> configureLatencyAssaults(
            @RequestParam boolean active,
            @RequestParam(defaultValue = "1000") int from,
            @RequestParam(defaultValue = "3000") int to) {
        
        Map<String, Object> config = new HashMap<>();
        config.put("latencyActive", active);
        config.put("latencyRangeStart", from);
        config.put("latencyRangeEnd", to);
        
        return updateAssaults(config);
    }

    @PostMapping("/assaults/exceptions")
    @Operation(summary = "Configurar asaltos de excepciones")
    public ResponseEntity<Map<String, Object>> configureExceptionAssaults(
            @RequestParam boolean active,
            @RequestParam(defaultValue = "java.lang.RuntimeException") String exceptionClass,
            @RequestParam(defaultValue = "Chaos Monkey - This is a random exception") String message) {
        
        AssaultProperties properties = chaosMonkeySettings.getAssaultProperties();
        properties.setExceptionsActive(active);
        
        // Configurar la excepción
        AssaultException exception = new AssaultException();
        exception.setType(exceptionClass);
        
        if (!message.isEmpty()) {
            AssaultException.ExceptionArgument arg = new AssaultException.ExceptionArgument();
            arg.setValue(message);
            exception.setArguments(Collections.singletonList(arg));
        } else if (properties.getException() != null) {
            exception.setArguments(properties.getException().getArguments());
        }
        
        properties.setException(exception);
        
        return getAssaults();
    }

    @PostMapping("/assaults/memory")
    @Operation(summary = "Configurar asaltos de memoria")
    public ResponseEntity<Map<String, Object>> configureMemoryAssaults(
            @RequestParam boolean active,
            @RequestParam(defaultValue = "50") int memoryMegabytes,
            @RequestParam(defaultValue = "15000") long millisWaitNextMemoryKill) {
        
        Map<String, Object> config = new HashMap<>();
        config.put("memoryActive", active);
        config.put("memoryMillisecondsWaitNextIncrease", millisWaitNextMemoryKill);
        
        return updateAssaults(config);
    }

    @PostMapping("/watchers")
    @Operation(summary = "Configurar observadores")
    public ResponseEntity<Map<String, Object>> configureWatchers(
            @RequestParam(defaultValue = "true") boolean controller,
            @RequestParam(defaultValue = "true") boolean restController,
            @RequestParam(defaultValue = "true") boolean service,
            @RequestParam(defaultValue = "true") boolean repository,
            @RequestParam(defaultValue = "true") boolean component) {
        
        WatcherProperties watcherProperties = chaosMonkeySettings.getWatcherProperties();
        if (watcherProperties == null) {
            watcherProperties = new WatcherProperties();
        }
        
        watcherProperties.setController(controller);
        watcherProperties.setRestController(restController);
        watcherProperties.setService(service);
        watcherProperties.setRepository(repository);
        watcherProperties.setComponent(component);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Observadores actualizados");
        response.put("controller", watcherProperties.isController());
        response.put("restController", watcherProperties.isRestController());
        response.put("service", watcherProperties.isService());
        response.put("repository", watcherProperties.isRepository());
        response.put("component", watcherProperties.isComponent());
        
        return ResponseEntity.ok(response);
    }
}
