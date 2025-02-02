package com.example.ankiaibackend.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.ankiaibackend.model.Sentence;
import com.example.ankiaibackend.repository.SentenceRepository;

@Service
public class BatchSentenceService {

    private final SentenceRepository sentenceRepository;
    private final SentenceClassificationService classificationService;

    public BatchSentenceService(SentenceRepository sentenceRepository,
            SentenceClassificationService classificationService) {
        this.sentenceRepository = sentenceRepository;
        this.classificationService = classificationService;
    }

    public void processarArquivo(String caminhoArquivo) {
        try {
            // Obtém o arquivo dentro da pasta resources
            ClassPathResource resource = new ClassPathResource(caminhoArquivo);
            InputStream inputStream = resource.getInputStream();

            // Lê o conteúdo do arquivo
            List<String> linhas = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .lines().collect(Collectors.toList());

            // Deleta todos os dados existentes
            sentenceRepository.deleteAll();
            System.out.println("Todos os dados foram deletados da coleção.");

            for (String linha : linhas) {
                String[] tokens = linha.split(",");
                for (String token : tokens) {
                    String texto = token.trim();
                    if (!texto.isEmpty()) {
                        String tipo = classificationService.classificar(texto);
                        Sentence sentence = new Sentence();
                        sentence.setText(texto);
                        sentence.setTipo(tipo);
                        sentenceRepository.save(sentence);
                        System.out.println("Inserida sentença: " + texto + " -> Tipo: " + tipo);
                    }
                }
            }
            System.out.println("Processamento concluído com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}