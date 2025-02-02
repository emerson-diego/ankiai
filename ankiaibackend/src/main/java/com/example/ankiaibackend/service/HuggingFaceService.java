package com.example.ankiaibackend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HuggingFaceService {

    @Value("${hf.api.token}")
    private String HF_API_TOKEN;

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Gera uma frase em inglês contendo a palavra informada usando o modelo
     * distilgpt2.
     * Implementa retry caso o modelo esteja ocupado.
     *
     * @param word A palavra que deve aparecer na frase.
     * @return Frase gerada ou mensagem de erro.
     */
    public String generateSentence(String word) {
        String modelUrl = "https://api-inference.huggingface.co/models/distilgpt2";
        String prompt = "Generate an English sentence that contains the word \"" + word + "\".";
        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", prompt);

        // Parâmetro opcional para definir o tamanho máximo da geração
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_length", 50);
        payload.put("parameters", parameters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(HF_API_TOKEN);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        int maxRetries = 3;
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(modelUrl, request, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    if (root.isArray() && root.size() > 0) {
                        String generatedText = root.get(0).get("generated_text").asText();
                        // Remove o prompt caso esteja presente no início do texto gerado
                        if (generatedText.startsWith(prompt)) {
                            generatedText = generatedText.substring(prompt.length()).trim();
                        }
                        return generatedText;
                    }
                }
                break;
            } catch (HttpServerErrorException e) {
                if (e.getMessage().contains("Model too busy")) {
                    attempt++;
                    try {
                        Thread.sleep(3000); // aguarda 3 segundos antes de tentar novamente
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "Generation interrupted.";
                    }
                } else {
                    e.printStackTrace();
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return "Unable to generate sentence at this moment. Please try again later.";
    }

    /**
     * Traduz uma frase do inglês para o português utilizando o modelo
     * Helsinki-NLP/opus-mt-en-ROMANCE.
     *
     * @param englishSentence Frase em inglês.
     * @return Tradução da frase ou mensagem de erro.
     */
    public String translateToPortuguese(String englishSentence) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("inputs", englishSentence);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(HF_API_TOKEN);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Modelo para tradução: Helsinki-NLP/opus-mt-en-ROMANCE (suporta tradução para
            // português)
            String modelUrl = "https://api-inference.huggingface.co/models/Helsinki-NLP/opus-mt-en-ROMANCE";
            ResponseEntity<String> response = restTemplate.postForEntity(modelUrl, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    String translation = root.get(0).get("translation_text").asText();
                    return translation;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unable to translate sentence at this moment.";
    }
}
