# Chaos Monkey for Spring Boot

Esta guía explica cómo usar Chaos Monkey para probar la resiliencia de la aplicación Vehículos API.

## ¿Qué es Chaos Monkey?

Chaos Monkey es una herramienta que ayuda a probar la resiliencia de las aplicaciones al inyectar fallos controlados en tiempo de ejecución. Esto ayuda a identificar puntos débiles antes de que afecten a los usuarios en producción.

## Configuración

La configuración básica se encuentra en `application.properties`:

```properties
# Habilitar/deshabilitar Chaos Monkey
chaos.monkey.enabled=true

# Tipos de componentes a monitorear
chaos.monkey.watcher.repository=true
chaos.monkey.watcher.rest-controller=true
chaos.monkey.watcher.service=true
chaos.monkey.watcher.component=true

# Nivel de agresividad (1-5)
chaos.monkey.assaults.level=3

# Configuración de latencia (ms)
chaos.monkey.assaults.latency-active=true
chaos.monkey.assaults.latency-range-start=1000
chaos.monkey.assaults.latency-range-end=3000

# Configuración de excepciones
chaos.monkey.assaults.exceptions-active=true
chaos.monkey.assaults.exception.type=java.lang.RuntimeException

# Configuración de consumo de memoria (MB)
chaos.monkey.assaults.memory-active=true
chaos.monkey.assaults.memory-megabytes=50
```

## Endpoints de la API

### Controlador de Chaos Monkey

- `GET /api/chaos-monkey/status` - Estado actual
- `POST /api/chaos-monkey/enable` - Habilitar Chaos Monkey
- `POST /api/chaos-monkey/disable` - Deshabilitar Chaos Monkey
- `GET /api/chaos-monkey/assaults` - Obtener configuración actual
- `POST /api/chaos-monkey/assaults` - Actualizar configuración
- `POST /api/chaos-monkey/assaults/latency` - Configurar latencia
- `POST /api/chaos-monkey/assaults/exceptions` - Configurar excepciones
- `POST /api/chaos-monkey/assaults/memory` - Configurar consumo de memoria
- `POST /api/chaos-monkey/watchers` - Configurar observadores

### Endpoints de Actuator

- `GET /actuator/chaosmonkey` - Estado y configuración
- `POST /actuator/chaosmonkey/enable` - Habilitar
- `POST /actuator/chaosmonkey/disable` - Deshabilitar

## Uso Básico

1. **Habilitar Chaos Monkey**:
   ```bash
   curl -X POST http://localhost:8080/api/chaos-monkey/enable
   ```

2. **Configurar latencia aleatoria** (entre 1-3 segundos):
   ```bash
   curl -X POST "http://localhost:8080/api/chaos-monkey/assaults/latency?active=true&from=1000&to=3000"
   ```

3. **Configurar excepciones aleatorias**:
   ```bash
   curl -X POST "http://localhost:8080/api/chaos-monkey/assaults/exceptions?active=true&exceptionClass=java.lang.RuntimeException&message=Chaos+Monkey+Error"
   ```

4. **Ver estado actual**:
   ```bash
   curl http://localhost:8080/api/chaos-monkey/status
   ```

## Perfiles

- **default**: Chaos Monkey deshabilitado por defecto
- **chaos**: Chaos Monkey habilitado automáticamente

Para ejecutar con el perfil de caos:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=chaos
```

## Integración con Pruebas

Puedes usar los endpoints para configurar Chaos Monkey durante las pruebas de carga:

```javascript
// Ejemplo en k6
import http from 'k6/http';

export default function () {
  // Habilitar Chaos Monkey antes de las pruebas
  http.post('http://localhost:8080/api/chaos-monkey/enable');
  
  // Configurar latencia aleatoria
  http.post('http://localhost:8080/api/chaos-monkey/assaults/latency?active=true&from=500&to=2000');
  
  // Ejecutar pruebas...
}
```

## Monitoreo

Chaos Monkey expone métricas a través de Micrometer que puedes visualizar en:
- Prometheus: `/actuator/prometheus`
- Health: `/actuator/health`
- Info: `/actuator/info`

## Mejores Prácticas

1. **No usar en producción** sin pruebas exhaustivas
2. **Empezar con bajo impacto** y aumentar gradualmente
3. **Monitorear** el impacto en el rendimiento
4. **Documentar** los escenarios de prueba
5. **Automatizar** la configuración para entornos específicos

## Solución de Problemas

- Si Chaos Monkey no se activa, verifica que el perfil esté configurado correctamente
- Revisa los logs de la aplicación para ver los fallos inyectados
- Asegúrate de que los endpoints de Actuator estén habilitados

## Referencias

- [Documentación oficial de Chaos Monkey](https://codecentric.github.io/chaos-monkey-spring-boot/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
