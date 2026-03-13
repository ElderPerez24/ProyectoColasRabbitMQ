package com.demo.producer;

import com.demo.producer.model.LoteTransacciones;
import com.demo.producer.model.Transaccion;
import com.demo.producer.service.ApiService;
import com.demo.producer.service.RabbitProducerService;

public class ProducerApp {

    public static void main(String[] args) {
        ApiService apiService = new ApiService();
        RabbitProducerService producerService = null;

        try {
            producerService = new RabbitProducerService();

            LoteTransacciones lote = apiService.obtenerLote();

            System.out.println("Lote recibido: " + lote.getLoteId());
            System.out.println("Fecha generación: " + lote.getFechaGeneracion());
            System.out.println("Cantidad de transacciones: " + lote.getTransacciones().size());

            for (Transaccion transaccion : lote.getTransacciones()) {
                System.out.println("Procesando ID: " + transaccion.getIdTransaccion()
                        + " | Banco: " + transaccion.getBancoDestino()
                        + " | Monto: " + transaccion.getMonto());

                producerService.enviarTransaccion(transaccion);
            }

            System.out.println("Todas las transacciones fueron enviadas a RabbitMQ.");

        } catch (Exception e) {
            System.out.println("Error en Producer: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (producerService != null) {
                producerService.cerrarConexion();
            }
        }
    }
}
