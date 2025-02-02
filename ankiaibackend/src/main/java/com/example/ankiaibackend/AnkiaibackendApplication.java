package com.example.ankiaibackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnkiaibackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnkiaibackendApplication.class, args);
	}

	// @Bean
	// public CommandLineRunner runner(BatchSentenceService batchSentenceService) {
	// return args -> {
	// // Arquivo agora é carregado corretamente do classpath
	// String caminhoArquivo = "arquivo.txt"; // Não precisa mudar, ele já será
	// encontrado na pasta resources
	// batchSentenceService.processarArquivo(caminhoArquivo);
	// };
	// }

}
