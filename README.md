# Vehículos API

API RESTful para la gestión de vehículos con capacidades de resiliencia y pruebas de caos controladas.

## 🚀 Características

- **CRUD completo** para la gestión de vehículos
- **Resiliencia** con Resilience4j (Circuit Breaker, Retry)
- **Pruebas de caos** con Chaos Monkey for Spring Boot
- **Monitoreo** con Spring Boot Actuator y Prometheus
- **Documentación** con Swagger/OpenAPI
- **Base de datos** H2 en memoria

## 🛠️ Requisitos

- Java 17+
- Maven 3.6+
- Spring Boot 3.5.3

## 🚀 Inicio rápido

1. Clona el repositorio:
   ```bash
   git clone [URL_DEL_REPOSITORIO]
   cd ResidenciaSpringboot
   ```

2. Construye la aplicación:
   ```bash
   mvn clean install
   ```

3. Ejecuta la aplicación:
   ```bash
   mvn spring-boot:run
   ```

4. Accede a la aplicación en:
   - API: http://localhost:8080/api/vehiculos
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Actuator: http://localhost:8080/actuator
   - Prometheus: http://localhost:8080/actuator/prometheus

## 🎯 Endpoints principales

### Vehículos
- `GET /api/vehiculos` - Obtener todos los vehículos
- `GET /api/vehiculos/{id}` - Obtener un vehículo por ID
- `POST /api/vehiculos` - Crear un nuevo vehículo
- `PUT /api/vehiculos/{id}` - Actualizar un vehículo existente
- `DELETE /api/vehiculos/{id}` - Eliminar un vehículo

### Chaos Monkey
- `GET /api/chaos-monkey/status` - Estado de Chaos Monkey
- `POST /api/chaos-monkey/enable` - Habilitar Chaos Monkey
- `POST /api/chaos-monkey/disable` - Deshabilitar Chaos Monkey
- `GET /api/chaos-monkey/assaults` - Obtener configuración de asaltos
- `POST /api/chaos-monkey/assaults` - Actualizar configuración de asaltos

## 🎭 Chaos Monkey

Chaos Monkey está configurado para inyectar fallos controlados en la aplicación. Por defecto, está deshabilitado y se puede habilitar mediante el perfil `chaos` o mediante los endpoints de la API.

### Configuración por defecto

- **Latencia**: 1000-3000ms
- **Excepciones**: Activadas (RuntimeException)
- **Memoria**: Consumo de memoria activado
- **Watchers**: Controladores, servicios y repositorios monitoreados

### Habilitar Chaos Monkey

1. **Usando perfiles** (en `application.properties`):
   ```properties
   spring.profiles.active=chaos
   chaos.monkey.enabled=true
   ```

2. **Usando la API** (cuando la aplicación está en ejecución):
   ```bash
   curl -X POST http://localhost:8080/api/chaos-monkey/enable
   ```

## 📊 Monitoreo

La aplicación expone métricas a través de Spring Boot Actuator y Prometheus:

- **Health**: `GET /actuator/health`
- **Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus**: `GET /actuator/prometheus`

## 🧪 Testing

Para ejecutar las pruebas unitarias:

```bash
mvn test
```

Para pruebas de carga con k6:

```bash
cd src/test/load-test
k6 run stress-test.js
```

## 🛡️ Seguridad

> **Nota:** En un entorno de producción, asegúrate de configurar la seguridad adecuadamente.

## 📄 Licencia

Este proyecto libre que puedes usar como base para tu propia aplicación.
