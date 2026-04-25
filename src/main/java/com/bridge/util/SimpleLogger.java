package com.bridge.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimpleLogger {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String msg) {
        System.out.println("[" + LocalDateTime.now().format(FMT) + "] [INFO] " + msg);
    }

    public static void warn(String msg) {
        System.out.println("[" + LocalDateTime.now().format(FMT) + "] [WARN] " + msg);
    }

    public static void error(String msg) {
        System.err.println("[" + LocalDateTime.now().format(FMT) + "] [ERROR] " + msg);
    }

    public static void error(String msg, Throwable t) {
        error(msg);
        t.printStackTrace();
    }
}