package com.optracker.api.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CardUpdater {

    // Link oficial do Punk Records (Branch Main)
    private static final String ZIP_URL = "https://github.com/buhbbl/punk-records/archive/refs/heads/main.zip";

    // ✅ CAMINHO CORRIGIDO (Aponta para dentro de backend/api)
    private static final String TARGET_DIR = "C:\\Users\\diogo.pinto\\Desktop\\pessoal\\appTCG\\op-card-tracker\\backend\\api\\src\\main\\resources\\data";

    public static void main(String[] args) {
        System.out.println("🚀 INICIANDO ATUALIZADOR DE CARTAS...");
        System.out.println("📂 Destino: " + TARGET_DIR);

        File dataDir = new File(TARGET_DIR);

        try {
            // 1. Limpar a pasta antiga (para não misturar dados velhos)
            if (dataDir.exists()) {
                System.out.println("🧹 A limpar ficheiros antigos...");
                deleteDirectory(dataDir.toPath());
            }
            // Criar a pasta nova
            dataDir.mkdirs();

            // 2. Baixar o ZIP e extrair apenas o necessário
            System.out.println("⬇️ A baixar ZIP do GitHub (aguarda)...");
            downloadAndExtract(ZIP_URL, dataDir);

            System.out.println("✅ SUCESSO! Ficheiros guardados corretamente.");
            System.out.println("👉 IMPORTANTE: Clica com o botão direito na pasta 'resources' no IntelliJ e escolhe 'Reload from Disk' para veres as pastas.");

        } catch (Exception e) {
            System.err.println("❌ ERRO CRÍTICO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void downloadAndExtract(String urlStr, File destDir) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Abre o ZIP diretamente da internet
        try (ZipInputStream zis = new ZipInputStream(conn.getInputStream())) {
            ZipEntry entry;
            int count = 0;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                // LÓGICA DE FILTRO:
                // Queremos: "english/packs.json" E tudo dentro de "english/cards/"
                boolean isPack = name.endsWith("english/packs.json");
                boolean isCardContent = name.contains("english/cards/");

                if (isPack || isCardContent) {
                    String localName;

                    if (isPack) {
                        localName = "packs.json";
                    } else {
                        // Remove o lixo do caminho para ficar: "569101/OP01-001.json"
                        int index = name.indexOf("cards/");
                        if (index == -1) continue; // Segurança
                        localName = name.substring(index + 6); // Pega o que está depois de cards/

                        // Se for a própria pasta "cards/" vazia, ignora
                        if (localName.isEmpty()) continue;
                    }

                    File file = new File(destDir, localName);

                    if (entry.isDirectory()) {
                        file.mkdirs();
                    } else {
                        // Garante que a pasta pai existe antes de escrever o ficheiro
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

    // Utilitário para apagar pastas cheias
    private static void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}