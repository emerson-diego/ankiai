package com.example.ankiaibackend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.ankiaibackend.model.Sentence;

public interface SentenceRepository extends MongoRepository<Sentence, String> {

}