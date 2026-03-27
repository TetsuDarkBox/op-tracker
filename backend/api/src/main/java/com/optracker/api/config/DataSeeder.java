package com.optracker.api.config;

import com.optracker.api.entity.Card;
import com.optracker.api.repository.CardRepository;
import com.optracker.api.service.BandaiScraperService;
import com.optracker.api.service.CardSyncService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CardRepository cardRepository;

    // TEMOS OS DOIS SERVIÇOS AQUI!
    private final CardSyncService cardSyncBackupService;
    private final BandaiScraperService bandaiScraperService;

    public DataSeeder(CardRepository cardRepository, CardSyncService cardSyncBackupService, BandaiScraperService bandaiScraperService) {
        this.cardRepository = cardRepository;
        this.cardSyncBackupService = cardSyncBackupService;
        this.bandaiScraperService = bandaiScraperService;
    }

    @Override
    public void run(String... args) {
        long cardCount = cardRepository.count();

        if (cardCount > 0) {
            System.out.println("✅ " + cardCount + " cartas encontradas. API disponível de imediato!");
            // Opcional: podes chamar o scraper aqui na mesma para atualizar,
            // mas como é @Async, ele não vai bloquear o arranque.
            return;
        }

        System.out.println("🚀 Base de dados vazia. A iniciar scraper em background...");
        bandaiScraperService.scrapeOfficialSite();
    }
}