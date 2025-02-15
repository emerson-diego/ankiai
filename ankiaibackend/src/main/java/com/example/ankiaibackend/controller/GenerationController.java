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

import com.example.ankiaibackend.datasource.GoogleSheetsService;
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

    // Serviço para integração com a planilha do Google
    @Autowired
    private GoogleSheetsService googleSheetService;

    /**
     * Gera uma frase (em inglês) com base na sentença encontrada pelo ID e a traduz
     * para o português.
     * Após gerar a frase, incrementa o contador de treino e atualiza a planilha do
     * Google.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> generateSentenceAndTranslate(@PathVariable("id") String id) {
        Optional<Sentence> optionalSentence = sentenceDataSource.findById(id);
        if (!optionalSentence.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Sentence sentence = optionalSentence.get();
        String palavra = sentence.getText();

        String englishSentence = huggingFaceService.generateSentence(palavra);
        String portugueseSentence = huggingFaceService.translateToPortuguese(englishSentence);

        // Incrementa o treino e salva na fonte de dados
        Integer treino = sentence.getTreino() != null ? sentence.getTreino() : 0;
        sentence.setTreino(treino + 1);
        //sentenceDataSource.save(sentence);

        // Atualiza a planilha do Google se necessário
        try {
            //googleSheetService.atualizarTreino(sentence);
        } catch (Exception e) {
            // Caso ocorra erro na integração com a planilha, loga a exceção e segue o fluxo
            // normalmente
            System.err.println("Erro ao atualizar a planilha: " + e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("englishSentence", englishSentence);
        response.put("portugueseSentence", portugueseSentence);
        //response.put("treino", sentence.getTreino());

        return ResponseEntity.ok(response);
    }

    /**
     * Seleciona uma sentença aleatória, gera uma frase com base nela, traduz para o
     * português
     * e atualiza o contador de treino tanto na fonte de dados quanto na planilha do
     * Google.
     */
    @GetMapping("/random")
    public ResponseEntity<?> generateRandomSentenceAndTranslate() {
        List<Sentence> sentences = sentenceDataSource.findAll();
        if (sentences.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Random random = new Random();
        Sentence sentence = sentences.get(random.nextInt(sentences.size()));
        String palavra = sentence.getText();

        String englishSentence = huggingFaceService.generateSentence(palavra);

        if (englishSentence == null || englishSentence.trim().isEmpty() ||
                englishSentence.contains("Unable to generate sentence")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Não foi possível gerar a frase no momento. Tente novamente mais tarde.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }

        String portugueseSentence = huggingFaceService.translateToPortuguese(englishSentence);

        // Incrementa o treino e salva na fonte de dados
        Integer treino = sentence.getTreino() != null ? sentence.getTreino() : 0;
        sentence.setTreino(treino + 1);
        //sentenceDataSource.save(sentence);

        // Atualiza a planilha do Google se necessário
        try {
            googleSheetService.atualizarTreino(sentence);
        } catch (Exception e) {
            System.err.println("Erro ao atualizar a planilha: " + e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("sentenca", palavra);
        response.put("englishSentence", englishSentence);
        response.put("portugueseSentence", portugueseSentence);
        response.put("treino", sentence.getTreino());

        return ResponseEntity.ok(response);
    }
}