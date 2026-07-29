package com.optracker.api.service;

import com.optracker.api.entity.Card;
import com.optracker.api.entity.CardVariant;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class ImageDownloaderService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private GitHubImageUploaderService gitHubUploaderService;

    private boolean isRunning = false;
    public boolean isRunning() { return isRunning; }

    private static class ImageTask {
        String sourceUrl;
        String targetFolder;
        String fileName;
        String repoPath; // Ex: OP-05/OP05-034_p1.png

        ImageTask(String sourceUrl, String targetFolder, String fileName) {
            this.sourceUrl = sourceUrl;
            this.targetFolder = targetFolder;
            this.fileName = fileName;
            this.repoPath = targetFolder + "/" + fileName;
        }
    }

    @Transactional(readOnly = true)
    public void downloadMissingImagesOnly() {
        this.isRunning = true;
        try {
            List<Card> cards = cardRepository.findAllWithVariants();
            List<ImageTask> tasks = new ArrayList<>();

            System.out.println("\n🔍 [SISTEMA] A mapear todas as variantes de imagens...");

            // 1. Mapear todas as imagens das cartas
            for (Card card : cards) {
                List<CardVariant> variants = card.getVariants();
                if (variants == null || variants.isEmpty()) continue;

                String code = card.getCode().trim();
                String targetSetFolder = extractSetFolderFromCode(code);

                for (CardVariant variant : variants) {
                    String sourceUrl = variant.getImageUrl();
                    if (sourceUrl == null || sourceUrl.trim().isEmpty()) continue;

                    String fileName = getFileNameFromUrl(sourceUrl);
                    if (fileName == null || fileName.isEmpty()) continue;

                    tasks.add(new ImageTask(sourceUrl, targetSetFolder, fileName));
                }
            }

            // =========================================================================
            // PASSO 1: VERIFICAÇÃO E UPLOAD PARA O GITHUB
            // =========================================================================
            System.out.println("\n☁️ [GITHUB] A verificar imagens em falta no repositório remoto...");

            List<ImageTask> githubMissingTasks = new ArrayList<>();
            for (ImageTask task : tasks) {
                if (!gitHubUploaderService.fileExistsOnGitHub(task.repoPath)) {
                    githubMissingTasks.add(task);
                }
            }

            int totalGithubMissing = githubMissingTasks.size();

            if (totalGithubMissing > 0) {
                System.out.println("📦 [GITHUB] Encontradas " + totalGithubMissing + " imagens em falta no repositório. A iniciar Upload...\n");
                int current = 0;
                for (ImageTask task : githubMissingTasks) {
                    current++;
                    int remaining = totalGithubMissing - current;

                    gitHubUploaderService.uploadToGitHub(task.sourceUrl, task.repoPath);
                    System.out.printf(" 📤 [%d/%d | Faltam %d] GitHub ➔ %s%n",
                            current, totalGithubMissing, remaining, task.repoPath);
                }
                System.out.println("\n✨ [GITHUB] Todos os uploads para o repositório foram concluídos!");
            } else {
                System.out.println("✅ [GITHUB] Repositório remoto já está 100% atualizado!");
            }

            // =========================================================================
            // PASSO 2: ESCOLHA INTERATIVA NO TERMINAL PARA DOWNLOAD LOCAL
            // =========================================================================
            System.out.println("\n-------------------------------------------------");
            System.out.print("👉 Desejas fazer o download de uma cópia local para o disco? (S/N): ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("S") || input.equalsIgnoreCase("SIM")) {
                System.out.println("\n💾 [DISCO LOCAL] A verificar imagens em falta na pasta do projeto...");
                Path baseFolderPath = getBaseFolderPath();

                List<ImageTask> localMissingTasks = new ArrayList<>();
                for (ImageTask task : tasks) {
                    Path setFolderPath = baseFolderPath.resolve(task.targetFolder);
                    Path fileDestination = setFolderPath.resolve(task.fileName);

                    if (!Files.exists(fileDestination)) {
                        localMissingTasks.add(task);
                    }
                }

                int totalLocalMissing = localMissingTasks.size();

                if (totalLocalMissing > 0) {
                    System.out.println("📦 [DISCO LOCAL] A descarregar " + totalLocalMissing + " imagens em falta...\n");
                    int current = 0;
                    for (ImageTask task : localMissingTasks) {
                        current++;
                        int remaining = totalLocalMissing - current;

                        Path setFolderPath = baseFolderPath.resolve(task.targetFolder);
                        Path fileDestination = setFolderPath.resolve(task.fileName);

                        if (!Files.exists(setFolderPath)) {
                            Files.createDirectories(setFolderPath);
                        }

                        downloadFile(task.sourceUrl, fileDestination.toString());
                        System.out.printf(" 📥 [%d/%d | Faltam %d] Disco ➔ %s/%s%n",
                                current, totalLocalMissing, remaining, task.targetFolder, task.fileName);
                    }
                    System.out.println("\n🎉 [DISCO LOCAL] Download local concluído com sucesso!");
                } else {
                    System.out.println("✅ [DISCO LOCAL] Todas as imagens já existem no disco!");
                }

            } else {
                System.out.println("⏩ [DISCO LOCAL] Download local ignorado pelo utilizador.");
            }

            System.out.println("\n🏁 [PROCESSO CONCLUÍDO] Tudo finalizado!\n");

        } catch (Exception e) {
            System.err.println("❌ Erro no Processamento de Imagens: " + e.getMessage());
        } finally {
            this.isRunning = false;
        }
    }

    private String getFileNameFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        String cleanUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
        return cleanUrl.substring(cleanUrl.lastIndexOf('/') + 1);
    }

    private String extractSetFolderFromCode(String code) {
        if (code != null && code.contains("-")) {
            String prefix = code.split("-")[0].toUpperCase();
            if (prefix.matches("^[A-Z]+[0-9]+$")) {
                String letters = prefix.replaceAll("[0-9]", "");
                String numbers = prefix.replaceAll("[^0-9]", "");
                return letters + "-" + numbers;
            }
            return prefix;
        }
        return "Promos";
    }

    private Path getBaseFolderPath() {
        Path executionPath = Paths.get("").toAbsolutePath();
        Path targetPath = executionPath;

        while (targetPath != null && !targetPath.getFileName().toString().equals("op-card-tracker")) {
            if (Files.exists(targetPath.resolve("frontend"))) {
                break;
            }
            targetPath = targetPath.getParent();
        }

        if (targetPath == null) {
            targetPath = executionPath;
        }

        return targetPath.resolve("frontend/src/assets/cards/");
    }

    private void downloadFile(String urlStr, String destination) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(urlStr).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(destination)) {
            byte[] dataBuffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 2048)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            System.err.println("⚠️ Falha ao descarregar URL (" + urlStr + "): " + e.getMessage());
        }
    }
}