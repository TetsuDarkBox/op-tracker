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
    public void run(String... args) throws Exception {
        System.out.println("🚀 A VERIFICAR A BASE DE DADOS...");

        List<Card> newCards = bandaiScraperService.scrapeOfficialSite();

        if (newCards != null && !newCards.isEmpty()) {
            System.out.println("⏳ A gravar " + newCards.size() + " cartas no TiDB Cloud...");
            cardRepository.saveAll(newCards);
            System.out.println("🏁 SUCESSO! Base de dados atualizada.");
        }
    }
}