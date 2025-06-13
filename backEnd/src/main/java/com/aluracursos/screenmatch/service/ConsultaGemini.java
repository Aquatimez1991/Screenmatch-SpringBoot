package com.aluracursos.screenmatch.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class ConsultaGemini {

    @Value("${gemini.api.key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        verificarApiKey();
    }

    public String obtenerTraduccion(String texto) {
        String modelo = "gemini-2.0-flash-lite";
        String prompt = "Traduce el siguiente texto al español: " + texto;

        try {
            Client cliente = new Client.Builder().apiKey(apiKey).build();

            GenerateContentResponse respuesta = cliente.models.generateContent(
                    modelo,
                    prompt,
                    null
            );

            if (!respuesta.text().isEmpty()) {
                return respuesta.text()
                        .replace("Aquí tienes la traducción al español:", "")
                        .replace("El texto se traduce al español como:", "")
                        .trim();
            }
        } catch (Exception e) {
            System.err.println("Error al llamar a la API de Gemini para traducción: " + e.getMessage());
        }

        return null;
    }

    public void verificarApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("La API Key no se ha configurado correctamente.");
        } else {
            System.out.println("La API Key se ha leído correctamente: " + apiKey);
        }
    }
}