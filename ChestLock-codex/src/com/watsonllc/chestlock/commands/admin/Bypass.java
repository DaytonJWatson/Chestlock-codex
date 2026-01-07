package com.watsonllc.chestlock.commands.admin;

import org.bukkit.entity.Player;

import com.watsonllc.chestlock.commands.ToggleState;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.PlayerStateManager;

public class Bypass {
        public static boolean logic(Player player, ToggleState toggleState) {
                if(!player.hasPermission("chestlock.bypass")) {
                        player.sendMessage(Config.getString("messages.noPermission"));
                        return false;
                }

                if (toggleState == ToggleState.OFF) {
                        PlayerStateManager.setBypassing(player, false);
                        player.sendMessage(Config.getString("messages.bypassOff"));
                        return true;
                }

                if (toggleState == ToggleState.ON) {
                        PlayerStateManager.setBypassing(player, true);
                        PlayerStateManager.resetBypassWarning(player);
                        player.sendMessage(Config.getString("messages.bypassOn"));
                        return true;
                }

                if (PlayerStateManager.isBypassing(player)) {
                        PlayerStateManager.setBypassing(player, false);
                        player.sendMessage(Config.getString("messages.bypassOff"));
                        return true;
                }

                PlayerStateManager.setBypassing(player, true);
                PlayerStateManager.resetBypassWarning(player);
                player.sendMessage(Config.getString("messages.bypassOn"));

                return true;
        }
}
