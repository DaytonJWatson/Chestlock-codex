package com.watsonllc.chestlock.commands.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.commands.Commands;
import com.watsonllc.chestlock.commands.ToggleState;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.ActionMessages;
import com.watsonllc.chestlock.logic.LockController;
import com.watsonllc.chestlock.logic.PlayerStateManager;
import com.watsonllc.chestlock.logic.PlayerActionType;
import com.watsonllc.chestlock.logic.ActionState;

public class AddOwner {

        public static boolean logic(Player player, String target, ToggleState toggleState) {
                if(Commands.usePermissions()) {
                        if(!player.hasPermission("chestlock.add")) {
                                player.sendMessage(Config.getString("messages.noPermission"));
                                return false;
                        }
                }

                if (toggleState == ToggleState.OFF) {
                        if (PlayerStateManager.hasAction(player, PlayerActionType.ADD_OWNER)) {
                                PlayerStateManager.clearAction(player, PlayerActionType.ADD_OWNER);
                                String disabled = Config.getString("messages.modeDisabled");
                                disabled = disabled.replace("%action%", ActionMessages.getActionName(PlayerActionType.ADD_OWNER));
                                player.sendMessage(disabled);
                                return true;
                        }
                        player.sendMessage(Config.getString("messages.noActionToCancel"));
                        return true;
                }

                if (toggleState == ToggleState.TOGGLE && PlayerStateManager.hasAction(player, PlayerActionType.ADD_OWNER)) {
                        String actionMSG = Config.getString("messages.cancelAction");
                        actionMSG = actionMSG.replace("%action%", ActionMessages.getActionName(PlayerActionType.ADD_OWNER));
                        player.sendMessage(actionMSG);
                        PlayerStateManager.clearAction(player, PlayerActionType.ADD_OWNER);
                        return true;
                }

                PlayerStateManager.startAction(player, PlayerActionType.ADD_OWNER, true, target);
                player.sendMessage(ActionMessages.getModeStart(PlayerActionType.ADD_OWNER, target));

                PlayerStateManager.scheduleTimeout(player, PlayerActionType.ADD_OWNER, ActionMessages.getActionName(PlayerActionType.ADD_OWNER));

                return true;
        }
	
        public static void eventChecker(PlayerInteractEvent event) {
                if(!PlayerStateManager.hasAction(event.getPlayer(), PlayerActionType.ADD_OWNER))
                        return;

                if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
                        return;

                Player player = event.getPlayer();
                ActionState actionState = PlayerStateManager.getAction(player, PlayerActionType.ADD_OWNER);
                String target = actionState != null ? actionState.getTarget() : null;

                if(!Utils.lockableBlock(event.getClickedBlock())) {
                        player.sendMessage(Config.getString("messages.invalidLock"));
                        PlayerStateManager.clearAction(player, PlayerActionType.ADD_OWNER);
                        return;
                }

                LockController lc = new LockController();
                Location blockLocation = event.getClickedBlock().getLocation();

                if (lc.naturalBlock(blockLocation)) {
                        player.sendMessage(Config.getString("messages.invalidLock"));
                        PlayerStateManager.clearAction(player, PlayerActionType.ADD_OWNER);
                        return;
                }

                // check if the player owns the lock or can bypass
                if(lc.getOwner(blockLocation).equals(player.getName()) || PlayerStateManager.isBypassing(player)) {
                        event.setCancelled(true);
                        lc.addOwner(player, target, blockLocation);
                        String shareMSG = Config.getString("messages.shareLock");
                        shareMSG = shareMSG.replace("%target%", target);
                        player.sendMessage(shareMSG);

                        if(PlayerStateManager.isToggleEnabled(player, PlayerActionType.ADD_OWNER))
                                return;

                        PlayerStateManager.clearAction(player, PlayerActionType.ADD_OWNER);
                        return;
                }

                String invalidOwnerMSG = Config.getString("messages.invalidOwner").replace("%player%", lc.getOwner(blockLocation));
                player.sendMessage(invalidOwnerMSG);
                PlayerStateManager.clearAction(player, PlayerActionType.ADD_OWNER);
        }
}
