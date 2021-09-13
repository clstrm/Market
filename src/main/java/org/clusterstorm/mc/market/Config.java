package org.clusterstorm.mc.market;

import org.bukkit.inventory.ItemStack;

public class Config {

    public static String getItemName(ItemStack item) {
        return item == null ? "null" : item.getType().name().toLowerCase();
    }
}

