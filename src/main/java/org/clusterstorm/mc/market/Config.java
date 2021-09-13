package org.clusterstorm.mc.market;

import org.bukkit.inventory.ItemStack;

public class Config {

    public static String getItemName(ItemStack item) {
        return item.getType().name().toLowerCase();
    }
}
