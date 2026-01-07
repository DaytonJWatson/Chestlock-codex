package com.watsonllc.chestlock.logic;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.ActionMessages;

public final class PlayerStateManager {
    public static final long ACTION_TIMEOUT_MILLIS = 15000L;
    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private PlayerStateManager() {
    }

    public static boolean hasAction(Player player, PlayerActionType type) {
        return getState(player).hasAction(type);
    }

    public static ActionState getAction(Player player, PlayerActionType type) {
        return getState(player).getAction(type);
    }

    public static boolean isToggleEnabled(Player player, PlayerActionType type) {
        ActionState state = getAction(player, type);
        return state != null && state.isToggleEnabled();
    }

    public static void startAction(Player player, PlayerActionType type, boolean toggle, String target) {
        long now = System.currentTimeMillis();
        getState(player).startAction(type, new ActionState(toggle, target, now, now + ACTION_TIMEOUT_MILLIS));
    }

    public static void clearAction(Player player, PlayerActionType type) {
        PlayerState state = getState(player);
        state.clearAction(type);
        cleanupIfIdle(player, state);
    }

    public static void clearAllActions(Player player) {
        PlayerState state = getState(player);
        state.clearActions();
        cleanupIfIdle(player, state);
    }

    public static void scheduleTimeout(Player player, PlayerActionType type, String actionName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!hasAction(player, type)) {
                    return;
                }

                ActionState state = getAction(player, type);
                clearAction(player, type);
                String commandTimeout = Config.getString("messages.commandTimeout");
                commandTimeout = commandTimeout.replace("%action%", actionName);
                String nextStep = ActionMessages.getNextStep(type, state == null ? null : state.getTarget());
                commandTimeout = commandTimeout.replace("%next%", nextStep);
                player.sendMessage(commandTimeout);
            }
        }.runTaskLater(Main.instance, (ACTION_TIMEOUT_MILLIS / 1000) * 20);
    }

    public static boolean isBypassing(Player player) {
        return getState(player).isBypassing();
    }

    public static void setBypassing(Player player, boolean bypassing) {
        PlayerState state = getState(player);
        state.setBypassing(bypassing);

        if (!bypassing) {
            state.setBypassWarned(false);
            cleanupIfIdle(player, state);
        }
    }

    public static boolean hasSeenBypassWarning(Player player) {
        return getState(player).hasSeenBypassWarning();
    }

    public static void markBypassWarned(Player player) {
        getState(player).setBypassWarned(true);
    }

    public static void resetBypassWarning(Player player) {
        getState(player).setBypassWarned(false);
    }

    public static String getSelectedGroup(Player player) {
        return getState(player).getSelectedGroup();
    }

    public static void setSelectedGroup(Player player, String groupName) {
        PlayerState state = getState(player);
        state.setSelectedGroup(groupName);
    }

    public static void clearState(Player player) {
        STATES.remove(player.getUniqueId());
    }

    public static PlayerActionType getPrimaryAction(Player player) {
        return getState(player).getPrimaryAction();
    }

    public static long getActionTimeRemainingSeconds(Player player, PlayerActionType type) {
        ActionState state = getAction(player, type);
        if (state == null)
            return 0L;
        long remaining = state.getExpiresAtMillis() - System.currentTimeMillis();
        return Math.max(0L, remaining / 1000L);
    }

    public static void forEachActiveAction(ActionConsumer consumer) {
        for (Map.Entry<UUID, PlayerState> entry : STATES.entrySet()) {
            PlayerState state = entry.getValue();
            if (state.actions.isEmpty())
                continue;
            PlayerActionType primary = state.getPrimaryAction();
            if (primary == null)
                continue;
            consumer.accept(entry.getKey(), primary, state.getAction(primary));
        }
    }

    private static PlayerState getState(Player player) {
        return STATES.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerState());
    }

    private static void cleanupIfIdle(Player player, PlayerState state) {
        if (state.isIdle()) {
            STATES.remove(player.getUniqueId());
        }
    }

    private static final class PlayerState {
        private final EnumMap<PlayerActionType, ActionState> actions = new EnumMap<>(PlayerActionType.class);
        private boolean bypassing;
        private boolean bypassWarned;
        private String selectedGroup;

        public boolean hasAction(PlayerActionType type) {
            return actions.containsKey(type);
        }

        public ActionState getAction(PlayerActionType type) {
            return actions.get(type);
        }

        public void startAction(PlayerActionType type, ActionState state) {
            actions.put(type, state);
        }

        public void clearAction(PlayerActionType type) {
            actions.remove(type);
        }

        public void clearActions() {
            actions.clear();
        }

        public boolean isBypassing() {
            return bypassing;
        }

        public void setBypassing(boolean bypassing) {
            this.bypassing = bypassing;
        }

        public boolean hasSeenBypassWarning() {
            return bypassWarned;
        }

        public void setBypassWarned(boolean bypassWarned) {
            this.bypassWarned = bypassWarned;
        }

        public String getSelectedGroup() {
            return selectedGroup;
        }

        public void setSelectedGroup(String selectedGroup) {
            this.selectedGroup = selectedGroup;
        }

        public PlayerActionType getPrimaryAction() {
            for (PlayerActionType type : PlayerActionType.values()) {
                if (actions.containsKey(type))
                    return type;
            }
            return null;
        }

        public boolean isIdle() {
            return actions.isEmpty() && !bypassing && !bypassWarned && (selectedGroup == null || selectedGroup.isEmpty());
        }
    }

    public interface ActionConsumer {
        void accept(UUID playerId, PlayerActionType type, ActionState state);
    }
}
