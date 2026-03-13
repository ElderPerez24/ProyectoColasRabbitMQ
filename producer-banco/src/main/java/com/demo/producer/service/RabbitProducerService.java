package com.demo.producer.service;

import java.nio.charset.StandardCharsets;

import com.demo.producer.config.RabbitMQConfig;
import com.demo.producer.model.Transaccion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitProducerService {

    private final ObjectMapper objectMapper;
    private final Connection connection;
    private final Channel channel;

    public RabbitProducerService() throws Exception {
        this.objectMapper = new ObjectMapper();

        ConnectionFactory factory = RabbitMQConfig.getConnectionFactory();
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
    }

    public void enviarTransaccion(Transaccion transaccion) throws Exception {
        String nombreCola = transaccion.getBancoDestino();

        channel.queueDeclare(nombreCola, true, false, false, null);

        String json = objectMapper.writeValueAsString(transaccion);

        channel.basicPublish("", nombreCola, null, json.getBytes(StandardCharsets.UTF_8));

        System.out.println("Transacción enviada a cola: " + nombreCola
                + " | ID: " + transaccion.getIdTransaccion());
    }

    public void cerrarConexion() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (Exception e) {
            System.out.println("Error al cerrar canal: " + e.getMessage());
        }

        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception e) {
            System.out.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
}
