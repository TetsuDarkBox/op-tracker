package com.optracker.api.service;

import org.springframework.stereotype.Service;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Service
public class GitHubImageUploaderService {

    // 🔒 Configurações do teu Repositório GitHub
    private static final String GITHUB_TOKEN = "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";
    private static final String REPO_OWNER = "TetsuDarkBox";
    private static final String REPO_NAME = "op-tracker";
    private static final String BRANCH = "master";


    public boolean fileExistsOnGitHub(String targetPathInRepo) {
        try {
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s",
                    REPO_OWNER, REPO_NAME, targetPathInRepo, BRANCH);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + GITHUB_TOKEN)
                    .header("Accept", "application/vnd.github+json")
                    .GET()
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Descarrega os bytes da Bandai e envia para o GitHub
     */
    public String uploadToGitHub(String sourceUrl, String targetPathInRepo) {
        try {
            byte[] imageBytes = fetchBytesFromUrl(sourceUrl);
            if (imageBytes == null) return null;

            String base64Content = Base64.getEncoder().encodeToString(imageBytes);
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/contents/%s",
                    REPO_OWNER, REPO_NAME, targetPathInRepo);

            String jsonPayload = String.format("""
                {
                  "message": "Upload automatico: %s",
                  "content": "%s",
                  "branch": "%s"
                }
                """, targetPathInRepo, base64Content, BRANCH);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + GITHUB_TOKEN)
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return String.format("https://raw.githubusercontent.com/%s/%s/%s/%s",
                        REPO_OWNER, REPO_NAME, BRANCH, targetPathInRepo);
            } else {
                System.err.println("⚠️ Erro no GitHub Upload (" + response.statusCode() + "): " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Exceção ao enviar para o GitHub: " + e.getMessage());
        }
        return null;
    }

    private byte[] fetchBytesFromUrl(String urlStr) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(urlStr).openStream());
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = in.read(buffer, 0, 2048)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}