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

import com.example.ankiaibackend.datasource.SentenceDataSource;
import com.example.ankiaibackend.model.Sentence;
import com.example.ankiaibackend.service.HuggingFaceService;

@RestController
@RequestMapping("/generate")
public class GenerationController {

    @Autowired
    private SentenceDataSource sentenceDataSource;

    @Autowired
    private HuggingFaceService huggingFaceService;

    @GetMapping("/{id}")
    public ResponseEntity<?> generateSentenceAndTranslate(@PathVariable("id") String id) {
        Optional<Sentence> optionalSentence = sentenceDataSource.findById(id);
        if (!optionalSentence.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Sentence sentence = optionalSentence.get();
        String word = sentence.getText();

        String englishSentence = huggingFaceService.generateSentence(word);
        String portugueseSentence = huggingFaceService.translateToPortuguese(englishSentence);

        Integer treino = sentence.getTreino() != null ? sentence.getTreino() : 0;
        sentence.setTreino(treino + 1);
        sentenceDataSource.save(sentence);

        Map<String, Object> response = new HashMap<>();
        response.put("englishSentence", englishSentence);
        response.put("portugueseSentence", portugueseSentence);
        response.put("treino", sentence.getTreino());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/random")
    public ResponseEntity<?> generateRandomSentenceAndTranslate() {
        List<Sentence> sentences = sentenceDataSource.findAll();
        if (sentences.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Random random = new Random();
        Sentence sentence = sentences.get(random.nextInt(sentences.size()));
        String word = sentence.getText();

        String englishSentence = huggingFaceService.generateSentence(word);

        if (englishSentence == null || englishSentence.trim().isEmpty() ||
                englishSentence.contains("Unable to generate sentence")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Não foi possível gerar a frase no momento. Tente novamente mais tarde.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }

        String portugueseSentence = huggingFaceService.translateToPortuguese(englishSentence);

        Integer treino = sentence.getTreino() != null ? sentence.getTreino() : 0;
        sentence.setTreino(treino + 1);
        sentenceDataSource.save(sentence);

        Map<String, Object> response = new HashMap<>();
        response.put("englishSentence", englishSentence);
        response.put("portugueseSentence", portugueseSentence);
        response.put("treino", sentence.getTreino());

        return ResponseEntity.ok(response);
    }
}