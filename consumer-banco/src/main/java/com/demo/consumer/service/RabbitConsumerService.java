package com.demo.consumer.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.demo.consumer.config.RabbitMQConfig;
import com.demo.consumer.model.Transaccion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

public class RabbitConsumerService {

    private final ObjectMapper objectMapper;
    private final ApiPostService apiPostService;

    public RabbitConsumerService() {
        this.objectMapper = new ObjectMapper();
        this.apiPostService = new ApiPostService();
    }

    public void escucharColas() throws Exception {
        List<String> colas = Arrays.asList("BAC", "BANRURAL", "BI", "GYT");

        Channel channel = RabbitMQConfig.getConnectionFactory()
                .newConnection()
                .createChannel();

        for (String cola : colas) {
            channel.queueDeclare(cola, true, false, false, null);
        }

        System.out.println("Consumer escuchando colas: " + colas);

        for (String cola : colas) {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String mensajeJson = new String(delivery.getBody(), StandardCharsets.UTF_8);

                try {
                    Transaccion transaccion = objectMapper.readValue(mensajeJson, Transaccion.class);

                    System.out.println("Mensaje recibido desde cola " + cola
                            + " | ID: " + transaccion.getIdTransaccion());

                    boolean exito = enviarConReintento(transaccion);

                    if (exito) {
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        System.out.println("ACK enviado para ID: " + transaccion.getIdTransaccion());
                    } else {
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                        System.out.println("NACK enviado y mensaje reencolado para ID: "
                                + transaccion.getIdTransaccion());
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