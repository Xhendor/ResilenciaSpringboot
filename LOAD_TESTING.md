# Pruebas de Carga y Estrés con k6

Este directorio contiene scripts para probar el rendimiento y la resistencia de la API de Vehículos utilizando [k6](https://k6.io/).

## Requisitos Previos

1. Instalar [k6](https://k6.io/docs/getting-started/installation/)
2. Tener la aplicación Spring Boot en ejecución en `http://localhost:8080`

## Scripts de Prueba

### 1. Prueba de Carga Básica (`basic-load-test.js`)

Prueba de carga gradual que simula un patrón de tráfico normal.

**Características:**
- Rampa gradual de usuarios (0 a 20 en 30s)
- Picos controlados (hasta 50 usuarios)
- Operaciones CRUD balanceadas

**Ejecutar:**
```bash
k6 run src/test/load-test/basic-load-test.js
```

### 2. Prueba de Estrés (`stress-test.js`)

Prueba de estrés con cargas pesadas y patrones de tráfico variables.

**Características:**
- Hasta 200 usuarios concurrentes
- Generación dinámica de datos de prueba
- Operaciones aleatorias CRUD

**Ejecutar:**
```bash
k6 run src/test/load-test/stress-test.js
```

### 3. Prueba de Caos (`chaos-test.js`)

Prueba de resistencia inyectando fallos controlados.

**Características:**
- Inyección aleatoria de fallos (20% de probabilidad)
- Cambios en estados de salud (liveness/readiness)
- Patrones de tráfico variables

**Ejecutar:**
```bash
k6 run src/test/load-test/chaos-test.js
```

## Configuración de Umbrales

Cada script incluye umbrales configurables para marcar la prueba como fallida:

- `http_req_duration`: Tiempo máximo de respuesta (p95 < 500ms-1000ms)
- `http_req_failed`: Tasa de error máxima permitida (10-30%)

## Visualización de Resultados

Por defecto, k6 muestra métricas en la consola. Para una mejor visualización:

1. **Exportar a CSV:**
   ```bash
   k6 run --out csv=test_results.csv src/test/load-test/basic-load-test.js
   ```

2. **Usar k6 Cloud (requiere cuenta):**
   ```bash
   k6 cloud src/test/load-test/basic-load-test.js
   ```

3. **Usar Grafana + InfluxDB:**
   ```bash
   k6 run --out influxdb=http://localhost:8086/k6 src/test/load-test/basic-load-test.js
   ```

## Análisis de Resultados

Las métricas clave a monitorear son:

- **Tasa de solicitudes por segundo (RPS)**
- **Tiempo de respuesta** (p95, p99)
- **Tasa de error**
- **Uso de recursos** (CPU, memoria)

## Recomendaciones

1. Ejecutar pruebas en un entorno aislado
2. Monitorear los recursos del servidor durante las pruebas
3. Ajustar los umbrales según los requisitos de tu aplicación
4. Revisar los logs de la aplicación después de las pruebas

## Solución de Problemas

Si encuentras errores:
1. Verifica que la aplicación esté en ejecución
2. Revisa los logs de la aplicación
3. Ajusta los timeouts si es necesario
4. Verifica la configuración de la base de datos

## Personalización

Puedes modificar los scripts para:
- Cambiar los volúmenes de carga
- Ajustar los tiempos de espera
- Agregar más casos de prueba
- Modificar los datos de prueba
