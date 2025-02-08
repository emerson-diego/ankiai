package com.example.ankiaibackend.datasource;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.ankiaibackend.model.Sentence;
import com.example.ankiaibackend.repository.SentenceRepository;

@Component
@ConditionalOnProperty(name = "app.datasource", havingValue = "mongodb")
public class MongoSentenceDataSource implements SentenceDataSource {

    @Autowired
    private SentenceRepository sentenceRepository;

    @Override
    public Sentence save(Sentence sentence) {
        return sentenceRepository.save(sentence);
    }

    @Override
    public List<Sentence> findAll() {
        return sentenceRepository.findAll();
    }

    @Override
    public Optional<Sentence> findById(String id) {
        return sentenceRepository.findById(id);
    }
}
