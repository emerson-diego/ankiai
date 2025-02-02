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
        // Ajuste o prompt para instruir de forma mais clara e criativa
        String prompt = "Write a creative and natural English sentence that uses the word \"" + word + "\".";
        String modelUrl = "https://api-inference.huggingface.co/models/distilgpt2";

        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", prompt);

        // Parâmetros de geração ajustados para incentivar variação e criatividade
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_length", 100); // Aumenta o tamanho máximo para gerar mais conteúdo
        parameters.put("do_sample", true); // Ativa a amostragem aleatória
        parameters.put("temperature", 0.8); // Define a temperatura para controlar a aleatoriedade
        parameters.put("top_k", 50); // Limita os tokens mais prováveis a serem considerados
        parameters.put("top_p", 0.95); // Usa nucleus sampling
        // Se suportado pelo modelo, podemos tentar retornar apenas o novo texto
        parameters.put("return_full_text", false);
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
                System.out.println("Resposta da API: " + response.getBody());

                if (response.getStatusCode() == HttpStatus.OK) {
                    JsonNode root = objectMapper.readTree(response.getBody());

                    if (root.isArray() && root.size() > 0) {
                        JsonNode firstElement = root.get(0);

                        if (firstElement.has("generated_text")) {
                            String generatedText = firstElement.get("generated_text").asText();

                            // Se o prompt for repetido no início da resposta, removê-lo
                            if (generatedText.startsWith(prompt)) {
                                generatedText = generatedText.substring(prompt.length()).trim();
                            }

                            if (generatedText == null || generatedText.trim().isEmpty()) {
                                System.out.println("generated_text está vazio.");
                            } else {
                                return generatedText;
                            }
                        } else {
                            System.out.println(
                                    "Campo 'generated_text' não encontrado na resposta: " + firstElement.toString());
                        }
                    } else {
                        System.out.println("Resposta da API não é um array ou está vazia: " + root.toString());
                    }
                } else {
                    System.out.println("Status de resposta não OK: " + response.getStatusCode());
                }
                // Caso não obtenha um resultado válido, interrompe as tentativas
                break;
            } catch (HttpServerErrorException e) {
                // Se o modelo estiver ocupado ou carregando, tenta novamente
                if (e.getMessage().contains("Model too busy") || e.getMessage().contains("currently loading")) {
                    attempt++;
                    System.out.println("Tentativa " + attempt + " falhou. Modelo ocupado, aguardando 3 segundos...");
                    try {
                        Thread.sleep(3000);
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
