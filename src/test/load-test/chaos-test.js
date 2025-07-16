import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// Configuración de la prueba de caos
export const options = {
  scenarios: {
    constant_load: {
      executor: 'constant-vus',
      vus: 10,
      duration: '5m',
      gracefulStop: '30s',
    },
    spike: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: 50 },  // Rampa hasta 50 usuarios
        { duration: '1m', target: 50 },  // Mantener 50 usuarios
        { duration: '1m', target: 0 },   // Reducir a 0 usuarios
      ],
      gracefulStop: '30s',
      startTime: '2m',  // Comenzar después de 2 minutos
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<1000'],
    http_req_failed: ['rate<0.3'],  // Umbral más alto para pruebas de caos
  },
};

const BASE_URL = 'http://localhost:8080';
const errorRate = new Rate('errors');
const chaosRate = 0.2; // 20% de probabilidad de inyectar caos

// Función para inyectar caos
function injectChaos() {
  if (Math.random() < chaosRate) {
    // 1. Cambiar estado de salud a no listo
    if (Math.random() > 0.5) {
      http.post(
        `${BASE_URL}/api/health/ready/false`,
        null,
        { tags: { chaos: 'readiness_false' } }
      );
      
      // Volver a estado listo después de un tiempo
      sleep(randomIntBetween(5, 15));
      http.post(
        `${BASE_URL}/api/health/ready/true`,
        null,
        { tags: { chaos: 'readiness_true' } }
      );
    } 
    // 2. Cambiar estado de liveness
    else {
      http.post(
        `${BASE_URL}/api/health/live/false`,
        null,
        { tags: { chaos: 'liveness_false' } }
      );
      
      // Volver a estado vivo después de un tiempo
      sleep(randomIntBetween(2, 5));
      http.post(
        `${BASE_URL}/api/health/live/true`,
        null,
        { tags: { chaos: 'liveness_true' } }
      );
    }
  }
}

export default function () {
  // Inyectar caos en algunas iteraciones
  if (Math.random() < 0.3) { // 30% de probabilidad de inyectar caos en esta iteración
    injectChaos();
  }
  
  // Operación aleatoria
  const operation = Math.floor(Math.random() * 3); // Solo operaciones de lectura para pruebas de caos
  
  try {
    switch (operation) {
      case 0: // Listar vehículos
        const listRes = http.get(`${BASE_URL}/api/vehiculos`);
        check(listRes, { 'Listar vehículos - status 200': (r) => r.status === 200 }) || errorRate.add(1);
        break;
        
      case 1: // Verificar estado de salud
        const healthRes = http.get(`${BASE_URL}/actuator/health`);
        check(healthRes, { 'Health check - status 200': (r) => r.status === 200 }) || errorRate.add(1);
        break;
        
      case 2: // Verificar métricas
        const metricsRes = http.get(`${BASE_URL}/actuator/metrics`);
        check(metricsRes, { 'Métricas - status 200': (r) => r.status === 200 }) || errorRate.add(1);
        break;
    }
  } catch (error) {
    errorRate.add(1);
    console.error('Error en la prueba de caos:', error);
  }
  
  // Pequeña pausa aleatoria entre operaciones
  sleep(randomIntBetween(1, 3));
}
