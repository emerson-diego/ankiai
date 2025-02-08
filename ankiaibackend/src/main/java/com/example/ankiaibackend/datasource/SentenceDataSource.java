package com.example.ankiaibackend.datasource;

import java.util.List;
import java.util.Optional;

import com.example.ankiaibackend.model.Sentence;

public interface SentenceDataSource {
    Sentence save(Sentence sentence);

    List<Sentence> findAll();

    Optional<Sentence> findById(String id);
}