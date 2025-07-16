package com.example.vehiculosapi.service;

import com.example.vehiculosapi.model.Vehiculo;
import com.example.vehiculosapi.repository.VehiculoRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.vehiculosapi.config.MetricsConfig;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class VehiculoService {

    private static final Logger logger = LoggerFactory.getLogger(VehiculoService.class);
    private static final String VEHICULO_SERVICE = "vehiculoService";

    private final VehiculoRepository vehiculoRepository;
    private final MetricsConfig metricsConfig;
    private final MeterRegistry meterRegistry;

    @Autowired
    public VehiculoService(VehiculoRepository vehiculoRepository, 
                          MetricsConfig metricsConfig,
                          MeterRegistry meterRegistry) {
        this.vehiculoRepository = vehiculoRepository;
        this.metricsConfig = metricsConfig;
        this.meterRegistry = meterRegistry;
    }
    
    // Simulador de fallos para pruebas
    private boolean simularFallo = false;

    @CircuitBreaker(name = VEHICULO_SERVICE, fallbackMethod = "obtenerTodosFallback")
    @Retry(name = VEHICULO_SERVICE, fallbackMethod = "obtenerTodosFallback")
    public List<Vehiculo> obtenerTodos() {
        return metricsConfig.recordOperationTime(() -> {
            simularErrorAleatorio();
            meterRegistry.counter("vehiculo.operacion", "tipo", "consulta").increment();
            return vehiculoRepository.findAll();
        }, "obtenerTodos");
    }

    @CircuitBreaker(name = VEHICULO_SERVICE, fallbackMethod = "obtenerPorIdFallback")
    @Retry(name = VEHICULO_SERVICE, fallbackMethod = "obtenerPorIdFallback")
    public Vehiculo obtenerPorId(Long id) {
        return metricsConfig.recordOperationTime(() -> {
            simularErrorAleatorio();
            meterRegistry.counter("vehiculo.operacion", "tipo", "consulta_por_id").increment();
            return vehiculoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado con ID: " + id));
        }, "obtenerPorId");
    }

    @CircuitBreaker(name = VEHICULO_SERVICE, fallbackMethod = "crearFallback")
    @Retry(name = VEHICULO_SERVICE, fallbackMethod = "crearFallback")
    public Vehiculo crear(Vehiculo vehiculo) {
        return metricsConfig.recordOperationTime(() -> {
            simularErrorAleatorio();
            meterRegistry.counter("vehiculo.operacion", "tipo", "crear").increment();
            if (vehiculoRepository.existsByPlaca(vehiculo.getPlaca())) {
                throw new IllegalArgumentException("Ya existe un vehículo con la placa: " + vehiculo.getPlaca());
            }
            return vehiculoRepository.save(vehiculo);
        }, "crear");
    }

    @CircuitBreaker(name = VEHICULO_SERVICE, fallbackMethod = "actualizarFallback")
    @Retry(name = VEHICULO_SERVICE, fallbackMethod = "actualizarFallback")
    public Vehiculo actualizar(Long id, Vehiculo vehiculoActualizado) {
        return metricsConfig.recordOperationTime(() -> {
            simularErrorAleatorio();
            meterRegistry.counter("vehiculo.operacion", "tipo", "actualizar").increment();
            return vehiculoRepository.findById(id)
                    .map(vehiculo -> {
                        if (!vehiculo.getPlaca().equals(vehiculoActualizado.getPlaca()) && 
                            vehiculoRepository.existsByPlaca(vehiculoActualizado.getPlaca())) {
                            throw new IllegalArgumentException("Ya existe un vehículo con la placa: " + vehiculoActualizado.getPlaca());
                        }
                        vehiculo.setMarca(vehiculoActualizado.getMarca());
                        vehiculo.setModelo(vehiculoActualizado.getModelo());
                        vehiculo.setAnio(vehiculoActualizado.getAnio());
                        vehiculo.setColor(vehiculoActualizado.getColor());
                        vehiculo.setPlaca(vehiculoActualizado.getPlaca());
                        vehiculo.setPrecio(vehiculoActualizado.getPrecio());
                        return vehiculoRepository.save(vehiculo);
                    })
                    .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado con ID: " + id));
        }, "actualizar");
    }

    @CircuitBreaker(name = VEHICULO_SERVICE, fallbackMethod = "eliminarFallback")
    @Retry(name = VEHICULO_SERVICE, fallbackMethod = "eliminarFallback")
    public void eliminar(Long id) {
        metricsConfig.recordOperationTime(() -> {
            simularErrorAleatorio();
            meterRegistry.counter("vehiculo.operacion", "tipo", "eliminar").increment();
            if (!vehiculoRepository.existsById(id)) {
                throw new EntityNotFoundException("Vehículo no encontrado con ID: " + id);
            }
            vehiculoRepository.deleteById(id);
            return null;
        }, "eliminar");
    }

    // Métodos de fallback
    public List<Vehiculo> obtenerTodosFallback(Exception e) {
        logger.warn("Fallback para obtenerTodos() - Retornando lista vacía", e);
        return Collections.emptyList();
    }

    public Vehiculo obtenerPorIdFallback(Long id, Exception e) {
        logger.warn("Fallback para obtenerPorId({}) - Retornando vehículo por defecto", id, e);
        return new Vehiculo();
    }

    public Vehiculo crearFallback(Vehiculo vehiculo, Exception e) {
        logger.warn("Fallback para crear() - No se pudo crear el vehículo: {}", vehiculo, e);
        throw new RuntimeException("No se pudo crear el vehículo. Por favor, intente más tarde.", e);
    }

    public Vehiculo actualizarFallback(Long id, Vehiculo vehiculo, Exception e) {
        logger.warn("Fallback para actualizar() - No se pudo actualizar el vehículo con ID: {}", id, e);
        throw new RuntimeException("No se pudo actualizar el vehículo. Por favor, intente más tarde.", e);
    }

    public void eliminarFallback(Long id, Exception e) {
        logger.warn("Fallback para eliminar() - No se pudo eliminar el vehículo con ID: {}", id, e);
        throw new RuntimeException("No se pudo eliminar el vehículo. Por favor, intente más tarde.", e);
    }

    // Método auxiliar para simular errores (solo para pruebas)
    private void simularErrorAleatorio() {
        if (simularFallo && Math.random() > 0.7) {
            logger.warn("Simulando error en el servicio de vehículos");
            throw new RuntimeException("Error simulado en el servicio de vehículos");
        }
    }

    // Método para activar/desactivar la simulación de fallos (solo para pruebas)
    public void setSimularFallo(boolean simularFallo) {
        this.simularFallo = simularFallo;
    }
}
