package com.example.ankiaibackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ankiaibackend.model.Sentence;
import com.example.ankiaibackend.repository.SentenceRepository;

@RestController
@RequestMapping("/sentences")
public class SentenceController {

    @Autowired
    private SentenceRepository sentenceRepository;

    // Endpoint para inserir uma nova sentença
    @PostMapping
    public ResponseEntity<Sentence> createSentence(@RequestBody Sentence sentence) {
        Sentence newSentence = sentenceRepository.save(sentence);
        return ResponseEntity.ok(newSentence);
    }

    // Endpoint para recuperar todas as sentenças
    @GetMapping
    public ResponseEntity<List<Sentence>> getAllSentences() {
        List<Sentence> sentences = sentenceRepository.findAll();
        return ResponseEntity.ok(sentences);
    }
}
