package com.demo.consumer.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.demo.consumer.config.RabbitMQConfig;
import com.demo.consumer.model.Transaccion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

public class RabbitConsumerService {

    private final ObjectMapper objectMapper;
    private final ApiPostService apiPostService;

    // Guarda en memoria los IDs ya procesados
    private static final Set<String> idsProcesados = ConcurrentHashMap.newKeySet();

    public RabbitConsumerService() {
        this.objectMapper = new ObjectMapper();
        this.apiPostService = new ApiPostService();
    }

    public void escucharColas() throws Exception {
        List<String> colas = Arrays.asList(
                RabbitMQConfig.COLA_BAC,
                RabbitMQConfig.COLA_BANRURAL,
                RabbitMQConfig.COLA_BI,
                RabbitMQConfig.COLA_GYT
        );

        Channel channel = RabbitMQConfig.getConnectionFactory()
                .newConnection()
                .createChannel();

        // Declarar colas normales
        for (String cola : colas) {
            channel.queueDeclare(cola, true, false, false, null);
        }

        // Declarar cola de duplicados
        channel.queueDeclare(RabbitMQConfig.COLA_DUPLICADOS, true, false, false, null);

        System.out.println("Consumer escuchando colas: " + colas);

        for (String cola : colas) {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String mensajeJson = new String(delivery.getBody(), StandardCharsets.UTF_8);

                try {
                    Transaccion transaccion = objectMapper.readValue(mensajeJson, Transaccion.class);
                    String id = transaccion.getIdTransaccion();

                    System.out.println("COLA ATENDIDA: " + cola);
                    System.out.println("ID TRANSACCION: " + id);

                    // Verificar si ya fue procesada
                    if (idsProcesados.contains(id)) {
                        channel.basicPublish(
                                "",
                                RabbitMQConfig.COLA_DUPLICADOS,
                                null,
                                mensajeJson.getBytes(StandardCharsets.UTF_8)
                        );

                        System.out.println("ESTADO: DUPLICADA");
                        System.out.println("COLA DESTINO: " + RabbitMQConfig.COLA_DUPLICADOS);
                        System.out.println("OBSERVACION: ID repetido, no se envia al POST");
                        System.out.println("--------------------------------------------------");

                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        return;
                    }

                    boolean exito = enviarConReintento(transaccion);

                    if (exito) {
                        idsProcesados.add(id);

                        System.out.println("ESTADO: PROCESADA");
                        System.out.println("COLA DESTINO: POST /guardarTransacciones");
                        System.out.println("--------------------------------------------------");

                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        System.out.println("ACK enviado para ID: " + id);
                    } else {
                        System.out.println("ESTADO: ERROR");
                        System.out.println("COLA DESTINO: REINTENTO / REENCOLADO");
                        System.out.println("--------------------------------------------------");

                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                        System.out.println("NACK enviado y mensaje reencolado para ID: " + id);
                    }

                } catch (Exception e) {
                    System.out.println("Error procesando mensaje: " + e.getMessage());
                    try {
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            };

            CancelCallback cancelCallback = consumerTag -> {
                System.out.println("Consumo cancelado: " + consumerTag);
            };

            channel.basicConsume(cola, false, deliverCallback, cancelCallback);
        }
    }

    private boolean enviarConReintento(Transaccion transaccion) {
        int intentos = 0;
        int maxIntentos = 2;

        while (intentos < maxIntentos) {
            try {
                intentos++;
                System.out.println("Intento " + intentos + " para enviar ID: "
                        + transaccion.getIdTransaccion());

                boolean exito = apiPostService.enviarTransaccion(transaccion);

                if (exito) {
                    return true;
                }

            } catch (Exception e) {
                System.out.println("Error en intento " + intentos + " para ID "
                        + transaccion.getIdTransaccion() + ": " + e.getMessage());
            }
        }

        return false;
    }
}