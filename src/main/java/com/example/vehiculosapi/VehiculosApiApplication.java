package com.example.vehiculosapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "API de Vehículos",
        version = "1.0",
        description = "API para gestión de vehículos"
    )
)
public class VehiculosApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(VehiculosApiApplication.class, args);
    }
}
