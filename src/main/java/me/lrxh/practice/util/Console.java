package me.lrxh.practice.util;

import org.bukkit.Bukkit;

public class Console {

    public static void sendMessage(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(message);
    }


    public static void sendError(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(CC.translate("&c" + message + "!"));
    }
}