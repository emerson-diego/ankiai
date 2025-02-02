package com.example.ankiaibackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ankiaibackend.model.Sentence;
import com.example.ankiaibackend.repository.SentenceRepository;
import com.example.ankiaibackend.service.HuggingFaceService;

@RestController
@RequestMapping("/generate")
public class GenerationController {

    @Autowired
    private SentenceRepository sentenceRepository;

    @Autowired
    private HuggingFaceService huggingFaceService;

    /**
     * Endpoint que, dado o id de uma Sentence (cujo campo 'text' é uma palavra),
     * gera uma frase em inglês contendo essa palavra, traduz para o português,
     * incrementa o campo 'treino' e retorna as informações.
     *
     * Exemplo de requisição: GET /generate/{id}
     *
     * @param id O id da Sentence.
     * @return JSON com a frase em inglês, a tradução em português e o novo valor de
     *         treino.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> generateSentenceAndTranslate(@PathVariable("id") String id) {
        Optional<Sentence> optionalSentence = sentenceRepository.findById(id);
        if (!optionalSentence.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Sentence sentence = optionalSentence.get();
        String word = sentence.getText();

        // Gera a frase em inglês que contenha a palavra
        String englishSentence = huggingFaceService.generateSentence(word);
        // Traduz a frase para o português
        String portugueseSentence = huggingFaceService.translateToPortuguese(englishSentence);

        // Atualiza o contador de treino (incrementa +1)
        Integer treino = sentence.getTreino() != null ? sentence.getTreino() : 0;
        sentence.setTreino(treino + 1);
        sentenceRepository.save(sentence);

        // Monta a resposta
        Map<String, Object> response = new HashMap<>();
        response.put("englishSentence", englishSentence);
        response.put("portugueseSentence", portugueseSentence);
        response.put("treino", sentence.getTreino());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/random")
    public ResponseEntity<?> generateRandomSentenceAndTranslate() {
        List<Sentence> sentences = sentenceRepository.findAll();
        if (sentences.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Random random = new Random();
        Sentence sentence = sentences.get(random.nextInt(sentences.size()));
        String word = sentence.getText();

        // Gera a frase em inglês que contenha a palavra
        String englishSentence = huggingFaceService.generateSentence(word);

        // Se a frase gerada estiver vazia ou indicar erro, retorne a mensagem sem
        // tentar traduzir
        if (englishSentence == null || englishSentence.trim().isEmpty() ||
                englishSentence.contains("Unable to generate sentence")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Não foi possível gerar a frase no momento. Tente novamente mais tarde.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }

        // Traduz a frase para o português
        String portugueseSentence = huggingFaceService.translateToPortuguese(englishSentence);

        // Atualiza o contador de treino (incrementa +1)
        Integer treino = sentence.getTreino() != null ? sentence.getTreino() : 0;
        sentence.setTreino(treino + 1);
        sentenceRepository.save(sentence);

        // Monta a resposta
        Map<String, Object> response = new HashMap<>();
        response.put("englishSentence", englishSentence);
        response.put("portugueseSentence", portugueseSentence);
        response.put("treino", sentence.getTreino());

        return ResponseEntity.ok(response);
    }
}