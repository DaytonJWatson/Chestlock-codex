package com.watsonllc.chestlock.events.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.logic.PromptManager;

public class PlayerChatPrompt implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!PromptManager.hasPrompt(player))
            return;

        String message = event.getMessage();
        event.setCancelled(true);

        Main.instance.getServer().getScheduler().runTask(Main.instance, () -> {
            PromptManager.handleChat(player, message);
        });
    }
}
