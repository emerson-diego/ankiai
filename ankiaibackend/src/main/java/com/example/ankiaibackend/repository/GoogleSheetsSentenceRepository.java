package com.example.ankiaibackend.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.ankiaibackend.datasource.GoogleSheetsService;
import com.example.ankiaibackend.datasource.SentenceDataSource;
import com.example.ankiaibackend.model.Sentence;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

@Component
@ConditionalOnProperty(name = "app.datasource", havingValue = "google", matchIfMissing = true)
public class GoogleSheetsSentenceRepository implements SentenceDataSource {

    private final Sheets sheetsService;
    private final String spreadsheetId;
    private final String range;

    @Autowired
    public GoogleSheetsSentenceRepository(GoogleSheetsService googleSheetsService,
            @Value("${google.sheets.spreadsheetId}") String spreadsheetId,
            @Value("${google.sheets.range}") String range) {
        this.sheetsService = googleSheetsService.getSheetsService();
        this.spreadsheetId = spreadsheetId;
        this.range = range;
    }

    @Override
    public Sentence save(Sentence sentence) {
        try {
            // Se não houver ID, adiciona uma nova linha
            if (sentence.getId() == null || sentence.getId().isEmpty()) {
                List<Object> rowData = Arrays.asList(
                        (Object) sentence.getText(),
                        (Object) sentence.getTipo(),
                        (Object) (sentence.getTreino() != null ? sentence.getTreino().toString() : "0"));
                List<List<Object>> values = Collections.singletonList(rowData);
                ValueRange body = new ValueRange().setValues(values);
                sheetsService.spreadsheets().values()
                        .append(spreadsheetId, range, body)
                        .setValueInputOption("USER_ENTERED")
                        .execute();
                // Gerar um ID fictício para a nova sentença
                sentence.setId(UUID.randomUUID().toString());
            } else {
                // Atualização: implementação ilustrativa (para atualizar, seria necessário
                // mapear o ID para a linha correta)
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar a sentença no Google Sheets", e);
        }
        return sentence;
    }

    @Override
    public List<Sentence> findAll() {
        List<Sentence> sentences = new ArrayList<>();
        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                return sentences;
            }
            int rowNumber = 0;
            for (List<Object> row : values) {
                // Se a primeira linha for cabeçalho, pular
                if (rowNumber == 0 && row.size() > 0 && row.get(0).toString().equalsIgnoreCase("Sentença")) {
                    rowNumber++;
                    continue;
                }
                Sentence sentence = new Sentence();
                // Atribui um ID fictício (pode ser o número da linha)
                sentence.setId(String.valueOf(rowNumber));
                sentence.setText(row.size() > 0 ? row.get(0).toString() : "");
                sentence.setTipo(row.size() > 1 ? row.get(1).toString() : "");
                // Utiliza a coluna 'Pontuação' como treino
                sentence.setTreino(row.size() > 2 ? Integer.parseInt(row.get(2).toString()) : 0);
                sentences.add(sentence);
                rowNumber++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar as sentenças do Google Sheets", e);
        }
        return sentences;
    }

    @Override
    public Optional<Sentence> findById(String id) {
        return findAll().stream()
                .filter(sentence -> sentence.getId().equals(id))
                .findFirst();
    }
}
