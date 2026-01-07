package com.watsonllc.chestlock.logic;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.watsonllc.chestlock.commands.ToggleState;
import com.watsonllc.chestlock.commands.admin.TagHoppers;
import com.watsonllc.chestlock.commands.player.AddOwner;
import com.watsonllc.chestlock.commands.player.GroupCommands;
import com.watsonllc.chestlock.commands.player.RemoveOwner;
import com.watsonllc.chestlock.config.Config;

public final class PromptManager {
    private static final Map<UUID, PromptType> PROMPTS = new ConcurrentHashMap<>();

    private PromptManager() {
    }

    public static void startPrompt(Player player, PromptType type) {
        PROMPTS.put(player.getUniqueId(), type);
        player.sendMessage(promptMessage(type));
    }

    public static boolean hasPrompt(Player player) {
        return PROMPTS.containsKey(player.getUniqueId());
    }

    public static void cancelPrompt(Player player) {
        if (PROMPTS.remove(player.getUniqueId()) != null) {
            String cancel = Config.getString("messages.cancelAction");
            cancel = cancel.replace("%action%", "Prompt");
            player.sendMessage(cancel);
        }
    }

    public static boolean handleChat(Player player, String message) {
        PromptType type = PROMPTS.get(player.getUniqueId());
        if (type == null)
            return false;

        if ("cancel".equalsIgnoreCase(message)) {
            cancelPrompt(player);
            return true;
        }

        PROMPTS.remove(player.getUniqueId());

        switch (type) {
        case ADD_OWNER:
            AddOwner.logic(player, message, ToggleState.TOGGLE);
            return true;
        case REMOVE_OWNER:
            RemoveOwner.logic(player, message, ToggleState.TOGGLE);
            return true;
        case GROUP_CREATE:
            GroupCommands.create(player, message);
            return true;
        case GROUP_INVITE:
            GroupCommands.invite(player, message, null);
            return true;
        case GROUP_REMOVE:
            GroupCommands.remove(player, message, null);
            return true;
        case GROUP_SELECT:
            GroupCommands.select(player, message);
            return true;
        case TAG_HOPPERS:
            handleTagHoppers(player, message);
            return true;
        default:
            return false;
        }
    }

    private static void handleTagHoppers(Player player, String message) {
        String[] parts = message.split("\\s+");
        if (parts.length < 2) {
            player.sendMessage(Config.getString("messages.invalidScope"));
            player.sendMessage(Config.getString("messages.promptTagHoppers"));
            return;
        }

        String[] args = new String[parts.length + 1];
        args[0] = "taghoppers";
        System.arraycopy(parts, 0, args, 1, parts.length);
        TagHoppers.logic(player, args);
    }

    private static String promptMessage(PromptType type) {
        switch (type) {
        case ADD_OWNER:
            return Config.getString("messages.promptAddOwner");
        case REMOVE_OWNER:
            return Config.getString("messages.promptRemoveOwner");
        case GROUP_CREATE:
            return Config.getString("messages.promptGroupCreate");
        case GROUP_INVITE:
            return Config.getString("messages.promptGroupInvite");
        case GROUP_REMOVE:
            return Config.getString("messages.promptGroupRemove");
        case GROUP_SELECT:
            return Config.getString("messages.promptGroupSelect");
        case TAG_HOPPERS:
            return Config.getString("messages.promptTagHoppers");
        default:
            return "";
        }
    }
}
