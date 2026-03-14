# Proyecto: Procesamiento de Transacciones Bancarias con RabbitMQ y Java

## Descripción general

Este proyecto implementa un sistema distribuido en **Java + Maven** utilizando **RabbitMQ** bajo el patrón **Producer–Consumer**.

El sistema obtiene un lote de transacciones bancarias desde una API externa, distribuye cada transacción a una cola según el **banco destino** y posteriormente consume dichas colas para enviar cada transacción a un endpoint `POST`, agregando **nombre**, **carnet** y **correo** antes de almacenarla.

---

## Arquitectura general

```text
API GET /transacciones
        │
        ▼
Producer (Java + Maven)
        │
        ▼
RabbitMQ
(cola por banco)
        │
        ▼
Consumer (Java + Maven)
        │
        ▼
API POST /guardarTransacciones
```

---

## Objetivos del proyecto

Desarrollar un sistema que permita:

- Obtener transacciones bancarias desde una API GET.
- Distribuir automáticamente las transacciones a colas por banco destino.
- Consumir las transacciones desde RabbitMQ.
- Enviar cada transacción a una API POST.
- Agregar información del estudiante antes del POST.
- Garantizar el uso de ACK manual para evitar pérdida de mensajes.
- Implementar un reintento básico en caso de fallo del POST.

---

## Objetivos cumplidos

### Producer

- Consume correctamente el endpoint GET `/transacciones`.
- Parsea correctamente el JSON recibido.
- Recorre todas las transacciones del lote.
- Identifica el campo `bancoDestino`.
- Crea una cola dinámica por banco si no existe.
- Envía cada transacción a RabbitMQ en formato JSON.
- Muestra logs informativos del proceso.

### Consumer

- Escucha múltiples colas (`BAC`, `BANRURAL`, `BI`, `GYT`).
- Consume mensajes JSON desde RabbitMQ.
- Deserializa cada mensaje a objeto Java.
- Agrega **nombre**, **carnet** y **correo** antes del POST.
- Envía cada transacción al endpoint POST.
- Implementa ACK manual.
- Solo confirma el mensaje si el POST responde exitosamente.
- Implementa reintento básico.

---

## Tecnologías utilizadas

- Java 17
- Maven
- RabbitMQ
- RabbitMQ Management Plugin
- Java HttpClient
- Jackson Databind
- Git
- GitHub
- Eclipse IDE

---

## Requisitos previos

Antes de ejecutar el proyecto se necesita tener instalado:

- Java 17
- Maven
- Erlang
- RabbitMQ Server
- RabbitMQ Management habilitado
- Git
- Eclipse IDE o cualquier IDE compatible con Maven
- Conexión a internet para consumir las APIs

---

## Estructura del repositorio

```text
ProyectoColas
├── producer-banco
│   ├── pom.xml
│   └── src/main/java/com/demo/producer
│       ├── ProducerApp.java
│       ├── config/
│       │   └── RabbitMQConfig.java
│       ├── model/
│       │   ├── LoteTransacciones.java
│       │   ├── Transaccion.java
│       │   ├── Detalle.java
│       │   └── Referencias.java
│       └── service/
│           ├── ApiService.java
│           └── RabbitProducerService.java
│
├── consumer-banco
│   ├── pom.xml
│   └── src/main/java/com/demo/consumer
│       ├── ConsumerApp.java
│       ├── config/
│       │   └── RabbitMQConfig.java
│       ├── model/
│       │   ├── Transaccion.java
│       │   ├── Detalle.java
│       │   ├── Referencias.java
│       │   └── TransaccionPost.java
│       └── service/
│           ├── ApiPostService.java
│           └── RabbitConsumerService.java
│
├── .gitignore
└── README.md
```

---

## APIs utilizadas

### 1. API GET de transacciones

**Endpoint:**

```text
https://hly784ig9d.execute-api.us-east-1.amazonaws.com/default/transacciones
```

**Función:**

- Obtiene el lote de transacciones bancarias.
- Cada transacción contiene el banco destino para enrutarla a RabbitMQ.

### 2. API POST de almacenamiento

**Endpoint:**

```text
https://7e0d9ogwzd.execute-api.us-east-1.amazonaws.com/default/guardarTransacciones
```

**Función:**

- Recibe una transacción individual.
- El Consumer agrega antes del envío:
  - `nombre`
  - `carnet`
  - `correo`

---

## Configuración de RabbitMQ

El sistema utiliza RabbitMQ local con la siguiente configuración:

- **Host:** `localhost`
- **Puerto:** `5672`
- **Usuario:** `guest`
- **Contraseña:** `guest`

**Panel de administración:**

```text
http://localhost:15672
```

---

## Modelo de datos

### Estructura base de una transacción

Cada transacción recibida desde la API GET tiene la siguiente estructura lógica:

