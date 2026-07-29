package com.optracker.api.controller;

import com.optracker.api.service.CardSyncService;
import com.optracker.api.service.ImageDownloaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private ImageDownloaderService imageDownloaderService;

    @Autowired
    private CardSyncService cardSyncService;

    // 🖼️ 1. Endpoint para verificar e descarregar apenas as imagens locais em falta
    @GetMapping("/sync-images")
    public String syncImages() {
        new Thread(() -> {
            try {
                imageDownloaderService.downloadMissingImagesOnly();
            } catch (Exception e) {
                System.err.println("❌ Erro na Thread de download de imagens: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        return "⚓ Verificação de imagens em falta iniciada! Acompanha a consola do servidor.";
    }

    // ⚡ 2. Endpoint para forçar verificação completa de Novos Sets no site da Bandai + Download
    @GetMapping("/force-sync")
    public String forceSync() {
        new Thread(() -> {
            try {
                cardSyncService.syncCards();
            } catch (Exception e) {
                System.err.println("❌ Erro na Thread de sincronização completa: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        return "🏴‍☠️ Sincronização inteligente com a Bandai iniciada! Acompanha a consola para ver o progresso dos Sets.";
    }
}