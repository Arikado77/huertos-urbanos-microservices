# Huertos urbanos: microservicios Parcel y Sensor

Solución de examen compuesta por dos aplicaciones Spring Boot independientes. Parcel tiene el contrato solicitado para parcelas. Sensor implementa una **propuesta pendiente de aprobación del contrato oficial** para registrar lecturas IoT.

## Arquitectura

```text
Cliente
  ├── HTTP :8081 ── parcel-service ── PostgreSQL / huertos_db
  └── HTTP :8082 ── sensor-service ── PostgreSQL / sensor_db
                         │
                         └── HTTP GET parcel-service:8081/api/v1/parcels/{id}
```

Los servicios tienen modelos, repositorios y esquemas de base de datos independientes. Sensor guarda `parcelId` como referencia lógica, no como clave foránea entre bases, y valida su existencia consultando Parcel. Las consultas de persistencia usan Spring Data JPA, no concatenación SQL.

## Tecnologías

- Java 17, Spring Boot 4.1.1, Spring MVC y Jakarta Validation.
- Spring Data JPA con PostgreSQL 18.
- Docker Compose, redes privadas, volumen persistente y comprobaciones de salud.

## Requisitos y configuración local

- JDK 17.
- PostgreSQL local en `localhost:5432`, usuario `postgres`; la contraseña está vacía en este entorno.
- La base existente `huertos_db` se conserva. Parcel sigue usando esa base, `ddl-auto=update` solo para desarrollo local y puerto HTTP `8081`.
- Sensor usa otra base, `sensor_db`, y puerto `8082`. No modifica `huertos_db`.

Desde la carpeta `EXAMEN`, inicia cada aplicación en una terminal separada:

```powershell
.\parcel-service\mvnw.cmd -f .\parcel-service\pom.xml spring-boot:run
.\parcel-service\mvnw.cmd -f .\sensor-service\pom.xml spring-boot:run
```

En modo local, Sensor consulta Parcel en `http://localhost:8081`. Puedes ajustar `PARCEL_SERVICE_URL` si se ejecuta en otro host.

### Crear `sensor_db` local si aún no existe

El proceso es aditivo: comprueba antes de crear la base, y nunca borra ni recrea `huertos_db`.

```powershell
$exists = psql -h localhost -p 5432 -U postgres -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'sensor_db'"
if ($exists.Trim() -ne "1") {
  createdb -h localhost -p 5432 -U postgres sensor_db
}
psql -h localhost -p 5432 -U postgres -d huertos_db -c "\dt"
psql -h localhost -p 5432 -U postgres -d sensor_db -c "\dt"
```

Al usar una contraseña local distinta de la vacía, establece `SPRING_DATASOURCE_PASSWORD` para la aplicación correspondiente. Hibernate actualiza las tablas durante desarrollo; en producción se recomienda usar migraciones versionadas y cambiar `ddl-auto`.

## Docker Compose

El Compose canónico está en esta carpeta. Usa un PostgreSQL 18 **propio del Compose**, separado del servidor local, expone el puerto `5433`, persiste datos en `postgres_data` y crea `sensor_db` al inicializar un volumen nuevo. Ambos contenedores se conectan a `postgres:5432`, no a `localhost`. El puerto `8081` es Parcel y `8082` Sensor.

En PowerShell:

```powershell
$env:POSTGRES_PASSWORD = "elige-una-clave-local-para-Docker"
docker compose up --build
```

Si un volumen Docker preexistente fue creado antes de añadir `sensor_db`, el script de inicialización no se repite. Verifica la base y créala solo si no existe:

```powershell
docker compose exec postgres psql -U postgres -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'sensor_db'"
docker compose exec postgres createdb -U postgres sensor_db
```

El segundo comando es solo para el caso en que la consulta no devuelva `1`. No se incluyen contraseñas reales en archivos de configuración.

Consultar ambas bases dentro de Compose:

```powershell
docker compose exec postgres psql -U postgres -d huertos_db -c "\dt"
docker compose exec postgres psql -U postgres -d sensor_db -c "\dt"
```

Los healthchecks de PostgreSQL, Parcel y Sensor definen la secuencia de inicio. Sensor depende de PostgreSQL y de que Parcel responda saludable. El archivo [parcel-service/docker-compose.yml](parcel-service/docker-compose.yml) incluye este Compose para compatibilidad al ejecutarlo desde la carpeta Parcel.

## API Parcel

### Crear parcela — `POST http://localhost:8081/api/v1/parcels`

```powershell
curl.exe -i -X POST http://localhost:8081/api/v1/parcels `
  -H "Content-Type: application/json" `
  --data-raw '{"name":"Parcela Norte","location":{"latitude":19.4326,"longitude":-99.1332},"soilType":"limoso","ownerOrLead":"Equipo del huerto"}'
```

Devuelve `201 Created`. El ejemplo de respuesta contiene el identificador y fecha que asigna el servicio:

```json
{
  "id": 1,
  "name": "Parcela Norte",
  "location": {"latitude": 19.4326, "longitude": -99.1332},
  "soilType": "limoso",
  "ownerOrLead": "Equipo del huerto",
  "createdAt": "2026-10-14T20:00:00Z"
}
```

### Listar y consultar por ID

```powershell
curl.exe -i http://localhost:8081/api/v1/parcels
curl.exe -i http://localhost:8081/api/v1/parcels/1
```