- `idTransaccion`
- `monto`
- `moneda`
- `cuentaOrigen`
- `bancoDestino`
- `detalle`
  - `nombreBeneficiario`
  - `tipoTransferencia`
  - `descripcion`
  - `referencias`
    - `factura`
    - `codigoInterno`

### Estructura final enviada por el Consumer al POST

Antes de realizar el POST, el Consumer agrega:

- `nombre`
- `carnet`
- `correo`

---

## Flujo de funcionamiento

### Paso 1: Producer consume la API GET

El Producer se conecta al endpoint GET y obtiene el lote completo de transacciones.

### Paso 2: Producer recorre el lote

Cada transacción es recorrida una por una para identificar el valor del campo `bancoDestino`.

### Paso 3: Producer crea colas dinámicas

Si una cola para ese banco no existe, el Producer la crea automáticamente en RabbitMQ.

### Paso 4: Producer publica mensajes

Cada transacción se convierte a JSON y se envía a la cola correspondiente.

### Paso 5: Consumer escucha múltiples colas

El Consumer se mantiene escuchando las colas:

- `BAC`
- `BANRURAL`
- `BI`
- `GYT`

### Paso 6: Consumer consume mensajes

Cada mensaje se recibe desde RabbitMQ, se convierte nuevamente a objeto Java y se prepara para el POST.

### Paso 7: Consumer agrega datos del estudiante

Antes de enviar la transacción al POST, agrega:

- `nombre`
- `carnet`
- `correo`

### Paso 8: Consumer hace el POST

La transacción enriquecida se envía a la API de almacenamiento.

### Paso 9: Consumer envía ACK manual

Si el POST responde correctamente, el Consumer confirma el mensaje con ACK.

### Paso 10: Reintento básico

Si el POST falla, el Consumer realiza un reintento básico antes de reencolar el mensaje.

---

## Instrucciones de ejecución

### 1. Iniciar RabbitMQ

Verificar que RabbitMQ esté funcionando correctamente y que el panel de administración esté disponible en:

```text
http://localhost:15672
```

### 2. Ejecutar Producer

Desde Eclipse o desde terminal, ejecutar la clase principal:

```text
ProducerApp.java
```

#### Qué hace el Producer

- Consume la API GET.
- Obtiene el lote.
- Identifica el banco destino.
- Crea la cola por banco.
- Publica mensajes en RabbitMQ.

#### Resultado esperado del Producer

Se crean las colas:

- `BAC`
- `BANRURAL`
- `BI`
- `GYT`

RabbitMQ queda con mensajes en cada cola.

### 3. Verificar colas en RabbitMQ

Después de ejecutar el Producer, revisar que las colas aparezcan con mensajes en RabbitMQ Management.

### 4. Ejecutar Consumer

Desde Eclipse o desde terminal, ejecutar la clase principal:

```text
ConsumerApp.java
```

#### Qué hace el Consumer

- Escucha colas.
- Recibe mensajes.
- Deserializa el JSON.
- Agrega nombre, carnet y correo.
- Hace el POST.
- Realiza ACK manual.

### 5. Verificar colas vacías

Al finalizar correctamente, RabbitMQ debe mostrar:

- `Ready = 0`
- `Unacked = 0`
- `Total = 0`

---

## Configuración interna del proyecto

### Producer

El Producer utiliza:

- `ApiService` para consumir el GET.
- `RabbitProducerService` para enviar mensajes a RabbitMQ.
- `RabbitMQConfig` para la configuración de conexión.
- Modelos Java (`LoteTransacciones`, `Transaccion`, `Detalle`, `Referencias`) para mapear el JSON.

### Consumer

El Consumer utiliza:

- `RabbitConsumerService` para escuchar colas.
- `ApiPostService` para enviar la transacción al POST.
- `RabbitMQConfig` para la configuración de conexión.
- `TransaccionPost` para agregar nombre, carnet y correo antes del POST.
- ACK manual y reintento básico.

---

## Clases principales

### ProducerApp

Clase principal del Producer. Orquesta:

- Consumo del GET.
- Recorrido de transacciones.
- Publicación en RabbitMQ.

### ApiService

Se encarga de consumir la API GET y convertir la respuesta a `LoteTransacciones`.

### RabbitProducerService

Se encarga de:

- Abrir conexión con RabbitMQ.
- Declarar colas.
- Publicar cada transacción.

### ConsumerApp

Clase principal del Consumer. Inicia la escucha de colas.

### RabbitConsumerService

Se encarga de:

- Escuchar varias colas.
- Recibir mensajes.
- Deserializar JSON.
- Invocar el POST.
- Manejar ACK y reintento.

### ApiPostService

Se encarga de:

- Construir el JSON final.
- Agregar nombre, carnet y correo.
- Ejecutar el POST.
- Validar el código de respuesta.

---

## Pruebas realizadas

### Prueba 1: GET exitoso

