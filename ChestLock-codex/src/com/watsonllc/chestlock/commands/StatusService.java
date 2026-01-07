package com.watsonllc.chestlock.commands;

import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.logic.ActionMessages;
import com.watsonllc.chestlock.logic.PlayerActionType;
import com.watsonllc.chestlock.logic.PlayerStateManager;
import org.bukkit.entity.Player;

public final class StatusService {
    private StatusService() {
    }

    public static void sendStatus(Player player) {
        player.sendMessage(Config.getString("messages.statusHeader"));

        PlayerActionType action = PlayerStateManager.getPrimaryAction(player);
        String noMode = Config.getStringRaw("messages.noModeActive");
        if (noMode == null) {
            noMode = "None";
        }
        String mode = action == null ? Utils.color(noMode) : ActionMessages.getActionName(action);
        player.sendMessage(Config.getString("messages.statusMode").replace("%mode%", mode));

        if (action != null) {
            String target = null;
            if (PlayerStateManager.getAction(player, action) != null) {
                target = PlayerStateManager.getAction(player, action).getTarget();
            }
            if (target != null) {
                player.sendMessage(Config.getString("messages.statusTarget").replace("%target%", target));
            }

            long remaining = PlayerStateManager.getActionTimeRemainingSeconds(player, action);
            player.sendMessage(Config.getString("messages.statusTimeRemaining")
                    .replace("%time%", String.valueOf(remaining))
                    .replace("%s", remaining == 1 ? "" : "s"));
        }

        String bypass = PlayerStateManager.isBypassing(player) ? "On" : "Off";
        player.sendMessage(Config.getString("messages.statusBypass").replace("%state%", bypass));

        String selectedGroup = PlayerStateManager.getSelectedGroup(player);
        if (selectedGroup == null || selectedGroup.isEmpty()) {
            selectedGroup = "None";
        }
        player.sendMessage(Config.getString("messages.statusGroup").replace("%group%", selectedGroup));
    }
}
