package com.example.ankiaibackend.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SentenceClassificationService {

    private final String MODEL_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";

    @Value("${hf.api.token}")
    private String HF_API_TOKEN;

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Classifica o termo informado entre os rótulos: "word", "phrasal verb" e
     * "idiom".
     * Caso nenhuma das três opções seja suficientemente provável, retorna "other".
     *
     * @param term o termo a ser classificado.
     * @return o rótulo predito ou "other" se a confiança for baixa.
     */
    public String classificar(String term) {
        try {
            // Adiciona contexto para melhorar a interpretação do modelo
            String contextualizedInput = String.format("Classifique o seguinte termo: %s", term);

            // Monta o payload com os rótulos desejados (incluindo "other" como fallback)
            Map<String, Object> payload = new HashMap<>();
            payload.put("inputs", contextualizedInput);

            Map<String, Object> parameters = new HashMap<>();
            List<String> candidateLabels = Arrays.asList("word", "phrasal verb", "idiom", "other");
            parameters.put("candidate_labels", candidateLabels);
            payload.put("parameters", parameters);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(HF_API_TOKEN);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(MODEL_URL, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode labelsNode = root.get("labels");
                JsonNode scoresNode = root.get("scores");

                if (labelsNode != null && scoresNode != null && labelsNode.size() > 0 && scoresNode.size() > 0) {
                    String labelPredito = labelsNode.get(0).asText();
                    double maxScore = scoresNode.get(0).asDouble();

                    // Se o modelo escolheu "other" ou se a confiança for baixa, retorna "other"
                    double threshold = 0.3; // ajuste conforme os experimentos
                    if ("other".equalsIgnoreCase(labelPredito) || maxScore < threshold) {
                        return "other";
                    } else {
                        return labelPredito;
                    }
                } else {
                    return "other";
                }
            } else {
                return "other";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "other";
        }
    }
}