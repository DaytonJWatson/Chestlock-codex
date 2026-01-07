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
import com.watsonllc.chestlock.logic.PlayerStateManager;

public final class GroupMenu {
    public static final String TITLE = Utils.color("&6ChestLock Groups");
    public static final int SLOT_CREATE = 10;
    public static final int SLOT_INVITE = 11;
    public static final int SLOT_REMOVE = 12;
    public static final int SLOT_SELECT = 13;
    public static final int SLOT_ACCEPT = 15;
    public static final int SLOT_DECLINE = 16;
    public static final int SLOT_LIST = 22;
    public static final int SLOT_INVITES = 23;
    public static final int SLOT_LEAVE = 24;

    private GroupMenu() {
    }

    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, TITLE);
        String selected = PlayerStateManager.getSelectedGroup(player);
        String selectedDisplay = (selected == null || selected.isEmpty()) ? "None" : selected;

        inventory.setItem(SLOT_CREATE, item(Material.EMERALD, "&6Create Group", Arrays.asList(
                "&7Type a new group name in chat.",
                "&7Cancel with &e/cl cancel")));
        inventory.setItem(SLOT_INVITE, item(Material.PLAYER_HEAD, "&6Invite Player", Arrays.asList(
                "&7Invite a player to your group.",
                "&7Uses selected group: &e" + selectedDisplay)));
        inventory.setItem(SLOT_REMOVE, item(Material.SKELETON_SKULL, "&6Remove Player", Arrays.asList(
                "&7Remove a player from your group.",
                "&7Uses selected group: &e" + selectedDisplay)));
        inventory.setItem(SLOT_SELECT, item(Material.COMPASS, "&6Select Group", Arrays.asList(
                "&7Select a group for invites and removals.",
                "&7Current: &e" + selectedDisplay)));
        inventory.setItem(SLOT_ACCEPT, item(Material.LIME_WOOL, "&6Accept Invite", Arrays.asList(
                "&7Accept a pending group invite.")));
        inventory.setItem(SLOT_DECLINE, item(Material.RED_WOOL, "&6Decline Invite", Arrays.asList(
                "&7Decline a pending group invite.")));
        inventory.setItem(SLOT_LIST, item(Material.BOOK, "&6List Members", Arrays.asList(
                "&7List members in your current group.")));
        inventory.setItem(SLOT_INVITES, item(Material.PAPER, "&6View Invites", Arrays.asList(
                "&7Show your pending invites.")));
        inventory.setItem(SLOT_LEAVE, item(Material.BARRIER, "&6Leave Group", Arrays.asList(
                "&7Leave your current group.")));

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
