package com.demo.consumer;

import com.demo.consumer.service.RabbitConsumerService;

public class ConsumerApp {

    public static void main(String[] args) {
        RabbitConsumerService consumerService = new RabbitConsumerService();

        try {
            consumerService.escucharColas();
            System.out.println("Consumer iniciado correctamente.");

        } catch (Exception e) {
            System.out.println("Error al iniciar Consumer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}