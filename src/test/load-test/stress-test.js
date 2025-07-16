import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { SharedArray } from 'k6/data';

// Configuración de la prueba de estrés
export const options = {
  stages: [
    { duration: '1m', target: 100 },  // Rápido incremento a 100 usuarios
    { duration: '3m', target: 100 },  // Mantener 100 usuarios por 3 minutos
    { duration: '30s', target: 200 }, // Incremento a 200 usuarios
    { duration: '2m', target: 200 },  // Mantener 200 usuarios por 2 minutos
    { duration: '30s', target: 0 },   // Reducción a 0 usuarios
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'], // Umbral más relajado para estrés
    http_req_failed: ['rate<0.2'],     // Hasta 20% de errores permitidos
  },
};

const BASE_URL = 'http://localhost:8080';
const errorRate = new Rate('errors');

// Datos de prueba más extensos
const testVehicles = new SharedArray('vehicles', function() {
  const vehicles = [];
  const brands = ['Toyota', 'Honda', 'Ford', 'Chevrolet', 'Nissan', 'BMW', 'Mercedes', 'Audi', 'Volkswagen', 'Hyundai'];
  const models = ['Corolla', 'Civic', 'Mustang', 'Camaro', 'Sentra', 'Serie 3', 'Clase C', 'A4', 'Golf', 'Elantra'];
  const colors = ['Rojo', 'Azul', 'Negro', 'Blanco', 'Gris', 'Plata'];
  
  for (let i = 0; i < 50; i++) {
    vehicles.push({
      marca: brands[Math.floor(Math.random() * brands.length)],
      modelo: `${models[Math.floor(Math.random() * models.length)]} ${Math.floor(Math.random() * 5) + 1}.${Math.floor(Math.random() * 10)}`,
      anio: 2020 + Math.floor(Math.random() * 5),
      color: colors[Math.floor(Math.random() * colors.length)],
      placa: `ABC${Math.floor(100 + Math.random() * 900)}`,
      precio: 15000 + Math.floor(Math.random() * 50000)
    });
  }
  return vehicles;
});

// Función auxiliar para generar una placa única
function generateUniquePlate() {
  const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  const numbers = '0123456789';
  let plate = '';
  
  for (let i = 0; i < 3; i++) {
    plate += letters.charAt(Math.floor(Math.random() * letters.length));
  }
  
  for (let i = 0; i < 3; i++) {
    plate += numbers.charAt(Math.floor(Math.random() * numbers.length));
  }
  
  return plate;
}

export default function () {
  // Operación aleatoria: 0=listar, 1=crear, 2=leer, 3=actualizar, 4=eliminar
  const operation = Math.floor(Math.random() * 5);
  
  try {
    switch (operation) {
      case 0: // Listar vehículos
        const listRes = http.get(`${BASE_URL}/api/vehiculos`);
        check(listRes, { 'Listar vehículos - status 200': (r) => r.status === 200 }) || errorRate.add(1);
        break;
        
      case 1: // Crear vehículo
        const newVehicle = {
          ...testVehicles[Math.floor(Math.random() * testVehicles.length)],
          placa: generateUniquePlate() // Asegurar placa única
        };
        
        const createRes = http.post(
          `${BASE_URL}/api/vehiculos`,
          JSON.stringify(newVehicle),
          { headers: { 'Content-Type': 'application/json' } }
        );
        
        const createSuccess = check(createRes, {
          'Crear vehículo - status 201': (r) => r.status === 201,
          'Crear vehículo - ID retornado': (r) => r.json().id !== undefined
        });
        
        if (createSuccess && createRes.json().id) {
          // Si la creación fue exitosa, guardar el ID para operaciones futuras
          __VU.vars.createdVehicleId = createRes.json().id;
        } else {
          errorRate.add(1);
        }
        break;
        
      case 2: // Leer vehículo
        if (__VU.vars.createdVehicleId) {
          const getRes = http.get(`${BASE_URL}/api/vehiculos/${__VU.vars.createdVehicleId}`);
          check(getRes, {
            'Obtener vehículo - status 200': (r) => r.status === 200,
            'Obtener vehículo - ID coincide': (r) => r.json().id === __VU.vars.createdVehicleId
          }) || errorRate.add(1);
        }
        break;
        
      case 3: // Actualizar vehículo
        if (__VU.vars.createdVehicleId) {
          const updatedVehicle = {
            ...testVehicles[Math.floor(Math.random() * testVehicles.length)],
            placa: generateUniquePlate()
          };
          
          const updateRes = http.put(
            `${BASE_URL}/api/vehiculos/${__VU.vars.createdVehicleId}`,
            JSON.stringify(updatedVehicle),
            { headers: { 'Content-Type': 'application/json' } }
          );
          
          check(updateRes, {
            'Actualizar vehículo - status 200': (r) => r.status === 200
          }) || errorRate.add(1);
        }
        break;
        
      case 4: // Eliminar vehículo
        if (__VU.vars.createdVehicleId) {
          const deleteRes = http.del(`${BASE_URL}/api/vehiculos/${__VU.vars.createdVehicleId}`);
          check(deleteRes, {
            'Eliminar vehículo - status 200': (r) => r.status === 200
          }) || errorRate.add(1);
          
          // Limpiar el ID después de eliminar
          if (deleteRes.status === 200) {
            delete __VU.vars.createdVehicleId;
          }
        }
        break;
    }
  } catch (error) {
    errorRate.add(1);
    console.error('Error en la prueba de estrés:', error);
  }
  
  // Pequeña pausa aleatoria entre operaciones
  sleep(Math.random() * 2);
}
