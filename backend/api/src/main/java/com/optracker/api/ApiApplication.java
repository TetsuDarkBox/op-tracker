package com.optracker.api;

import com.optracker.api.service.CardSyncService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner runOnStartup(CardSyncService cardSyncService) {
		return args -> {
			// Executamos numa Thread em segundo plano para a API ficar disponível de imediato!
			new Thread(() -> {
				try {
					System.out.println("🚀 [ARRANQUE] A iniciar verificação e sincronização do sistema...");
					cardSyncService.syncCards();
				} catch (Exception e) {
					System.err.println("❌ Erro durante o arranque automático: " + e.getMessage());
				}
			}).start();
		};
	}
}