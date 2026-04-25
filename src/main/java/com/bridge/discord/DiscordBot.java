package com.bridge.discord;

import com.bridge.bridge.MessageBridgeService;
import com.bridge.config.AppConfig;
import com.bridge.util.SimpleLogger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.List;

public class DiscordBot extends ListenerAdapter {
    private final AppConfig config;
    private final MessageBridgeService bridgeService;
    private JDA jda;

    public DiscordBot(AppConfig config, MessageBridgeService bridgeService) {
        this.config = config;
        this.bridgeService = bridgeService;
    }

    public void start() throws InterruptedException {
        this.jda = JDABuilder.createDefault(config.getDiscordToken())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(this)
                .build()
                .awaitReady();

        SimpleLogger.info("Discord conectado como " + jda.getSelfUser().getAsTag());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.isFromGuild()) return;
        if (event.getChannel().getIdLong() != config.getDiscordChannelId()) return;

        String content = event.getMessage().getContentDisplay();

        if (content != null && !content.isBlank()) {
            bridgeService.handleDiscordText(content);
        }

        List<Attachment> attachments = event.getMessage().getAttachments();

        for (Attachment attachment : attachments) {
            try {
                File tempFile = File.createTempFile("discord-", "-" + attachment.getFileName());

                attachment.getProxy().downloadToFile(tempFile).thenAccept(file -> {
                    String caption = content != null ? content : "";
                    bridgeService.handleDiscordFile(file, caption);
                    file.deleteOnExit();
                });

            } catch (Exception e) {
                SimpleLogger.error("Erro ao baixar anexo do Discord", e);
            }
        }
    }

    public void sendMessage(String text) {
        TextChannel channel = jda.getTextChannelById(config.getDiscordChannelId());

        if (channel == null) {
            throw new IllegalStateException("Canal do Discord não encontrado.");
        }

        channel.sendMessage(text).queue();
    }

    public void sendFile(File file, String caption) {
        TextChannel channel = jda.getTextChannelById(config.getDiscordChannelId());

        if (channel == null) {
            throw new IllegalStateException("Canal do Discord não encontrado.");
        }

        if (caption != null && !caption.isBlank()) {
            channel.sendMessage(caption)
                    .addFiles(FileUpload.fromData(file))
                    .queue();
        } else {
            channel.sendFiles(FileUpload.fromData(file)).queue();
        }
    }
}