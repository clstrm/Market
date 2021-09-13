package org.clusterstorm.mc.market.core.db;

import org.clusterstorm.mc.market.core.Order;
import org.clusterstorm.mc.market.core.OrderType;

import java.util.List;

public interface Database {

    List<Order> saveOrder(Order order);

    List<Order> getOrders(String player, OrderType type);

    Order removeOrder(String player, int id);

    void close();
}
