package com.watsonllc.chestlock.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.commands.admin.Bypass;
import com.watsonllc.chestlock.commands.admin.TagHoppers;
import com.watsonllc.chestlock.commands.player.AddOwner;
import com.watsonllc.chestlock.commands.player.ClaimLock;
import com.watsonllc.chestlock.commands.player.DestroyLock;
import com.watsonllc.chestlock.commands.player.GroupCommands;
import com.watsonllc.chestlock.commands.player.MakePublic;
import com.watsonllc.chestlock.commands.player.RemoveOwner;
import com.watsonllc.chestlock.commands.StatusService;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.gui.ChestLockMenu;
import com.watsonllc.chestlock.gui.GroupMenu;
import com.watsonllc.chestlock.logic.ActionMessages;
import com.watsonllc.chestlock.logic.PlayerActionType;
import com.watsonllc.chestlock.logic.PlayerStateManager;
import com.watsonllc.chestlock.logic.PromptManager;

public class Commands implements CommandExecutor {

    public static void setup() {
        Main.instance.getCommand("chestlock").setExecutor(new Commands());
        Main.instance.getCommand("chestlock").setTabCompleter(new TabCommand());
    }

    public static boolean usePermissions() {
        return Config.getBoolean("settings.usePermissions");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
        if (!(sender instanceof Player) && (args.length == 0 || !args[0].equalsIgnoreCase("taghoppers"))) {
            Main.instance.getLogger().warning(Config.getString("messages.invalidInstance"));
            return false;
        }

        Player player = sender instanceof Player ? (Player) sender : null;

        if (args.length == 0) {
            if (player == null)
                return false;
            if (shouldOpenGui()) {
                ChestLockMenu.open(player);
                return true;
            }
            HelpService.sendQuickHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
        case "help":
            return HelpService.handleHelp(sender, args);
        case "cancel":
            return handleCancel(player);
        case "status":
            return handleStatus(player);
        case "menu":
            if (player != null && shouldOpenGui()) {
                ChestLockMenu.open(player);
                return true;
            }
            return player != null && HelpService.handleHelp(player, new String[] { "help" });
        case "claim":
            return handleToggleAction(player, args, PlayerActionType.CLAIM_LOCK);
        case "destroy":
            return handleToggleAction(player, args, PlayerActionType.DESTROY_LOCK);
        case "public":
            return handleToggleAction(player, args, PlayerActionType.MAKE_PUBLIC);
        case "add":
            return handleOwnerAction(player, args, true);
        case "remove":
            return handleOwnerAction(player, args, false);
        case "group":
            return handleGroup(player, args);
        case "bypass":
            return handleBypass(player, args);
        case "taghoppers":
            return TagHoppers.logic(sender, args);
        default:
            if (player != null) {
                HelpService.sendQuickHelp(player);
                return true;
            }
            return false;
        }
    }

    private boolean handleCancel(Player player) {
        if (player == null)
            return false;

        PlayerActionType action = PlayerStateManager.getPrimaryAction(player);
        boolean hadPrompt = PromptManager.hasPrompt(player);
        if (action == null && !hadPrompt) {
            player.sendMessage(Config.getString("messages.noActionToCancel"));
            return true;
        }

        if (action != null) {
            PlayerStateManager.clearAllActions(player);
            String cancel = Config.getString("messages.cancelAction");
            cancel = cancel.replace("%action%", ActionMessages.getActionName(action));
            player.sendMessage(cancel);
        }

        if (hadPrompt) {
            PromptManager.cancelPrompt(player);
        }

        return true;
    }

    private boolean handleStatus(Player player) {
        if (player == null)
            return false;
        StatusService.sendStatus(player);
        return true;
    }

    private boolean handleOwnerAction(Player player, String[] args, boolean add) {
        if (player == null)
            return false;

        if (args.length < 2) {
            String syntax = add ? "/cl add <player> [on|off|toggle]" : "/cl remove <player> [on|off|toggle]";
            List<String> examples = add
                    ? Arrays.asList("/cl add Notch", "/cl add Notch toggle")
                    : Arrays.asList("/cl remove Notch", "/cl remove Notch toggle");
            HelpService.sendUsage(player, syntax, examples);
            return true;
        }

        ToggleState state = ToggleState.TOGGLE;
        if (args.length >= 3) {
            state = ToggleState.parse(args[2]);
            if (state == null) {
                HelpService.sendUsage(player,
                        add ? "/cl add <player> [on|off|toggle]" : "/cl remove <player> [on|off|toggle]",
                        Arrays.asList(add ? "/cl add Notch" : "/cl remove Notch"));
                return true;
            }
        }

        return add ? AddOwner.logic(player, args[1], state) : RemoveOwner.logic(player, args[1], state);
    }

