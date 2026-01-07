package com.watsonllc.chestlock.events.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.watsonllc.chestlock.logic.PlayerStateManager;
import com.watsonllc.chestlock.logic.PromptManager;

public class PlayerQuit implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PromptManager.cancelPrompt(event.getPlayer());
        PlayerStateManager.clearState(event.getPlayer());
    }
}
