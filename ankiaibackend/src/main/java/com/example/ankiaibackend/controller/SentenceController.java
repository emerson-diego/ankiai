package com.example.ankiaibackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ankiaibackend.datasource.SentenceDataSource;
import com.example.ankiaibackend.model.Sentence;
import com.example.ankiaibackend.service.SentenceClassificationService;

@RestController
@RequestMapping("/sentences")
public class SentenceController {

    @Autowired
    private SentenceDataSource sentenceDataSource;

    @Autowired
    private SentenceClassificationService classificationService;

    // Endpoint para inserir uma nova sentença
    @PostMapping
    public ResponseEntity<Sentence> createSentence(@RequestBody Sentence sentence) {
        String tipoClassificacao = classificationService.classificar(sentence.getText());
        sentence.setTipo(tipoClassificacao);
        sentence.setTreino(0);
        Sentence newSentence = sentenceDataSource.save(sentence);
        return ResponseEntity.ok(newSentence);
    }

    // Endpoint para recuperar todas as sentenças
    @GetMapping
    public ResponseEntity<List<Sentence>> getAllSentences() {
        List<Sentence> sentences = sentenceDataSource.findAll();
        return ResponseEntity.ok(sentences);
    }
}