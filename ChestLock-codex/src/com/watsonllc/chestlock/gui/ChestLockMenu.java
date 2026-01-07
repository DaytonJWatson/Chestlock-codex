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

public final class ChestLockMenu {
    public static final String TITLE = Utils.color("&6ChestLock Menu");
    public static final int SLOT_CLAIM = 10;
    public static final int SLOT_DESTROY = 11;
    public static final int SLOT_PUBLIC = 12;
    public static final int SLOT_ADD_OWNER = 14;
    public static final int SLOT_REMOVE_OWNER = 15;
    public static final int SLOT_GROUPS = 16;
    public static final int SLOT_STATUS = 22;
    public static final int SLOT_ADMIN = 24;

    private ChestLockMenu() {
    }

    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, TITLE);

        inventory.setItem(SLOT_CLAIM, item(Material.CHEST, "&6Claim Mode", Arrays.asList(
                "&7Enter claim mode",
                "&7Right-click a container to lock it.",
                "&7Cancel with &e/cl cancel")));
        inventory.setItem(SLOT_DESTROY, item(Material.BARRIER, "&6Destroy Mode", Arrays.asList(
                "&7Enter destroy mode",
                "&7Right-click a container to remove its lock.",
                "&7Cancel with &e/cl cancel")));
        inventory.setItem(SLOT_PUBLIC, item(Material.OAK_SIGN, "&6Public Mode", Arrays.asList(
                "&7Enter public mode",
                "&7Right-click a container to make it public.",
                "&7Cancel with &e/cl cancel")));
        inventory.setItem(SLOT_ADD_OWNER, item(Material.PLAYER_HEAD, "&6Add Owner", Arrays.asList(
                "&7Type a player name in chat",
                "&7Then right-click a container.",
                "&7Cancel with &e/cl cancel")));
        inventory.setItem(SLOT_REMOVE_OWNER, item(Material.SKELETON_SKULL, "&6Remove Owner", Arrays.asList(
                "&7Type a player name in chat",
                "&7Then right-click a container.",
                "&7Cancel with &e/cl cancel")));
        inventory.setItem(SLOT_GROUPS, item(Material.NAME_TAG, "&6Groups", Arrays.asList(
                "&7Open group management",
                "&7Invite, remove, and list members.")));
        inventory.setItem(SLOT_STATUS, item(Material.PAPER, "&6Status", Arrays.asList(
                "&7View current mode, bypass, and selected group.")));

        if (player.hasPermission("chestlock.bypass") || player.hasPermission("chestlock.taghoppers")) {
            inventory.setItem(SLOT_ADMIN, item(Material.REDSTONE, "&6Admin Tools", Arrays.asList(
                    "&7Bypass toggle and hopper tagging.")));
        }

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
