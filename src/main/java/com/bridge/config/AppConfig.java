package com.bridge.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final String discordToken;
    private final long discordChannelId;
    private final String telegramToken;
    private final long telegramChatId;
    private final int pollTimeoutSeconds;
    private final int httpTimeoutSeconds;

    public AppConfig(
            String discordToken,
            long discordChannelId,
            String telegramToken,
            long telegramChatId,
            int pollTimeoutSeconds,
            int httpTimeoutSeconds
    ) {
        this.discordToken = discordToken;
        this.discordChannelId = discordChannelId;
        this.telegramToken = telegramToken;
        this.telegramChatId = telegramChatId;
        this.pollTimeoutSeconds = pollTimeoutSeconds;
        this.httpTimeoutSeconds = httpTimeoutSeconds;
    }

    public static AppConfig load() throws IOException {
        Properties props = new Properties();

        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new IOException("Arquivo application.properties não encontrado.");
            }
            props.load(in);
        }

        String discordToken = required(props, "discord.token");
        long discordChannelId = Long.parseLong(required(props, "discord.channelId"));
        String telegramToken = required(props, "telegram.token");
        long telegramChatId = Long.parseLong(required(props, "telegram.chatId"));
        int pollTimeoutSeconds = Integer.parseInt(props.getProperty("bridge.pollTimeoutSeconds", "30"));
        int httpTimeoutSeconds = Integer.parseInt(props.getProperty("bridge.httpTimeoutSeconds", "20"));

        return new AppConfig(
                discordToken,
                discordChannelId,
                telegramToken,
                telegramChatId,
                pollTimeoutSeconds,
                httpTimeoutSeconds
        );
    }

    private static String required(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Configuração obrigatória ausente: " + key);
        }
        return value.trim();
    }

    public String getDiscordToken() {
        return discordToken;
    }

    public long getDiscordChannelId() {
        return discordChannelId;
    }

    public String getTelegramToken() {
        return telegramToken;
    }

    public long getTelegramChatId() {
        return telegramChatId;
    }

    public int getPollTimeoutSeconds() {
        return pollTimeoutSeconds;
    }

    public int getHttpTimeoutSeconds() {
        return httpTimeoutSeconds;
    }
}