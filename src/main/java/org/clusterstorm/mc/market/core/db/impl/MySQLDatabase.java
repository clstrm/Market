package org.clusterstorm.mc.market.core.db.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.clusterstorm.mc.market.Market;
import org.clusterstorm.mc.market.core.Order;
import org.clusterstorm.mc.market.core.OrderType;
import org.clusterstorm.mc.market.core.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabase implements Database {

    private static final String orders = "orders";

    private final HikariDataSource src;

    public MySQLDatabase(String host, int port, String database, String user, String password) throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(user);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);

        src = new HikariDataSource(config);

        try(Connection cn = connect(); Statement st = cn.createStatement()) {
            st.executeUpdate(String.format(
                    "CREATE TABLE IF NOT EXISTS `%s` (" +
                            "`id` INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                            "`player` TEXT NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`item` TEXT NOT NULL, " +
                            "`price` REAL NOT NULL, " +
                            "`amount` INTEGER NOT NULL, " +
                            "`closed` INTEGER NOT NULL, " +
                            "`time` BIGINT NOT NULL)",
                    orders));
        }
    }

    private Connection connect() throws SQLException {
        return src.getConnection();
    }

    private Order getOrder(ResultSet set) throws SQLException {
        String player = set.getString("player");
        OrderType type = OrderType.valueOf(set.getString("type"));
        int id = set.getInt("id");
        String item = set.getString("item");
        double price = set.getDouble("price");
        int amount = set.getInt("amount");
        int closed = set.getInt("closed");
        long time = set.getLong("time");

        Order order = new Order(player, type, item, price, amount, closed, time);
        order.setId(id);
        return order;
    }

    @Override
    public List<Order> saveOrder(Order order) {
        try(Connection c = connect()) {
            save(c, order);
        } catch (SQLException e) {
            Market.error("Failed to save order #" + order.getId(), e);
        }

        return new ArrayList<>();
    }

    private void save(Connection c, Order order) throws SQLException {
        String query;
        if(order.getId() <= 0) {
            query = "INSERT INTO `%s` (`player`, `type`, `item`, `price`, `amount`, `closed`, `time`) VALUES (?, ?, ?, ?, ?, ?, ?);";
        } else {
            query = "UPDATE `%s` SET `closed` = ? WHERE `id` = ?";
        }

        try(PreparedStatement s = c.prepareStatement(String.format(query, orders), Statement.RETURN_GENERATED_KEYS)) {
            if(order.getId() <= 0) {

                s.setString(1, order.getPlayer());
                s.setString(2, order.getType().name());
                s.setString(3, order.getItem());
                s.setDouble(4, order.getPrice());
                s.setInt(5, order.getAmount());
                s.setInt(6, order.getClosed());
                s.setLong(7, order.getTime());

                s.executeUpdate();
                ResultSet rs = s.getGeneratedKeys();
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    order.setId(newId);
                }
            } else {

                s.setInt(1, order.getClosed());
                s.setInt(2, order.getId());

                s.executeUpdate();
            }
        }
    }

    @Override
    public List<Order> getOrders(String player, OrderType type) {
        String query = String.format("SELECT * FROM `%s` WHERE `player` = ? AND `type` = ?", orders);
        try(Connection c = connect(); PreparedStatement st = c.prepareStatement(query)) {

            st.setString(1, player);
            st.setString(2, type.name());

            List<Order> list = new ArrayList<>();

            try (ResultSet set = st.executeQuery()) {
                while (set.next()) {
                    list.add(getOrder(set));
                }
            }

            return list;
        } catch (SQLException e) {
            Market.error("Failed to load " + type + " orders of " + player, e);
            return null;
        }
    }

    @Override
    public Order removeOrder(String player, int id) {
        try (Connection c = connect()) {

            Order order = null;

            try(PreparedStatement s = c.prepareStatement(String.format("SELECT * FROM `%s` WHERE `id` = ? AND `player` = ?", orders))) {

                s.setInt(1, id);
                s.setString(2, player);

                try (ResultSet set = s.executeQuery()) {
                    if(set.next()) {
                        order = getOrder(set);
                    }
                }
            }

            if(order != null) {
                try (PreparedStatement s = c.prepareStatement(String.format("DELETE FROM %s WHERE id = ?", orders))) {
                    s.setInt(1, id);
                    s.executeUpdate();
                }
            }

            return order;
        } catch (SQLException e) {
            Market.error("Failed to delete order #" + id, e);
            return null;
        }
    }

    @Override
    public void close() {
        src.close();
    }
}
