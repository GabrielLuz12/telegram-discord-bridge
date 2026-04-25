package com.bridge.bridge;

import com.bridge.discord.DiscordBot;
import com.bridge.telegram.TelegramBot;
import com.bridge.util.SimpleLogger;

import java.io.File;

public class MessageBridgeService {

    private DiscordBot discordBot;
    private TelegramBot telegramBot;

    public void setDiscordBot(DiscordBot discordBot) {
        this.discordBot = discordBot;
    }

    public void setTelegramBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void handleDiscordText(String content) {
        try {
            telegramBot.sendMessage(content);
            SimpleLogger.info("Texto enviado do Discord para o Telegram.");
        } catch (Exception e) {
            SimpleLogger.error("Erro ao enviar texto do Discord para o Telegram", e);
        }
    }

    public void handleTelegramText(String content) {
        try {
            discordBot.sendMessage(content);
            SimpleLogger.info("Texto enviado do Telegram para o Discord.");
        } catch (Exception e) {
            SimpleLogger.error("Erro ao enviar texto do Telegram para o Discord", e);
        }
    }

    public void handleTelegramFile(File file, String caption) {
        try {
            discordBot.sendFile(file, caption);
            SimpleLogger.info("Arquivo/imagem enviado do Telegram para o Discord.");
        } catch (Exception e) {
            SimpleLogger.error("Erro ao enviar arquivo do Telegram para o Discord", e);
        }
    }

    public void handleDiscordFile(File file, String caption) {
        try {
            telegramBot.sendPhoto(file, caption);
            SimpleLogger.info("Arquivo/imagem enviado do Discord para o Telegram.");
        } catch (Exception e) {
            SimpleLogger.error("Erro ao enviar arquivo do Discord para o Telegram", e);
        }
    }
}