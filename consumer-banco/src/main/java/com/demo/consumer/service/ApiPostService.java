package com.demo.consumer.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.demo.consumer.model.Transaccion;
import com.demo.consumer.model.TransaccionPost;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiPostService {

    private static final String POST_URL =
            "https://7e0d9ogwzd.execute-api.us-east-1.amazonaws.com/default/guardarTransacciones";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiPostService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public boolean enviarTransaccion(Transaccion transaccion) throws IOException, InterruptedException {
        TransaccionPost transaccionPost = convertirTransaccion(transaccion);

        String json = objectMapper.writeValueAsString(transaccionPost);

        System.out.println("JSON enviado al POST: " + json);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(POST_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Respuesta POST: " + response.statusCode());
        System.out.println("Body respuesta: " + response.body());

        return response.statusCode() == 200 || response.statusCode() == 201;
    }

    private TransaccionPost convertirTransaccion(Transaccion transaccion) {
        TransaccionPost transaccionPost = new TransaccionPost();

        transaccionPost.setIdTransaccion(transaccion.getIdTransaccion());
        transaccionPost.setMonto(transaccion.getMonto());
        transaccionPost.setMoneda(transaccion.getMoneda());
        transaccionPost.setCuentaOrigen(transaccion.getCuentaOrigen());
        transaccionPost.setBancoDestino(transaccion.getBancoDestino());
        transaccionPost.setDetalle(transaccion.getDetalle());

        transaccionPost.setNombre("Elder Perez");
        transaccionPost.setCarnet("0905-24-1631");
        transaccionPost.setCorreo("eperezy7@miumg.edu.gt");

        return transaccionPost;
    }
}
