package com.optracker.api.service;

import com.optracker.api.entity.Card;
import com.optracker.api.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ImageDownloaderService {

    @Autowired
    private CardRepository cardRepository;

    private int totalCards = 0;
    private int currentProgress = 0;
    private boolean isRunning = false;

    public int getTotalCards() { return totalCards; }
    public int getCurrentProgress() { return currentProgress; }
    public boolean isRunning() { return isRunning; }

    @Transactional(readOnly = true)
    public void downloadAllImages() {
        this.isRunning = true;
        this.currentProgress = 0;

        try {
            // 1. Encontrar a raiz do projeto e o destino no Frontend
            Path executionPath = Paths.get("").toAbsolutePath();
            Path targetPath = executionPath;
            while (targetPath != null && !targetPath.getFileName().toString().equals("op-card-tracker")) {
                targetPath = targetPath.getParent();
            }

            Path baseFolderPath = targetPath.resolve("frontend/src/assets/cards/");

            List<Card> cards = cardRepository.findAllWithVariants();
            this.totalCards = cards.size();

            System.out.println("📍 Destino Base: " + baseFolderPath.toAbsolutePath());

            for (Card card : cards) {
                // Tenta obter o ID do Set (ex: OP01, ST10) através da variante
                String setId = "Unknown";
                if (card.getVariants() != null && !card.getVariants().isEmpty() &&
                        card.getVariants().get(0).getCardSet() != null) {
                    setId = card.getVariants().get(0).getCardSet().getSetId();
                }

                // 2. Criar o caminho da subpasta do Set (ex: .../assets/cards/OP01/)
                Path setFolderPath = baseFolderPath.resolve(setId);
                if (!Files.exists(setFolderPath)) {
                    Files.createDirectories(setFolderPath);
                }

                String sourceUrl = (card.getVariants() != null && !card.getVariants().isEmpty())
                        ? card.getVariants().get(0).getImageUrl()
                        : null;

                if (sourceUrl != null && !sourceUrl.isEmpty()) {
                    String fileName = card.getCode().replaceAll("[\\\\/:*?\"<>|]", "-") + ".png";
                    Path fileDestination = setFolderPath.resolve(fileName);

                    if (!Files.exists(fileDestination)) {
                        downloadFile(sourceUrl, fileDestination.toString());
                    }
                }

                this.currentProgress++;
                if (currentProgress % 50 == 0 || currentProgress == totalCards) {
                    System.out.printf("🚢 [%s] Progresso: %d/%d%n", setId, currentProgress, totalCards);
                }
            }
            System.out.println("🏴‍☠️ SAQUE ORGANIZADO CONCLUÍDO!");

        } catch (Exception e) {
            System.err.println("❌ ERRO: " + e.getMessage());
        } finally {
            this.isRunning = false;
        }
    }

    private void downloadFile(String urlStr, String destination) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(urlStr).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(destination)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // Ignora falhas de download individuais
        }
    }
}