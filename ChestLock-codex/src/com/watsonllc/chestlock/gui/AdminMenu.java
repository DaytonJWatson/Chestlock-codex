package com.watsonllc.chestlock.gui;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.watsonllc.chestlock.Utils;

public final class AdminMenu {
    public static final String TITLE = Utils.color("&6ChestLock Admin");
    public static final int SLOT_BYPASS = 11;
    public static final int SLOT_TAGHOPPERS = 15;

    private AdminMenu() {
    }

    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, TITLE);

        inventory.setItem(SLOT_BYPASS, item(Material.REDSTONE_TORCH, "&6Bypass Mode", Arrays.asList(
                "&7Toggle bypass mode on or off.")));
        inventory.setItem(SLOT_TAGHOPPERS, item(Material.HOPPER, "&6Tag Hoppers", Arrays.asList(
                "&7Tag hopper owners within a scope.",
                "&7Click to enter parameters in chat.")));

        player.openInventory(inventory);
    }

    private static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Utils.color(name));
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(colorLore(lore));
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static List<String> colorLore(List<String> lore) {
        List<String> colored = new java.util.ArrayList<>();
        for (String line : lore) {
            colored.add(Utils.color(line));
        }
        return colored;
    }
}
