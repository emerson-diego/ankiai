package com.example.ankiaibackend.datasource;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.ankiaibackend.model.Sentence;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
// import com.google.api.client.json.gson.GsonFactory; // Se preferir evitar warning de depreciação
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

@Service
public class GoogleSheetsService {

    // ID da planilha definido no application.properties, por exemplo:
    // google.sheets.spreadsheet-id=1xT5n7TcwJJ36_tuRoeNp7jN7wTWvt9OOuIeGvLk4PhE
    @Value("${google.sheets.spreadsheet-id}")
    private String spreadsheetId;

    /**
     * Retorna uma instância autenticada do serviço Google Sheets.
     * Certifique-se de que o arquivo 'credentials.json' esteja na pasta resources.
     */
    public Sheets getSheetsService() {
        try {
            var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            // Carrega as credenciais a partir do classpath
            InputStream credentialsStream = getClass().getResourceAsStream("/credentials.json");
            if (credentialsStream == null) {
                throw new RuntimeException("Arquivo 'credentials.json' não encontrado no classpath.");
            }

            // Carrega as credenciais e cria o escopo necessário para a API do Google Sheets
            GoogleCredential credentials = GoogleCredential.fromStream(credentialsStream)
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

            return new Sheets.Builder(httpTransport, jsonFactory, credentials)
                    .setApplicationName("AnkiaI Backend")
                    .build();

        } catch (GeneralSecurityException | java.io.IOException e) {
            throw new RuntimeException("Erro ao criar o serviço do Google Sheets", e);
        }
    }

    /**
     * Atualiza a contagem de treino (na aba DIC_REAL, coluna C) de uma sentença na
     * planilha do Google.
     *
     * @param sentence A sentença cuja contagem de treino será atualizada.
     * @throws IllegalArgumentException se o rowNumber não estiver definido na
     *                                  sentença.
     * @throws RuntimeException         se ocorrer erro na comunicação com a API do
     *                                  Google Sheets.
     */
    public void atualizarTreino(Sentence sentence) {
        if (sentence.getRowNumber() == null) {
            throw new IllegalArgumentException(
                    "O número da linha (rowNumber) não está definido para a sentença com id: "
                            + sentence.getId());
        }

        Sheets sheetsService = getSheetsService();
        // Define o intervalo na planilha; aqui, supomos que a contagem de treino está
        // na coluna C da aba DIC_REAL
        String range = String.format("DIC_REAL!C%d", sentence.getRowNumber());

        // Prepara os valores a serem atualizados (aqui, uma lista contendo o novo valor
        // de treino)
        List<List<Object>> values = List.of(List.of(sentence.getTreino()));
        ValueRange body = new ValueRange().setValues(values);

        try {
            UpdateValuesResponse response = sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();

            if (response.getUpdatedCells() == null || response.getUpdatedCells() == 0) {
                throw new RuntimeException("Nenhuma célula foi atualizada na planilha.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar a planilha: " + e.getMessage(), e);
        }
    }
}