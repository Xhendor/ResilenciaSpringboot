package com.example.vehiculosapi.controller;

import com.example.vehiculosapi.model.Vehiculo;
import com.example.vehiculosapi.service.VehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@Tag(name = "Vehículos", description = "API para la gestión de vehículos")
public class VehiculoController {

    @Autowired
    private VehiculoService vehiculoService;

    @GetMapping
    @Operation(summary = "Obtener todos los vehículos")
    public ResponseEntity<List<Vehiculo>> obtenerTodos() {
        return ResponseEntity.ok(vehiculoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un vehículo por ID")
    public ResponseEntity<Vehiculo> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo vehículo")
    public ResponseEntity<Vehiculo> crear(@RequestBody Vehiculo vehiculo) {
        return ResponseEntity.ok(vehiculoService.crear(vehiculo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un vehículo existente")
    public ResponseEntity<Vehiculo> actualizar(
            @PathVariable Long id, 
            @RequestBody Vehiculo vehiculo) {
        return ResponseEntity.ok(vehiculoService.actualizar(id, vehiculo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un vehículo")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
