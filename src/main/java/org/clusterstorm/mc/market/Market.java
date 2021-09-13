package org.clusterstorm.mc.market;

import org.bukkit.plugin.java.JavaPlugin;
import org.clusterstorm.mc.market.command.MarketCommand;

import java.util.logging.Level;

public final class Market extends JavaPlugin {

    private static Market instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        Message.load(getConfig());

        new MarketCommand();
    }

    @Override
    public void onDisable() {

    }

    public static Market getInstance() {
        return instance;
    }


    public static void error(String msg) {
        instance.getLogger().severe(msg);
    }

    public static void error(String msg, Throwable e) {
        instance.getLogger().log(Level.SEVERE, msg, e);
    }

    public static void log(String msg) {
        instance.getLogger().info(msg);
    }
}
