package com.bridge;

import com.bridge.bridge.MessageBridgeService;
import com.bridge.config.AppConfig;
import com.bridge.discord.DiscordBot;
import com.bridge.telegram.TelegramBot;
import com.bridge.util.SimpleLogger;

public class Main {
    public static void main(String[] args) {
        try {
            startRenderPort();

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

    private static void startRenderPort() throws Exception {
        String port = System.getenv("PORT");

        if (port == null || port.isBlank()) {
            return;
        }

        var server = com.sun.net.httpserver.HttpServer.create(
                new java.net.InetSocketAddress("0.0.0.0", Integer.parseInt(port)),
                0
        );

        server.createContext("/", exchange -> {
            byte[] response = "online".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        server.start();
        SimpleLogger.info("Porta Render aberta em " + port);
    }
}