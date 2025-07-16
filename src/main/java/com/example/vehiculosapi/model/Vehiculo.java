package com.example.vehiculosapi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "vehiculos")
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String marca;
    
    @Column(nullable = false)
    private String modelo;
    
    @Column(nullable = false)
    private Integer anio;
    
    @Column(nullable = false)
    private String color;
    
    @Column(unique = true, nullable = false)
    private String placa;
    
    private Double precio;
}