Se verificó que el Producer consume correctamente el endpoint GET y obtiene un lote de 100 transacciones.

### Prueba 2: Creación de colas por banco

Se verificó que RabbitMQ crea dinámicamente las colas por banco destino:

- `BAC`
- `BANRURAL`
- `BI`
- `GYT`

### Prueba 3: Publicación correcta en RabbitMQ

Se verificó que las transacciones se almacenan en las colas correspondientes y se distribuyen correctamente.

### Prueba 4: Consumo y POST exitoso

Se verificó que el Consumer consume mensajes de RabbitMQ y envía correctamente cada transacción al endpoint POST.

### Prueba 5: ACK manual

Se verificó que el mensaje solo se confirma cuando el POST responde exitosamente.

### Prueba 6: Inclusión de datos del estudiante

Se verificó que el JSON enviado al POST incluye:

- `nombre`
- `carnet`
- `correo`

### Prueba 7: Reintento básico

Se implementó reintento básico en el Consumer antes de reenviar o reencolar el mensaje si el POST falla.

### Prueba 8: Flujo completo

Se verificó el flujo completo:

- GET exitoso
- Colas llenas en RabbitMQ
- Consumo exitoso
- POST correcto
- Colas vacías al final

---

## Ejemplo del JSON enviado por el Consumer

```json
{
  "idTransaccion": "TX-10081",
  "monto": 3560.25,
  "moneda": "GTQ",
  "cuentaOrigen": "001-100081-7",
  "bancoDestino": "GYT",
  "nombre": "Elder Perez",
  "carnet": "0905-24-1631",
  "correo": "eperezy7@miumg.edu.gt",
  "detalle": {
    "nombreBeneficiario": "Cliente 82",
    "tipoTransferencia": "INTERBANCARIA",
    "descripcion": "Pago simulado",
    "referencias": {
      "factura": "F-80081",
      "codigoInterno": "REF1081"
    }
  }
}
```

---

## Logs esperados

### Ejemplo de logs del Producer

```text
Lote recibido: L-2026-0312-001
Fecha generación: 2026-03-12T...
Cantidad de transacciones: 100
Procesando ID: TX-10081 | Banco: GYT | Monto: 3560.25
Transacción enviada a cola: GYT | ID: TX-10081
Todas las transacciones fueron enviadas a RabbitMQ.
```

### Ejemplo de logs del Consumer

```text
Consumer escuchando colas: [BAC, BANRURAL, BI, GYT]
Mensaje recibido desde cola GYT | ID: TX-10081
Intento 1 para enviar ID: TX-10081
JSON enviado al POST: {...}
Respuesta POST: 201
Body respuesta: {"message":"Transaccion almacenada","id":"TX-10081"}
ACK enviado para ID: TX-10081
```

---

## Evidencias obtenidas

Durante la ejecución se verificó:

- Consola del Producer enviando transacciones a RabbitMQ.
- RabbitMQ con colas llenas por banco.
- Consola del Consumer procesando mensajes.
- JSON enviado al POST con nombre, carnet y correo.
- Respuestas POST exitosas con código `201`.
- ACK enviados correctamente.
- RabbitMQ con colas vacías al finalizar.

---

## Rama de trabajo

El proyecto fue preparado y subido en la rama:

```text
ExamenParcial
```
---

## Posibles mensajes durante la ejecución

### Mensaje SLF4J

Durante la ejecución del Consumer puede aparecer un mensaje relacionado con SLF4J.

**Ejemplo:**

```text
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
```

### Significado

Ese mensaje no impide la ejecución del proyecto y no afecta el flujo principal del Producer ni del Consumer.

---

## Consideraciones importantes

- El Consumer puede mostrar temporalmente mensajes en estado `unacked` mientras procesa mensajes.
- Al finalizar correctamente, todas las colas deben quedar en cero.
- El Consumer permanece escuchando aunque ya no existan mensajes, por lo que puede requerir detenerlo manualmente desde Eclipse.
- RabbitMQ debe estar iniciado antes de correr Producer o Consumer.

---

## Conclusión

Se desarrolló correctamente un sistema distribuido con **Java**, **Maven** y **RabbitMQ** que permite procesar transacciones bancarias mediante el patrón **Producer–Consumer**.

El proyecto cumple con:

- Distribución automática por banco.
- Procesamiento independiente por entidad.
- Integración con RabbitMQ.
- Integración con APIs externas.
- Uso de ACK manual.
- Inclusión de datos del estudiante en el POST.
- Reintento básico en caso de fallo.

---

## Autor

- **Nombre:** Elder Perez
- **Carnet:** 0905-24-1631
- **Correo:** eperezy7@miumg.edu.gt
- **Link explicación del proyecto:** [https://drive.google.com/file/d/1s-owQ2ocVtZcHVHrD-XOG-kOGZ1MdUhM/view?usp=sharing]


