package com.example.vehiculosapi.service;

import com.example.vehiculosapi.model.Vehiculo;
import com.example.vehiculosapi.repository.VehiculoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    public List<Vehiculo> obtenerTodos() {
        return vehiculoRepository.findAll();
    }

    public Vehiculo obtenerPorId(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado con ID: " + id));
    }

    public Vehiculo crear(Vehiculo vehiculo) {
        if (vehiculoRepository.existsByPlaca(vehiculo.getPlaca())) {
            throw new IllegalArgumentException("Ya existe un vehículo con la placa: " + vehiculo.getPlaca());
        }
        return vehiculoRepository.save(vehiculo);
    }

    public Vehiculo actualizar(Long id, Vehiculo vehiculoActualizado) {
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
    }

    public void eliminar(Long id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new EntityNotFoundException("Vehículo no encontrado con ID: " + id);
        }
        vehiculoRepository.deleteById(id);
    }
}
