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
     * Gera uma frase em inglês que incorpora a palavra ou texto selecionado usando o modelo deepseek-ai/DeepSeek-R1-Distill-Qwen-32B.
     * Implementa retry caso o modelo esteja ocupado.
     *
     * @param selectedText A palavra ou texto selecionado que deve ser incorporado na frase.
     * @return Frase gerada ou mensagem de erro.
     */
    public String generateSentence(String selectedText) {
        // Atualiza o prompt para que o modelo retorne a frase delimitada por <sentence> e </sentence>
        String prompt = "Please provide exactly one example sentence in English that uses the following word: \""
                        + selectedText
                        + "\". Output only the sentence in the following format: <sentence> Your generated sentence here </sentence>";
        String modelUrl = "https://api-inference.huggingface.co/models/deepseek-ai/DeepSeek-R1-Distill-Qwen-32B";
    
        // Criação do payload para a requisição
        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", prompt);
    
        // Configuração dos parâmetros de geração
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_new_tokens", 1000);
        parameters.put("do_sample", true);
        parameters.put("temperature", 0.2);
        parameters.put("top_k", 50);
        parameters.put("top_p", 0.95);
        parameters.put("return_full_text", true);
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
    
                    // Verifica se a API retornou um erro
                    if (root.has("error")) {
                        String errorMsg = root.get("error").asText();
                        System.out.println("Erro na API: " + errorMsg);
                        if (errorMsg.contains("busy") || errorMsg.contains("loading")) {
                            attempt++;
                            System.out.println("Tentativa " + attempt + " falhou. Modelo ocupado/carregando, aguardando 3 segundos...");
                            Thread.sleep(3000);
                            continue;
                        } else {
                            break;
                        }
                    }
    
                    if (root.isArray() && root.size() > 0) {
                        JsonNode firstElement = root.get(0);
                        if (firstElement.has("generated_text")) {
                            String generatedText = firstElement.get("generated_text").asText();
    
                            // Remove o prompt caso esteja repetido no início da resposta
                            if (generatedText.startsWith(prompt)) {
                                generatedText = generatedText.substring(prompt.length()).trim();
                            }
    
                            // Extrai a frase delimitada entre <sentence> e </sentence>
                            int inicio = generatedText.indexOf("<sentence>");
                            int fim = generatedText.indexOf("</sentence>");
                            if (inicio != -1 && fim != -1 && inicio < fim) {
                                String sentence = generatedText.substring(inicio + "<sentence>".length(), fim).trim();
                                if (!sentence.isEmpty()) {
                                    return sentence;
                                } else {
                                    System.out.println("A frase extraída está vazia.");
                                }
                            } else {
                                System.out.println("Marcadores <sentence> não encontrados na resposta: " + generatedText);
                            }
                        } else {
                            System.out.println("Campo 'generated_text' não encontrado na resposta: " + firstElement.toString());
                        }
                    } else {
                        System.out.println("Resposta da API não é um array ou está vazia: " + root.toString());
                    }
                } else {
                    System.out.println("Status de resposta não OK: " + response.getStatusCode());
                }
                break;
            } catch (HttpServerErrorException e) {
                String exceptionMessage = e.getMessage();
                if (exceptionMessage.contains("busy") || exceptionMessage.contains("loading")) {
                    attempt++;
                    System.out.println("Tentativa " + attempt + " falhou. Modelo ocupado/carregando, aguardando 3 segundos...");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "Generation interrupted.";
                    }
                    continue;
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
     * Traduz uma frase do inglês para o português utilizando o mesmo modelo deepseek-ai/DeepSeek-R1-Distill-Qwen-32B.
     * O prompt instrui a tradução da frase e retorna apenas a sentença traduzida.
     *
     * @param englishSentence Frase em inglês a ser traduzida.
     * @return Tradução da frase ou mensagem de erro.
     */
    public String translateToPortuguese(String englishSentence) {
        // Prompt atualizado para forçar o formato exato e evitar explicações adicionais.
        String prompt = "Traduza a seguinte sentença do inglês para o português. Sua resposta DEVE CONTER APENAS a tradução e nada mais. " +
                        "A resposta DEVE COMEÇAR com o delimitador <translation> e TERMINAR com o delimitador </translation>. " +
                        "Sentença: \"" + englishSentence + "\".";
    
        String modelUrl = "https://api-inference.huggingface.co/models/deepseek-ai/DeepSeek-R1-Distill-Qwen-32B";
    
        // Cria o payload para a requisição
        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", prompt);
    
        // Configura os parâmetros de geração: temperatura baixa para respostas mais diretas
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_new_tokens", 1000);
        parameters.put("do_sample", true);
        parameters.put("temperature", 0.2); // Valor menor para reduzir a geração de chain-of-thought
        parameters.put("top_k", 50);
        parameters.put("top_p", 0.95);
        parameters.put("return_full_text", true);
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
                System.out.println("Resposta da API (tradução): " + response.getBody());
    
                if (response.getStatusCode() == HttpStatus.OK) {
                    JsonNode root = objectMapper.readTree(response.getBody());
    
                    if (root.has("error")) {
                        String errorMsg = root.get("error").asText();
                        System.out.println("Erro na API (tradução): " + errorMsg);
                        if (errorMsg.contains("busy") || errorMsg.contains("loading")) {
                            attempt++;
                            System.out.println("Tentativa " + attempt + " falhou. Modelo ocupado/carregando, aguardando 3 segundos...");
                            Thread.sleep(3000);
                            continue;
                        } else {
                            break;
                        }
                    }
    
                    if (root.isArray() && root.size() > 0) {
                        JsonNode firstElement = root.get(0);
                        if (firstElement.has("generated_text")) {
                            String generatedText = firstElement.get("generated_text").asText();
                            System.out.println("Texto gerado: " + generatedText);
    
                            // Extrai a tradução delimitada entre <translation> e </translation>
                            String cleanedText = generatedText.replace("\n", ""); // Remove quebras de linha
                            int inicio = cleanedText.lastIndexOf("<translation>");  // Encontra a *última* ocorrência
                            int fim = cleanedText.lastIndexOf("</translation>");
                            if (inicio != -1 && fim != -1 && inicio < fim) {
                                String translatedText = cleanedText.substring(inicio + "<translation>".length(), fim).trim();
                                if (!translatedText.isEmpty()) {
                                    return translatedText;
                                } else {
                                    System.out.println("A tradução extraída está vazia.");
                                }
                            } else {
                                System.out.println("Marcadores <translation> não encontrados na resposta: " + generatedText);
                            }
                        } else {
                            System.out.println("Campo 'generated_text' não encontrado na resposta (tradução): " + firstElement.toString());
                        }
                    } else {
                        System.out.println("Resposta da API (tradução) não é um array ou está vazia: " + root.toString());
                    }
                } else {
                    System.out.println("Status de resposta (tradução) não OK: " + response.getStatusCode());
                }
                break;
            } catch (HttpServerErrorException e) {
                String exceptionMessage = e.getMessage();
                if (exceptionMessage.contains("busy") || exceptionMessage.contains("loading")) {
                    attempt++;
                    System.out.println("Tentativa " + attempt + " falhou. Modelo ocupado/carregando, aguardando 3 segundos...");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "Translation interrupted.";
                    }
                    continue;
                } else {
                    e.printStackTrace();
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return "Unable to translate sentence at this moment.";
    }


    /**
     * Gera áudio a partir do texto informado.
     *
     * @param sentence Texto que será convertido em áudio.
     * @return Vetor de bytes contendo o áudio gerado.
     */
    public byte[] generateAudio(String sentence) {
        String modelUrl = "https://api-inference.huggingface.co/models/" + "facebook/mms-tts";
        
        // Criação do payload com o texto
        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", sentence);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(HF_API_TOKEN);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                ResponseEntity<byte[]> response = restTemplate.postForEntity(modelUrl, request, byte[].class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return response.getBody();
                } else {
                    attempt++;
                    // Aguarda 3 segundos antes de tentar novamente
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                attempt++;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        throw new RuntimeException("Não foi possível gerar áudio no momento.");
    }

}
