import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Configuración de la prueba
export const options = {
  stages: [
    { duration: '30s', target: 20 },  // Rampa de 0 a 20 usuarios en 30 segundos
    { duration: '1m', target: 20 },   // Mantener 20 usuarios por 1 minuto
    { duration: '10s', target: 50 },  // Aumentar a 50 usuarios en 10 segundos
    { duration: '1m', target: 50 },   // Mantener 50 usuarios por 1 minuto
    { duration: '10s', target: 0 },   // Reducir a 0 usuarios en 10 segundos
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% de las peticiones deben ser más rápidas que 500ms
    http_req_failed: ['rate<0.1'],    // Menos del 10% de peticiones pueden fallar
  },
};

// URL base de la API
const BASE_URL = 'http://localhost:8080';

// Métricas personalizadas
const errorRate = new Rate('errors');

// Datos de prueba
const testVehicles = [
  { marca: 'Toyota', modelo: 'Corolla', anio: 2020, color: 'Rojo', placa: 'ABC123', precio: 25000 },
  { marca: 'Honda', modelo: 'Civic', anio: 2021, color: 'Azul', placa: 'XYZ789', precio: 23000 },
  { marca: 'Ford', modelo: 'Mustang', anio: 2022, color: 'Negro', placa: 'MUS001', precio: 45000 }
];

// Función para obtener un vehículo aleatorio
function getRandomVehicle() {
  return testVehicles[Math.floor(Math.random() * testVehicles.length)];
}

export default function () {
  // Obtener lista de vehículos
  const listRes = http.get(`${BASE_URL}/api/vehiculos`);
  check(listRes, { 'Listar vehículos - status 200': (r) => r.status === 200 }) || errorRate.add(1);

  // Crear un nuevo vehículo
  const newVehicle = getRandomVehicle();
  const createRes = http.post(
    `${BASE_URL}/api/vehiculos`,
    JSON.stringify(newVehicle),
    { headers: { 'Content-Type': 'application/json' } }
  );
  
  const createSuccess = check(createRes, {
    'Crear vehículo - status 201': (r) => r.status === 201,
    'Crear vehículo - ID retornado': (r) => r.json().id !== undefined
  });
  
  if (!createSuccess) {
    errorRate.add(1);
    return;
  }
  
  const vehicleId = createRes.json().id;
  
  // Obtener el vehículo creado
  const getRes = http.get(`${BASE_URL}/api/vehiculos/${vehicleId}`);
  check(getRes, {
    'Obtener vehículo - status 200': (r) => r.status === 200,
    'Obtener vehículo - ID coincide': (r) => r.json().id === vehicleId
  }) || errorRate.add(1);
  
  // Actualizar el vehículo
  const updatedVehicle = { ...newVehicle, color: 'Gris' };
  const updateRes = http.put(
    `${BASE_URL}/api/vehiculos/${vehicleId}`,
    JSON.stringify(updatedVehicle),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(updateRes, {
    'Actualizar vehículo - status 200': (r) => r.status === 200,
    'Actualizar vehículo - color actualizado': (r) => r.json().color === 'Gris'
  }) || errorRate.add(1);
  
  // Eliminar el vehículo
  const deleteRes = http.del(`${BASE_URL}/api/vehiculos/${vehicleId}`);
  check(deleteRes, {
    'Eliminar vehículo - status 200': (r) => r.status === 200
  }) || errorRate.add(1);
  
  // Pequeña pausa entre iteraciones
  sleep(1);
}
