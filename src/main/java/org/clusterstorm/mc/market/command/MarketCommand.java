package org.clusterstorm.mc.market.command;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.clusterstorm.mc.market.Config;
import org.clusterstorm.mc.market.Market;
import org.clusterstorm.mc.market.Message;

import java.util.HashMap;
import java.util.List;

public class MarketCommand extends AbstractCommand {

    public MarketCommand() {
        super("market");
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if(args.length == 0) {
            Message.usage.send(sender);
            return;
        }

        if(args[0].equalsIgnoreCase("sell")) {
            if(args.length < 2) {
                Message.usage.send(sender);
                return;
            }
            if(!(sender instanceof Player)) return;

            Player player = (Player) sender;
            ItemStack item = player.getInventory().getItemInMainHand();
            if(item.getType() == Material.AIR) {
                Message.noItem.send(sender);
                return;
            }

            double price;

            try {
                price = Double.parseDouble(args[1]);
                if(price <= 0.01) throw new IllegalArgumentException();
            } catch (Exception e) {
                Message.invalidPrice.send(sender);
                return;
            }

            String itemName = Config.getItemName(item);

            // CREATE SELL ORDER

            int amount = item.getAmount();
            item.setAmount(0);
            player.getInventory().setItemInMainHand(item);


            Message.sellOrderCreated
                    .replace("{item}", itemName)
                    .replace("{amount}", String.valueOf(amount))
                    .replace("{price}", String.valueOf(price)).send(sender);

            return;
        }

        if(args[0].equalsIgnoreCase("remove")) {
            if(!(sender instanceof Player)) return;

            Player player = (Player) sender;
            if(args.length < 2) {
                sender.sendMessage("Remove order: /market remove <id>");
                return;
            }
            
            int id;
            try {
                id = Integer.parseInt(args[1]);
            } catch (Exception e) {
                Message.noOrder.replace("{id}", args[1]).send(player);
                return;
            }

            // REMOVE ORDER

            return;
        }

        if(args[0].equalsIgnoreCase("reload")) {
            if(!sender.hasPermission("market.reload")) {
                Message.noPermission.send(sender);
                return;
            }

            Market.getInstance().reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Market reloaded.");
            return;
        }

        sender.sendMessage(ChatColor.RED + "Unknown command: " + args[0]);
    }

    private String giveItem(Player player, ItemStack item, int amount) {
        if(amount <= 0) return Config.getItemName(item);
        item.setAmount(amount);

        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
        if(!overflow.isEmpty()) {
            for(ItemStack i : overflow.values()) {
                player.getLocation().getWorld().dropItem(player.getLocation(), i);
            }
        }
        return Config.getItemName(item);
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if(args.length == 1) return Lists.newArrayList("buy", "sell", "remove", "reload");
        return Lists.newArrayList();
    }
}
