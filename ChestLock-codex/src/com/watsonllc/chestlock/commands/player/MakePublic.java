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
import com.watsonllc.chestlock.logic.PlayerActionType;
import com.watsonllc.chestlock.logic.PlayerStateManager;

public class MakePublic {

        public static boolean logic(Player player, ToggleState toggleState) {
                if (Commands.usePermissions()) {
                        if (!player.hasPermission("chestlock.public")) {
                                player.sendMessage(Config.getString("messages.noPermission"));
                                return false;
                        }
                }

                if (toggleState == ToggleState.OFF) {
                        if (PlayerStateManager.hasAction(player, PlayerActionType.MAKE_PUBLIC)) {
                                PlayerStateManager.clearAction(player, PlayerActionType.MAKE_PUBLIC);
                                String disabled = Config.getString("messages.modeDisabled");
                                disabled = disabled.replace("%action%", ActionMessages.getActionName(PlayerActionType.MAKE_PUBLIC));
                                player.sendMessage(disabled);
                                return true;
                        }
                        player.sendMessage(Config.getString("messages.noActionToCancel"));
                        return true;
                }

                if (toggleState == ToggleState.TOGGLE && PlayerStateManager.hasAction(player, PlayerActionType.MAKE_PUBLIC)) {
                        String actionMSG = Config.getString("messages.cancelAction");
                        actionMSG = actionMSG.replace("%action%", ActionMessages.getActionName(PlayerActionType.MAKE_PUBLIC));
                        player.sendMessage(actionMSG);
                        PlayerStateManager.clearAction(player, PlayerActionType.MAKE_PUBLIC);
                        return true;
                }

                PlayerStateManager.startAction(player, PlayerActionType.MAKE_PUBLIC, true, null);
                player.sendMessage(ActionMessages.getModeStart(PlayerActionType.MAKE_PUBLIC, null));

                PlayerStateManager.scheduleTimeout(player, PlayerActionType.MAKE_PUBLIC, ActionMessages.getActionName(PlayerActionType.MAKE_PUBLIC));

                return true;
        }

        public static void eventChecker(PlayerInteractEvent event) {
                if (!PlayerStateManager.hasAction(event.getPlayer(), PlayerActionType.MAKE_PUBLIC))
                        return;

                if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                        return;

                Player player = event.getPlayer();

                event.setCancelled(true);

                // Check if the interacted block is a valid lock
                if (!Utils.lockableBlock(event.getClickedBlock())) {
                        player.sendMessage(Config.getString("messages.invalidLock"));
                        PlayerStateManager.clearAction(player, PlayerActionType.MAKE_PUBLIC);
                        return;
                }

                LockController lc = new LockController();
                Location blockLocation = event.getClickedBlock().getLocation();

                if (lc.naturalBlock(event.getClickedBlock().getLocation())) {
                        player.sendMessage(Config.getString("messages.invalidLock"));
                        PlayerStateManager.clearAction(player, PlayerActionType.MAKE_PUBLIC);
                        return;
                }

                // check if the player owns the lock or can bypass
                if (lc.getOwner(blockLocation).equals(player.getName()) || PlayerStateManager.isBypassing(player)) {
                        if (!lc.isPublic(event.getClickedBlock().getLocation())) {
                                String madePublicMSG = Config.getString("messages.makePublic");
                                player.sendMessage(madePublicMSG);
                        } else {
                                String makePrivateMSG = Config.getString("messages.makePrivate");
                                player.sendMessage(makePrivateMSG);
                        }

                        lc.changePublicMode(event.getClickedBlock().getLocation());
                        if (PlayerStateManager.isToggleEnabled(player, PlayerActionType.MAKE_PUBLIC))
                                return;
                        PlayerStateManager.clearAction(player, PlayerActionType.MAKE_PUBLIC);
                        return;
                }

                String invalidOwnerMSG = Config.getString("messages.invalidOwner").replace("%player%",lc.getOwner(blockLocation));
                player.sendMessage(invalidOwnerMSG);
                PlayerStateManager.clearAction(player, PlayerActionType.MAKE_PUBLIC);
        }
}
