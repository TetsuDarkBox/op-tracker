package com.optracker.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class CardSyncService {

    private static final String ZIP_URL = "https://github.com/buhbbl/punk-records/archive/refs/heads/main.zip";
    private static final String RELATIVE_PATH = "src/main/resources/data";

    @Autowired
    private BandaiScraperService bandaiScraperService;

    @Autowired
    private ImageDownloaderService imageDownloaderService;

    // Método principal de Sincronização Global
    public void syncCards() {
        System.out.println("🔄 A INICIAR SINCRONIZAÇÃO COMPLETA E INTELIGENTE DO SISTEMA...");

        // 1. PRIMÁRIO: Scraping inteligente direto do site da Bandai
        try {
            System.out.println("🏴‍☠️ [PASSO 1/3] A verificar e recolher dados oficiais da Bandai...");
            bandaiScraperService.scrapeOfficialSite();
        } catch (Exception e) {
            System.err.println("🚨 Erro no scraping da Bandai: " + e.getMessage());
        }

        // 2. PRIMÁRIO: Download RÁPIDO das Imagens em falta no disco
        try {
            System.out.println("🖼️ [PASSO 2/3] A verificar e descarregar imagens/variantes em falta...");
            imageDownloaderService.downloadMissingImagesOnly(); // 👈 Usamos o método otimizado!
        } catch (Exception e) {
            System.err.println("🚨 Erro no download das imagens: " + e.getMessage());
        }

        // 3. SECUNDÁRIO: Download de dados auxiliares/JSONs do GitHub (Punk-Records)
        try {
            System.out.println("⬇️ [PASSO 3/3] A atualizar cópia de segurança local do GitHub...");
            File dataDir = new File(RELATIVE_PATH).getAbsoluteFile();
            if (dataDir.exists()) {
                deleteDirectory(dataDir.toPath());
            }
            dataDir.mkdirs();
            downloadAndExtract(ZIP_URL, dataDir);
        } catch (Exception e) {
            System.err.println("⚠️ Falha na sincronização secundária do GitHub (não afeta as imagens/BD): " + e.getMessage());
        }

        System.out.println("✅ SINCRONIZAÇÃO E DOWNLOADS FINALIZADOS COM SUCESSO!");
    }

    private void downloadAndExtract(String urlStr, File destDir) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (ZipInputStream zis = new ZipInputStream(conn.getInputStream())) {
            ZipEntry entry;
            int count = 0;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                boolean isPack = name.endsWith("english/packs.json");
                boolean isCardContent = name.contains("english/cards/") && name.endsWith(".json");

                if (isPack || isCardContent) {
                    String localName;
                    if (isPack) {
                        localName = "packs.json";
                    } else {
                        int index = name.indexOf("cards/");
                        if (index == -1) continue;
                        localName = name.substring(index + 6);
                        if (localName.isEmpty()) continue;
                    }

                    File file = new File(destDir, localName);
                    if (entry.isDirectory()) {
                        file.mkdirs();
                    } else {
                        new File(file.getParent()).mkdirs();
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);
                        }
                        count++;
                    }
                }
            }
            System.out.println("📦 Extraídos " + count + " ficheiros do GitHub.");
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}