package org.clusterstorm.mc.market;

import org.bukkit.plugin.java.JavaPlugin;
import org.clusterstorm.mc.market.command.MarketCommand;
import org.clusterstorm.mc.market.core.MarketCore;

import java.util.logging.Level;

public final class Market extends JavaPlugin {

    private static Market instance;
    private MarketCore core;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        Message.load(getConfig());

        core = new MarketCore();


        new MarketCommand();
    }

    @Override
    public void onDisable() {
        if(core != null) core.close();
    }

    public static Market getInstance() {
        return instance;
    }

    public static MarketCore getCore() {
        return instance.core;
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
