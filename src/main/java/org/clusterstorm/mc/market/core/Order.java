package org.clusterstorm.mc.market.core;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class Order {

    private int id;
    private final String player;
    private final OrderType type;
    private final long time;

    private final String item;
    private final double price;

    private final int amount;
    private int closed;



    public Order(String player, OrderType type, String item, double price, int amount, int closed, long time) {
        this.player = player;
        this.type = type;
        this.item = item;
        this.price = price;
        this.amount = amount;
        this.closed = closed;
        this.time = time;
    }

    public static Order create(String player, OrderType type, ItemStack item, double price) {
        ItemStack copy = item.clone();
        copy.setAmount(1);
        String itemBase64 = itemToString(copy);
        int amount = item.getAmount();

        return new Order(player, type, itemBase64, price, amount, 0, System.currentTimeMillis());
    }




    public ItemStack toItemStack() {
        return stringToItem(item);
    }

    public static String itemToString(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize item stack", e);
        }
    }

    public static ItemStack stringToItem(String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deserialize item stack", e);
        }
    }

    public int close() {
        int diff = amount - closed;
        closed = amount;
        return diff;
    }

    public void closePartially(int part) throws IllegalArgumentException {
        if(closed + part > amount) throw new IllegalArgumentException("Cannot close order #" + id + " with value " + part + ", amount = " + amount);
        closed += part;
    }

    public int getClosedPercent() {
        return (int) (((float) closed) / amount * 100);
    }

    public boolean isClosed() {
        return closed == amount;
    }

    public int getRemain() {
        return amount - closed;
    }

    public double getTotalSum() {
        return amount * price;
    }

    public double getClosedSum() {
        return closed * price;
    }

    public double getRemainSum() {
        return getRemain() * price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayer() {
        return player;
    }

    public OrderType getType() {
        return type;
    }

    public String getItem() {
        return item;
    }

    public double getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public int getClosed() {
        return closed;
    }

    public long getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id && time == order.time && Double.compare(order.price, price) == 0 && amount == order.amount && closed == order.closed && Objects.equals(player, order.player) && type == order.type && Objects.equals(item, order.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, player, type, time, item, price, amount, closed);
    }
}
