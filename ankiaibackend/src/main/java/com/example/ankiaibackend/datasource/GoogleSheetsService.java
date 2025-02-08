package com.example.ankiaibackend.datasource;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
// import com.google.api.client.json.gson.GsonFactory; // Alternativa se preferir evitar o warning de depreciação
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

@Service
public class GoogleSheetsService {

    /**
     * Retorna uma instância autenticada do serviço Google Sheets.
     * Certifique-se de que o arquivo 'credentials.json' esteja na pasta resources.
     */
    public Sheets getSheetsService() {
        try {
            var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // Você pode optar por usar GsonFactory para evitar o warning de depreciação:
            // final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            // Carrega as credenciais a partir do classpath
            InputStream credentialsStream = getClass().getResourceAsStream("/credentials.json");
            if (credentialsStream == null) {
                throw new RuntimeException("Arquivo 'credentials.json' não encontrado no classpath.");
            }

            // Carrega as credenciais e cria o escopo necessário para a API do Google Sheets
            GoogleCredential credentials = GoogleCredential.fromStream(credentialsStream)
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

            // GoogleCredential já implementa HttpRequestInitializer
            HttpRequestInitializer requestInitializer = credentials;

            return new Sheets.Builder(httpTransport, jsonFactory, requestInitializer)
                    .setApplicationName("AnkiaI Backend")
                    .build();

        } catch (GeneralSecurityException | java.io.IOException e) {
            throw new RuntimeException("Erro ao criar o serviço do Google Sheets", e);
        }
    }
}