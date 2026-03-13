package com.demo.producer.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.demo.producer.model.LoteTransacciones;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiService {

    private static final String GET_URL =
            "https://hly784ig9d.execute-api.us-east-1.amazonaws.com/default/transacciones";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public LoteTransacciones obtenerLote() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GET_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al consumir GET /transacciones. Código: " + response.statusCode());
        }

        return objectMapper.readValue(response.body(), LoteTransacciones.class);
    }
}
