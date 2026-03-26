package com.optracker.api.service;

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

    // Caminho relativo (Funciona se o projeto for corrido na pasta 'backend/api')
    private static final String RELATIVE_PATH = "src/main/resources/data";

    public void syncCards() {
        System.out.println("🔄 A VERIFICAR ATUALIZAÇÕES DE CARTAS...");

        // Tenta descobrir o caminho absoluto correto
        File dataDir = new File(RELATIVE_PATH).getAbsoluteFile();

        System.out.println("📂 Pasta de Destino: " + dataDir.getAbsolutePath());

        try {
            // 1. Limpeza (Opcional: Podes comentar isto se quiseres ser mais rápido e só substituir)
            if (dataDir.exists()) {
                System.out.println("🧹 A limpar ficheiros antigos para garantir atualização total...");
                deleteDirectory(dataDir.toPath());
            }
            dataDir.mkdirs();

            // 2. Download e Extração
            System.out.println("⬇️ A baixar dados do GitHub...");
            downloadAndExtract(ZIP_URL, dataDir);

            System.out.println("✅ SINCRONIZAÇÃO CONCLUÍDA! As cartas estão atualizadas.");

        } catch (Exception e) {
            System.err.println("❌ Falha na sincronização: " + e.getMessage());
            System.err.println("⚠️ A aplicação vai tentar usar os ficheiros que já existirem.");
        }
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
            System.out.println("📦 Extraídos " + count + " ficheiros.");
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