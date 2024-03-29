package net.kunmc.lab.paperplugintemplate;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class PaperPluginTemplate extends JavaPlugin {
    private static PaperPluginTemplate INSTANCE;

    public static PaperPluginTemplate getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
    }

    public static void print(Object obj) {
        if (Objects.equals(System.getProperty("plugin.env"), "DEV")) {
            System.out.printf("[%s] %s%n", PaperPluginTemplate.class.getSimpleName(), obj);
        }
    }

    public static void broadcast(Object obj) {
        if (Objects.equals(System.getProperty("plugin.env"), "DEV")) {
            Bukkit.broadcastMessage(String.format("[%s] %s", PaperPluginTemplate.class.getSimpleName(), obj));
        }
    }
}
