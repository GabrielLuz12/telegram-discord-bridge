package com.bridge.telegram;

import com.bridge.bridge.MessageBridgeService;
import com.bridge.config.AppConfig;
import com.bridge.telegram.TelegramModels.*;
import com.bridge.util.SimpleLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class TelegramBot {
    private final AppConfig config;
    private final MessageBridgeService bridgeService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private volatile long offset = 0;

    public TelegramBot(AppConfig config, MessageBridgeService bridgeService) {
        this.config = config;
        this.bridgeService = bridgeService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getHttpTimeoutSeconds()))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public void startPolling() {
        Thread thread = new Thread(this::pollLoop);
        thread.setName("telegram-polling-thread");
        thread.setDaemon(false);
        thread.start();

        SimpleLogger.info("Polling do Telegram iniciado.");
    }

    private void pollLoop() {
        while (true) {
            try {
                UpdateList updates = getUpdates();

                if (updates != null && updates.ok && updates.result != null) {
                    for (Update update : updates.result) {
                        offset = update.update_id + 1;

                        if (update.message == null) continue;
                        if (update.message.chat == null) continue;
                        if (update.message.chat.id != config.getTelegramChatId()) continue;

                        String text = update.message.text;
                        String caption = update.message.caption;

                        if (text != null && !text.isBlank()) {
                            bridgeService.handleTelegramText(text);
                        }

                        if (update.message.photo != null && !update.message.photo.isEmpty()) {
                            PhotoSize bestPhoto = update.message.photo.get(update.message.photo.size() - 1);
                            File image = downloadTelegramFile(bestPhoto.file_id, ".jpg");
                            bridgeService.handleTelegramFile(image, caption);
                        }

                        if (update.message.document != null) {
                            File document = downloadTelegramFile(
                                    update.message.document.file_id,
                                    "-" + safeName(update.message.document.file_name)
                            );
                            bridgeService.handleTelegramFile(document, caption);
                        }
                    }
                }

            } catch (Exception e) {
                SimpleLogger.error("Erro no polling do Telegram", e);
                sleepSilently(3000);
            }
        }
    }

    private UpdateList getUpdates() throws IOException, InterruptedException {
        String url = "https://api.telegram.org/bot" + config.getTelegramToken()
                + "/getUpdates?timeout=" + config.getPollTimeoutSeconds()
                + "&offset=" + offset;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(config.getPollTimeoutSeconds() + 10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Erro HTTP no getUpdates: " + response.statusCode() + " - " + response.body());
        }

        return objectMapper.readValue(response.body(), UpdateList.class);
    }

    private File downloadTelegramFile(String fileId, String suffix) throws IOException, InterruptedException {
        String getFileUrl = "https://api.telegram.org/bot" + config.getTelegramToken()
                + "/getFile?file_id=" + encode(fileId);

        HttpRequest getFileRequest = HttpRequest.newBuilder()
                .uri(URI.create(getFileUrl))
                .GET()
                .timeout(Duration.ofSeconds(config.getHttpTimeoutSeconds()))
                .build();

        HttpResponse<String> getFileResponse = httpClient.send(getFileRequest, HttpResponse.BodyHandlers.ofString());

        if (getFileResponse.statusCode() != 200) {
            throw new IOException("Erro ao obter arquivo do Telegram: " + getFileResponse.body());
        }

        FileResponse fileResponse = objectMapper.readValue(getFileResponse.body(), FileResponse.class);

        if (fileResponse == null || fileResponse.result == null || fileResponse.result.file_path == null) {
            throw new IOException("Telegram não retornou file_path.");
        }

        String downloadUrl = "https://api.telegram.org/file/bot" + config.getTelegramToken()
                + "/" + fileResponse.result.file_path;

        HttpRequest downloadRequest = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .GET()
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<byte[]> downloadResponse = httpClient.send(downloadRequest, HttpResponse.BodyHandlers.ofByteArray());

        if (downloadResponse.statusCode() != 200) {
            throw new IOException("Erro ao baixar arquivo do Telegram.");
        }

        File tempFile = File.createTempFile("telegram-", suffix);

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(downloadResponse.body());
        }

        tempFile.deleteOnExit();
        return tempFile;
    }

    public void sendMessage(String text) throws IOException, InterruptedException {
        String body = "chat_id=" + encode(String.valueOf(config.getTelegramChatId()))
                + "&text=" + encode(text);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.telegram.org/bot" + config.getTelegramToken() + "/sendMessage"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(config.getHttpTimeoutSeconds()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Erro HTTP no sendMessage: " + response.statusCode() + " - " + response.body());
        }
    }

    public void sendPhoto(File file, String caption) throws IOException, InterruptedException {
        String boundary = "----JavaBoundary" + System.currentTimeMillis();

        byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());

        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n"
                + config.getTelegramChatId() + "\r\n"
                + "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"caption\"\r\n\r\n"
                + (caption == null ? "" : caption) + "\r\n"
                + "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"photo\"; filename=\"" + file.getName() + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n";

        String footer = "\r\n--" + boundary + "--\r\n";

        byte[] body = concat(
                header.getBytes(StandardCharsets.UTF_8),
                fileBytes,
                footer.getBytes(StandardCharsets.UTF_8)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.telegram.org/bot" + config.getTelegramToken() + "/sendPhoto"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Erro HTTP no sendPhoto: " + response.statusCode() + " - " + response.body());
        }
    }

    private byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] arr : arrays) length += arr.length;

        byte[] result = new byte[length];
        int pos = 0;

        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, result, pos, arr.length);
            pos += arr.length;
        }

        return result;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String safeName(String name) {
        if (name == null || name.isBlank()) {
            return "arquivo";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void sleepSilently(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}