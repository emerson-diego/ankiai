package com.example.ankiaibackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "sentences")
@Data
public class Sentence {

    @Id
    private String id;
    private String text;
    private String tipo;
    private Integer treino;

    public Sentence() {
    }

    public Sentence(String text) {
        this.text = text;
    }

}
