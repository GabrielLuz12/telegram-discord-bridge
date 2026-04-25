package com.bridge.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

public class TelegramModels {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramResponse<T> {
        public boolean ok;
        public T result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Update {
        public long update_id;
        public Message message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        public long message_id;
        public User from;
        public Chat chat;
        public String text;
        public String caption;
        public List<PhotoSize> photo;
        public Document document;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PhotoSize {
        public String file_id;
        public int width;
        public int height;
        public int file_size;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        public String file_id;
        public String file_name;
        public String mime_type;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileResult {
        public String file_id;
        public String file_unique_id;
        public String file_path;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        public String first_name;
        public String username;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Chat {
        public long id;
    }

    public static class UpdateList extends TelegramResponse<List<Update>> {}
    public static class FileResponse extends TelegramResponse<FileResult> {}
}