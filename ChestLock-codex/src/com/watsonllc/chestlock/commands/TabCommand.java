package com.watsonllc.chestlock.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.watsonllc.chestlock.logic.GroupController;

public class TabCommand implements TabCompleter {
    private static final List<String> TOGGLE_OPTIONS = Arrays.asList("on", "off", "toggle");
    private static final List<String> TAGHOPPER_RADII = Arrays.asList("16", "32", "64", "128", "256", "512");
    private static final List<String> GROUP_SUBCOMMANDS = Arrays.asList("create", "invite", "remove", "accept", "decline",
            "invites", "leave", "list", "select", "delete");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0)
            return Collections.emptyList();

        if (args.length == 1) {
            List<String> root = new ArrayList<>();
            root.add("help");
            root.add("status");
            root.add("cancel");

            if (canUse(sender, "chestlock.claim"))
                root.add("claim");
            if (canUse(sender, "chestlock.destroy"))
                root.add("destroy");
            if (canUse(sender, "chestlock.public"))
                root.add("public");
            if (canUse(sender, "chestlock.add"))
                root.add("add");
            if (canUse(sender, "chestlock.remove"))
                root.add("remove");

            if (canUseGroup(sender))
                root.add("group");

            if (canUse(sender, "chestlock.bypass"))
                root.add("bypass");
            if (canUse(sender, "chestlock.taghoppers"))
                root.add("taghoppers");

            return filterByPrefix(root, args[0]);
        }

        String sub = args[0].toLowerCase();
        if (args.length == 2) {
            switch (sub) {
            case "claim":
            case "destroy":
            case "public":
            case "bypass":
                return filterByPrefix(TOGGLE_OPTIONS, args[1]);
            case "add":
            case "remove":
                return filterByPrefix(getOnlinePlayers(sender), args[1]);
            case "group":
                return filterByPrefix(getGroupSubcommands(sender), args[1]);
            case "help":
                return filterByPrefix(Arrays.asList("1", "2", "locks", "owners", "groups", "admin"), args[1]);
            case "taghoppers":
                List<String> ownerOptions = new ArrayList<>();
                ownerOptions.add("SERVER");
                ownerOptions.addAll(getOnlinePlayers(sender));
                return filterByPrefix(ownerOptions, args[1]);
            default:
                return Collections.emptyList();
            }
        }

        if (args.length == 3) {
            switch (sub) {
            case "add":
            case "remove":
                return filterByPrefix(TOGGLE_OPTIONS, args[2]);
            case "group":
                return groupArg2Completions(sender, args);
            case "taghoppers":
                List<String> scopeOptions = new ArrayList<>(TAGHOPPER_RADII);
                for (World world : Bukkit.getWorlds()) {
                    scopeOptions.add(world.getName());
                }
                return filterByPrefix(scopeOptions, args[2]);
            default:
                return Collections.emptyList();
            }
        }

        if (args.length == 4) {
            if (sub.equals("group")) {
                return groupArg3Completions(sender, args);
            }
            if (sub.equals("taghoppers")) {
                return filterByPrefix(Collections.singletonList("minecarts"), args[3]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> groupArg2Completions(CommandSender sender, String[] args) {
        String sub = args[1].toLowerCase();
        GroupController groupController = new GroupController();
        if (!canUseGroupSub(sender, sub))
            return Collections.emptyList();
        if (sub.equals("invite") || sub.equals("remove")) {
            return filterByPrefix(getOnlinePlayers(sender), args[2]);
        }

        if (sub.equals("accept") || sub.equals("decline")) {
            if (sender instanceof Player) {
                return filterByPrefix(groupController.getInviteGroupsForPlayer(((Player) sender).getName()), args[2]);
            }
            return Collections.emptyList();
        }

        if (sub.equals("leave") || sub.equals("select")) {
            return filterByPrefix(getPlayerGroups(sender, groupController), args[2]);
        }

        return Collections.emptyList();
    }

    private List<String> groupArg3Completions(CommandSender sender, String[] args) {
        String sub = args[1].toLowerCase();
        if (!(sub.equals("invite") || sub.equals("remove")))
            return Collections.emptyList();
        if (!canUseGroupSub(sender, sub))
            return Collections.emptyList();

        GroupController groupController = new GroupController();
        List<String> groups = getPlayerGroups(sender, groupController);
        if (groups.size() == 1) {
            return filterByPrefix(groups, args[3]);
        }

        return filterByPrefix(groups, args[3]);
    }

    private boolean canUse(CommandSender sender, String permission) {
        if (!Commands.usePermissions())
            return true;
        return sender.hasPermission(permission);
    }

    private boolean canUseGroup(CommandSender sender) {
        if (!Commands.usePermissions())
            return true;

        return sender.hasPermission("chestlock.group.create") || sender.hasPermission("chestlock.group.delete")
                || sender.hasPermission("chestlock.group.invite") || sender.hasPermission("chestlock.group.remove")
                || sender.hasPermission("chestlock.group.accept") || sender.hasPermission("chestlock.group.decline")
                || sender.hasPermission("chestlock.group.invites") || sender.hasPermission("chestlock.group.leave")
                || sender.hasPermission("chestlock.group.list");
    }

    private List<String> getGroupSubcommands(CommandSender sender) {
        List<String> subcommands = new ArrayList<>();
        for (String sub : GROUP_SUBCOMMANDS) {
            if (canUseGroupSub(sender, sub))
                subcommands.add(sub);
        }
        return subcommands;
    }

    private boolean canUseGroupSub(CommandSender sender, String subcommand) {
        if (!Commands.usePermissions())
            return true;

        switch (subcommand) {
        case "create":
            return sender.hasPermission("chestlock.group.create");
        case "delete":
            return sender.hasPermission("chestlock.group.delete");
        case "invite":
            return sender.hasPermission("chestlock.group.invite");
        case "remove":
            return sender.hasPermission("chestlock.group.remove");
        case "accept":
            return sender.hasPermission("chestlock.group.accept");
        case "decline":
            return sender.hasPermission("chestlock.group.decline");
        case "invites":
            return sender.hasPermission("chestlock.group.invites");
        case "leave":
            return sender.hasPermission("chestlock.group.leave");
        case "list":
            return sender.hasPermission("chestlock.group.list");
        case "select":
            return canUseGroup(sender);
        default:
            return false;
        }
    }

    private List<String> getOnlinePlayers(CommandSender sender) {
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (sender instanceof Player && player.getName().equalsIgnoreCase(((Player) sender).getName()))
                continue;
            players.add(player.getName());
        }
        return players;
    }

    private List<String> getPlayerGroups(CommandSender sender, GroupController controller) {
        if (!(sender instanceof Player))
            return Collections.emptyList();

        Player player = (Player) sender;
        Set<String> groups = new LinkedHashSet<>();
        String owned = controller.getOwnedGroup(player.getName());
        if (owned != null)
            groups.add(owned);

        String member = controller.getGroupForPlayer(player.getName());
        if (member != null)
            groups.add(member);

        return new ArrayList<>(groups);
    }

    private List<String> filterByPrefix(List<String> options, String prefix) {
        String lower = prefix == null ? "" : prefix.toLowerCase();
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                matches.add(option);
            }
        }
        matches.sort(Comparator.naturalOrder());
        return matches;
    }
}
