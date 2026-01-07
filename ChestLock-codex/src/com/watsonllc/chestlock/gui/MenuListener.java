package com.watsonllc.chestlock.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.watsonllc.chestlock.commands.StatusService;
import com.watsonllc.chestlock.commands.ToggleState;
import com.watsonllc.chestlock.commands.admin.Bypass;
import com.watsonllc.chestlock.commands.player.ClaimLock;
import com.watsonllc.chestlock.commands.player.DestroyLock;
import com.watsonllc.chestlock.commands.player.GroupCommands;
import com.watsonllc.chestlock.commands.player.MakePublic;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.PromptManager;
import com.watsonllc.chestlock.logic.PromptType;

public class MenuListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (title.equals(ChestLockMenu.TITLE)) {
            if (!isTopInventoryClick(event))
                return;
            event.setCancelled(true);
            handleMainMenuClick(player, event.getRawSlot());
            return;
        }

        if (title.equals(GroupMenu.TITLE)) {
            if (!isTopInventoryClick(event))
                return;
            event.setCancelled(true);
            handleGroupMenuClick(player, event.getRawSlot());
            return;
        }

        if (title.equals(AdminMenu.TITLE)) {
            if (!isTopInventoryClick(event))
                return;
            event.setCancelled(true);
            handleAdminMenuClick(player, event.getRawSlot());
        }
    }

    private boolean isTopInventoryClick(InventoryClickEvent event) {
        return event.getRawSlot() < event.getView().getTopInventory().getSize();
    }

    private void handleMainMenuClick(Player player, int slot) {
        switch (slot) {
        case ChestLockMenu.SLOT_CLAIM:
            player.closeInventory();
            ClaimLock.logic(player, ToggleState.TOGGLE);
            break;
        case ChestLockMenu.SLOT_DESTROY:
            player.closeInventory();
            DestroyLock.logic(player, ToggleState.TOGGLE);
            break;
        case ChestLockMenu.SLOT_PUBLIC:
            player.closeInventory();
            MakePublic.logic(player, ToggleState.TOGGLE);
            break;
        case ChestLockMenu.SLOT_ADD_OWNER:
            player.closeInventory();
            PromptManager.startPrompt(player, PromptType.ADD_OWNER);
            break;
        case ChestLockMenu.SLOT_REMOVE_OWNER:
            player.closeInventory();
            PromptManager.startPrompt(player, PromptType.REMOVE_OWNER);
            break;
        case ChestLockMenu.SLOT_GROUPS:
            GroupMenu.open(player);
            break;
        case ChestLockMenu.SLOT_STATUS:
            player.closeInventory();
            StatusService.sendStatus(player);
            break;
        case ChestLockMenu.SLOT_ADMIN:
            if (player.hasPermission("chestlock.bypass") || player.hasPermission("chestlock.taghoppers")) {
                AdminMenu.open(player);
            } else {
                player.closeInventory();
                player.sendMessage(Config.getString("messages.noPermission"));
            }
            break;
        default:
            break;
        }
    }

    private void handleGroupMenuClick(Player player, int slot) {
        switch (slot) {
        case GroupMenu.SLOT_CREATE:
            player.closeInventory();
            PromptManager.startPrompt(player, PromptType.GROUP_CREATE);
            break;
        case GroupMenu.SLOT_INVITE:
            player.closeInventory();
            PromptManager.startPrompt(player, PromptType.GROUP_INVITE);
            break;
        case GroupMenu.SLOT_REMOVE:
            player.closeInventory();
            PromptManager.startPrompt(player, PromptType.GROUP_REMOVE);
            break;
        case GroupMenu.SLOT_SELECT:
            player.closeInventory();
            PromptManager.startPrompt(player, PromptType.GROUP_SELECT);
            break;
        case GroupMenu.SLOT_ACCEPT:
            player.closeInventory();
            GroupCommands.accept(player, null);
            break;
        case GroupMenu.SLOT_DECLINE:
            player.closeInventory();
            GroupCommands.decline(player, null);
            break;
        case GroupMenu.SLOT_LIST:
            player.closeInventory();
            GroupCommands.list(player);
            break;
        case GroupMenu.SLOT_INVITES:
            player.closeInventory();
            GroupCommands.invites(player);
            break;
        case GroupMenu.SLOT_LEAVE:
            player.closeInventory();
            GroupCommands.leave(player);
            break;
        default:
            break;
        }
    }

    private void handleAdminMenuClick(Player player, int slot) {
        switch (slot) {
        case AdminMenu.SLOT_BYPASS:
            player.closeInventory();
            Bypass.logic(player, ToggleState.TOGGLE);
            break;
        case AdminMenu.SLOT_TAGHOPPERS:
            player.closeInventory();
            PromptManager.startPrompt(player, PromptType.TAG_HOPPERS);
            break;
        default:
            break;
        }
    }
}
