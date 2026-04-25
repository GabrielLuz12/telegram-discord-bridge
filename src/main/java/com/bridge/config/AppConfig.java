package com.bridge.config;

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

    public static AppConfig load() {
        String discordToken = requiredEnv("DISCORD_TOKEN");
        long discordChannelId = Long.parseLong(requiredEnv("DISCORD_CHANNEL_ID"));

        String telegramToken = requiredEnv("TELEGRAM_TOKEN");
        long telegramChatId = Long.parseLong(requiredEnv("TELEGRAM_CHAT_ID"));

        int pollTimeoutSeconds = Integer.parseInt(optionalEnv("POLL_TIMEOUT_SECONDS", "30"));
        int httpTimeoutSeconds = Integer.parseInt(optionalEnv("HTTP_TIMEOUT_SECONDS", "20"));

        return new AppConfig(
                discordToken,
                discordChannelId,
                telegramToken,
                telegramChatId,
                pollTimeoutSeconds,
                httpTimeoutSeconds
        );
    }

    private static String requiredEnv(String key) {
        String value = System.getenv(key);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Variável de ambiente obrigatória ausente: " + key);
        }

        return value.trim();
    }

    private static String optionalEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
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