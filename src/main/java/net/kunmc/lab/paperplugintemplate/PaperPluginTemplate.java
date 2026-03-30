package net.kunmc.lab.paperplugintemplate;

import net.kunmc.lab.commandlib.CommandLib;
import net.kunmc.lab.configlib.ConfigCommandBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class PaperPluginTemplate extends JavaPlugin {
    private static PaperPluginTemplate INSTANCE;
    private Config config;

    public static PaperPluginTemplate getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        config = new Config(this);
        MainCommand command = new MainCommand();
        command.addChildren(new ConfigCommandBuilder(config).build());
        CommandLib.register(this, command);
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
