package com.demo.producer.config;

import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConfig {

    public static ConnectionFactory getConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        return factory;
    }
}
