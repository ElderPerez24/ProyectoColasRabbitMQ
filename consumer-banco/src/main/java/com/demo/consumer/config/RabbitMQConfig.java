package com.demo.consumer.config;

import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConfig {

    public static final String COLA_BAC = "BAC";
    public static final String COLA_BANRURAL = "BANRURAL";
    public static final String COLA_BI = "BI";
    public static final String COLA_GYT = "GYT";
    public static final String COLA_DUPLICADOS = "COLA_DUPLICADOS";

    public static ConnectionFactory getConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        return factory;
    }
}
