package com.optracker.api.controller;

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

    @GetMapping("/sync-images")
    public String syncImages() {
        // Criamos a thread aqui para o download não bloquear a resposta do browser
        new Thread(() -> {
            try {
                imageDownloaderService.downloadAllImages();
            } catch (Exception e) {
                System.err.println("❌ Erro na Thread de download: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        return "⚓ Navio lançado! Verifica a consola do IntelliJ para veres as imagens a entrar.";
    }
}