    private boolean handleToggleAction(Player player, String[] args, PlayerActionType type) {
        if (player == null)
            return false;

        ToggleState state = ToggleState.TOGGLE;
        if (args.length >= 2) {
            state = ToggleState.parse(args[1]);
            if (state == null) {
                HelpService.sendUsage(player, "/cl " + typeName(type) + " [on|off|toggle]",
                        Arrays.asList("/cl " + typeName(type) + "", "/cl " + typeName(type) + " toggle"));
                return true;
            }
        }

        switch (type) {
        case CLAIM_LOCK:
            return ClaimLock.logic(player, state);
        case DESTROY_LOCK:
            return DestroyLock.logic(player, state);
        case MAKE_PUBLIC:
            return MakePublic.logic(player, state);
        default:
            return false;
        }
    }

    private boolean handleGroup(Player player, String[] args) {
        if (player == null)
            return false;

        if (args.length == 1) {
            if (shouldOpenGui()) {
                GroupMenu.open(player);
                return true;
            }
            HelpService.handleHelp(player, new String[] { "help", "groups" });
            return true;
        }

        String action = args[1].toLowerCase();
        switch (action) {
        case "create":
            if (args.length < 3) {
                HelpService.sendUsage(player, "/cl group create <name>", Arrays.asList("/cl group create Builders"));
                return true;
            }
            return GroupCommands.create(player, args[2]);
        case "delete":
            return GroupCommands.delete(player);
        case "invite":
        case "add":
            if (args.length < 3) {
                HelpService.sendUsage(player, "/cl group invite <player> [group]",
                        Arrays.asList("/cl group invite Notch", "/cl group invite Notch Builders"));
                return true;
            }
            return GroupCommands.invite(player, args[2], args.length > 3 ? args[3] : null);
        case "remove":
            if (args.length < 3) {
                HelpService.sendUsage(player, "/cl group remove <player> [group]",
                        Arrays.asList("/cl group remove Notch", "/cl group remove Notch Builders"));
                return true;
            }
            return GroupCommands.remove(player, args[2], args.length > 3 ? args[3] : null);
        case "accept":
            return GroupCommands.accept(player, args.length > 2 ? args[2] : null);
        case "decline":
            return GroupCommands.decline(player, args.length > 2 ? args[2] : null);
        case "invites":
            return GroupCommands.invites(player);
        case "leave":
            return GroupCommands.leave(player);
        case "list":
            return GroupCommands.list(player);
        case "select":
            if (args.length < 3) {
                HelpService.sendUsage(player, "/cl group select <group>", Arrays.asList("/cl group select Builders"));
                return true;
            }
            return GroupCommands.select(player, args[2]);
        case "menu":
            GroupMenu.open(player);
            return true;
        default:
            HelpService.handleHelp(player, new String[] { "help", "groups" });
            return true;
        }
    }

    private boolean handleBypass(Player player, String[] args) {
        if (player == null)
            return false;

        ToggleState state = ToggleState.TOGGLE;
        if (args.length >= 2) {
            state = ToggleState.parse(args[1]);
            if (state == null) {
                HelpService.sendUsage(player, "/cl bypass [on|off|toggle]",
                        Arrays.asList("/cl bypass", "/cl bypass off"));
                return true;
            }
        }

        return Bypass.logic(player, state);
    }

    private boolean shouldOpenGui() {
        return Config.getBoolean("settings.gui.enabled") && Config.getBoolean("settings.gui.command-opens");
    }

    private String typeName(PlayerActionType type) {
        switch (type) {
        case CLAIM_LOCK:
            return "claim";
        case DESTROY_LOCK:
            return "destroy";
        case MAKE_PUBLIC:
            return "public";
        default:
            return "";
        }
    }
}
