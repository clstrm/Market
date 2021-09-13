package org.clusterstorm.mc.market.core;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.clusterstorm.mc.market.Market;
import org.clusterstorm.mc.market.core.db.Database;
import org.clusterstorm.mc.market.core.db.impl.MySQLDatabase;
import org.clusterstorm.mc.market.core.db.impl.SQLiteDatabase;

import java.io.File;
import java.sql.SQLException;
import java.util.function.Consumer;

public class MarketCore {

    private static final double MIN_PRICE = 0.01;

    private final Database db;

    public MarketCore() {
        try {

            if(Market.getInstance().getConfig().getBoolean("database.enabled")) {
                db = new MySQLDatabase(
                        Market.getInstance().getConfig().getString("database.host"),
                        Market.getInstance().getConfig().getInt("database.port", 3306),
                        Market.getInstance().getConfig().getString("database.database"),
                        Market.getInstance().getConfig().getString("database.user"),
                        Market.getInstance().getConfig().getString("database.password")
                        );
            } else {
                db = new SQLiteDatabase(new File(Market.getInstance().getDataFolder(), "database.db"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }


    public void buy(String player, ItemStack item, double price) {
        addOrder(player, item, price, OrderType.BUY);
    }

    public void sell(String player, ItemStack item, double price) {
        addOrder(player, item, price, OrderType.SELL);
    }

    private void addOrder(String player, ItemStack item, double price, OrderType type) {
        if(price < MIN_PRICE) throw new IllegalArgumentException("Invalid price");

        Order order = Order.create(player, type, item, price);

        async(() -> {
            db.saveOrder(order);
        });
    }

    public void removeOrder(String player, int id, Consumer<Order> callback) {
        async(() -> {
            Order cancelled = db.removeOrder(player, id);
            sync(() -> callback.accept(cancelled));
        });
    }

    public void async(Runnable run) {
        Bukkit.getScheduler().runTaskAsynchronously(Market.getInstance(), () -> {
            synchronized (MarketCore.this) {
                run.run();
            }
        });
    }

    public void sync(Runnable run) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Market.getInstance(), run);
    }

    public void close() {
        if(db != null) db.close();
    }
}
