package com.watsonllc.chestlock.commands.player;

import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import com.watsonllc.chestlock.commands.Commands;
import com.watsonllc.chestlock.commands.ToggleState;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.ActionMessages;
import com.watsonllc.chestlock.logic.LockController;
import com.watsonllc.chestlock.logic.PlayerActionType;
import com.watsonllc.chestlock.logic.PlayerStateManager;
import com.watsonllc.chestlock.Utils;

public class DestroyLock {
	
        public static boolean logic(Player player, ToggleState toggleState) {
                if(Commands.usePermissions()) {
                        if(!player.hasPermission("chestlock.destroy")) {
                                player.sendMessage(Config.getString("messages.noPermission"));
                                return false;
                        }
                }

                if (toggleState == ToggleState.OFF) {
                        if (PlayerStateManager.hasAction(player, PlayerActionType.DESTROY_LOCK)) {
                                PlayerStateManager.clearAction(player, PlayerActionType.DESTROY_LOCK);
                                String disabled = Config.getString("messages.modeDisabled");
                                disabled = disabled.replace("%action%", ActionMessages.getActionName(PlayerActionType.DESTROY_LOCK));
                                player.sendMessage(disabled);
                                return true;
                        }
                        player.sendMessage(Config.getString("messages.noActionToCancel"));
                        return true;
                }

                if (toggleState == ToggleState.TOGGLE && PlayerStateManager.hasAction(player, PlayerActionType.DESTROY_LOCK)) {
                        String actionMSG = Config.getString("messages.cancelAction");
                        actionMSG = actionMSG.replace("%action%", ActionMessages.getActionName(PlayerActionType.DESTROY_LOCK));
                        player.sendMessage(actionMSG);
                        PlayerStateManager.clearAction(player, PlayerActionType.DESTROY_LOCK);
                        return true;
                }

                PlayerStateManager.startAction(player, PlayerActionType.DESTROY_LOCK, true, null);
                player.sendMessage(ActionMessages.getModeStart(PlayerActionType.DESTROY_LOCK, null));

                PlayerStateManager.scheduleTimeout(player, PlayerActionType.DESTROY_LOCK, ActionMessages.getActionName(PlayerActionType.DESTROY_LOCK));

                return true;
        }
	
        public static void eventChecker(PlayerInteractEvent event) {
                if(!PlayerStateManager.hasAction(event.getPlayer(), PlayerActionType.DESTROY_LOCK))
                        return;

                if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
                        return;

                Player player = event.getPlayer();
                Block block = event.getClickedBlock();
                Location blockLocation = block.getLocation();

                event.setCancelled(true);

                LockController lc = new LockController();

                if(lc.naturalBlock(blockLocation)) {
                        player.sendMessage(Config.getString("messages.invalidLock"));
                        PlayerStateManager.clearAction(player, PlayerActionType.DESTROY_LOCK);
                        return;
                }

                // Check if the interacted block is a valid lock
                if(!Utils.lockableBlock(block)) {
                        player.sendMessage(Config.getString("messages.invalidLock"));
                        PlayerStateManager.clearAction(player, PlayerActionType.DESTROY_LOCK);
                        return;
                }

                // check if the player owns the lock or can bypass
                if(lc.getOwner(blockLocation).equals(player.getName()) || PlayerStateManager.isBypassing(player)) {
                        String lockType = lc.getLockType(event.getClickedBlock().getLocation());
                        String destroyedLockMSG = Config.getString("messages.destroyedLock");
                        destroyedLockMSG = destroyedLockMSG.replace("%type%", lockType);
                        player.sendMessage(destroyedLockMSG);

                        Set<String> lockIds = new LinkedHashSet<>();
                        for (Block targetBlock : Utils.getConnectedChestBlocks(block)) {
                                if (lc.naturalBlock(targetBlock.getLocation()))
                                        continue;
                                String lockId = lc.getLockID(targetBlock.getLocation());
                                if (lockId != null) {
                                        lockIds.add(lockId);
                                }
                        }

                        for (String lockId : lockIds) {
                                lc.removeLock(lockId);
                        }

                        if(PlayerStateManager.isToggleEnabled(player, PlayerActionType.DESTROY_LOCK))
                                return;
                        PlayerStateManager.clearAction(player, PlayerActionType.DESTROY_LOCK);
                        return;
                }

                String invalidOwnerMSG = Config.getString("messages.invalidOwner").replace("%player%", lc.getOwner(blockLocation));
                player.sendMessage(invalidOwnerMSG);
                PlayerStateManager.clearAction(player, PlayerActionType.DESTROY_LOCK);
        }
}
