package com.bridge;

import com.bridge.bridge.MessageBridgeService;
import com.bridge.config.AppConfig;
import com.bridge.discord.DiscordBot;
import com.bridge.telegram.TelegramBot;
import com.bridge.util.SimpleLogger;

public class Main {
    public static void main(String[] args) {
        try {
            AppConfig config = AppConfig.load();
            MessageBridgeService bridgeService = new MessageBridgeService();

            DiscordBot discordBot = new DiscordBot(config, bridgeService);
            TelegramBot telegramBot = new TelegramBot(config, bridgeService);

            bridgeService.setDiscordBot(discordBot);
            bridgeService.setTelegramBot(telegramBot);

            discordBot.start();
            telegramBot.startPolling();

            SimpleLogger.info("Bridge iniciada com sucesso.");
        } catch (Exception e) {
            SimpleLogger.error("Falha ao iniciar a aplicação", e);
        }
    }
}