Los dos endpoints devuelven `200 OK`; un ID inexistente devuelve `404 Not Found`. La ubicación se expresa como latitud `[-90, 90]` y longitud `[-180, 180]`. Tipos de suelo admitidos: `arcilloso`, `limoso`, `arenoso`. Nombre y responsable se recortan y normalizan a Unicode NFC; se rechazan texto en blanco, demasiado largo o con caracteres de control/formato.

## API Sensor — propuesta no oficial

Los rangos siguientes son supuestos de validación para esta propuesta, no un contrato oficial:

- Humedad de suelo: porcentaje `0–100`.
- Temperatura: grados Celsius `-50–80`.
- pH: `0–14`.
- Nitrógeno, fósforo y potasio: valor numérico `0–100000`; unidad pendiente de confirmar.
- Calidad del aire: índice `0–5000`; escala pendiente de confirmar.
- `recordedAt` lo genera el servicio al persistir y no se acepta del cliente.

### Crear lectura — `POST http://localhost:8082/api/v1/sensors/readings`

```powershell
curl.exe -i -X POST http://localhost:8082/api/v1/sensors/readings `
  -H "Content-Type: application/json" `
  --data-raw '{"parcelId":1,"soilMoisture":42.5,"temperature":21.5,"ph":6.5,"nitrogen":12,"phosphorus":8,"potassium":10,"airQuality":40}'
```

La parcela se verifica mediante `GET` a Parcel antes del alta. La respuesta normal es `201 Created`; si la parcela no existe devuelve `404`, y si Parcel no responde o presenta un error inesperado devuelve `503 Service Unavailable`.

### Listar, consultar y filtrar por parcela

```powershell
curl.exe -i http://localhost:8082/api/v1/sensors/readings
curl.exe -i http://localhost:8082/api/v1/sensors/readings/15
curl.exe -i http://localhost:8082/api/v1/sensors/readings/parcel/1
```

## Ejemplos de entradas inválidas y errores

Nombre vacío o coordenadas fuera de rango en Parcel:

```powershell
curl.exe -i -X POST http://localhost:8081/api/v1/parcels `
  -H "Content-Type: application/json" `
  --data-raw '{"name":" ","location":{"latitude":91,"longitude":-99.1332},"soilType":"limoso","ownerOrLead":"Equipo"}'
```

Tipo de suelo no permitido o JSON malformado también responden `400 Bad Request`:

```powershell
curl.exe -i -X POST http://localhost:8081/api/v1/parcels -H "Content-Type: application/json" --data-raw '{"name":"Parcela","location":{"latitude":19,"longitude":-99},"soilType":"turboso","ownerOrLead":"Equipo"}'
curl.exe -i -X POST http://localhost:8081/api/v1/parcels -H "Content-Type: application/json" --data-raw '{'
```

pH/humedad fuera de rango o campos obligatorios ausentes en Sensor:

```powershell
curl.exe -i -X POST http://localhost:8082/api/v1/sensors/readings `
  -H "Content-Type: application/json" `
  --data-raw '{"parcelId":1,"soilMoisture":101,"temperature":21.5,"ph":15,"nitrogen":12,"phosphorus":8,"potassium":10,"airQuality":40}'
```

Formato uniforme de error:

```json
{
  "timestamp": "2026-10-14T20:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "La solicitud contiene campos inválidos",
  "path": "/api/v1/sensors/readings",
  "details": {
    "ph": "debe ser menor o igual que 14.0",
    "soilMoisture": "debe ser menor o igual que 100.0"
  }
}
```

Las pruebas comprueban los errores de validación, JSON inválido, IDs ausentes y fallos de comunicación entre servicios.

## Pruebas y compilación

Desde `EXAMEN`, el wrapper Maven existente puede ejecutar ambos proyectos independientemente:

```powershell
.\parcel-service\mvnw.cmd -f .\parcel-service\pom.xml test
.\parcel-service\mvnw.cmd -f .\sensor-service\pom.xml test
.\parcel-service\mvnw.cmd -f .\parcel-service\pom.xml package
.\parcel-service\mvnw.cmd -f .\sensor-service\pom.xml package
```

Evidencia durante esta entrega:

- `parcel-service`: **9 pruebas aprobadas**, 0 fallidas, incluyendo contexto Spring con conexión a PostgreSQL 18 local y pruebas MVC.
- `sensor-service`: **18 pruebas aprobadas**, 0 fallidas; pruebas MVC y de servicio incluyen rangos, JSON malformado, recursos inexistentes y cliente HTTP simulado para respuestas correctas, 404, 500 y conexión rechazada.
- Se confirmó `BUILD SUCCESS` en ambos `mvn test`. Los empaquetados se ejecutan después de las modificaciones finales.

## Supuestos y verificaciones pendientes

- Sensor es una propuesta porque no se entregó contrato oficial: quedan por confirmar nombres de campos, unidades y rangos físicos, escala de calidad del aire, autenticación, frecuencia, retención y política de reintentos.
- La verificación de parcela de Sensor es síncrona y usa HTTP; no hay FK entre bases ni circuit breaker.
- Docker Compose no se ha podido ejecutar ni validar en este entorno si no está instalado Docker CLI. La ejecución real de imágenes, healthchecks, red y volumen debe verificarse en una máquina con Docker Compose.
- Los tests MVC usan servicios simulados; el cliente HTTP se valida contra un servidor local simulado. Solo el test de contexto Parcel usa la PostgreSQL local ya existente.
