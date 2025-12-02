package com.watsonllc.chestlock.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Player;

import com.watsonllc.chestlock.config.LocksData;

public class GroupManager {

    private static final String GROUPS_PATH = "Groups";

    public static boolean createGroup(Player owner, String groupName) {
        String normalizedName = normalize(groupName);
        if (LocksData.get(GROUPS_PATH + "." + normalizedName) != null) {
            return false;
        }

        List<String> members = new ArrayList<>();
        members.add(owner.getName());

        LocksData.set(GROUPS_PATH + "." + normalizedName + ".owner", owner.getName());
        LocksData.set(GROUPS_PATH + "." + normalizedName + ".members", members);
        LocksData.save();
        return true;
    }

    public static boolean inviteToGroup(String groupName, String inviter, String target) {
        String normalizedName = normalize(groupName);
        if (!isOwner(normalizedName, inviter)) {
            return false;
        }

        List<String> members = getMembers(normalizedName);
        if (members.contains(target)) {
            return false;
        }

        members.add(target);
        LocksData.set(GROUPS_PATH + "." + normalizedName + ".members", members);
        LocksData.save();
        return true;
    }

    public static boolean leaveGroup(String groupName, String playerName) {
        String normalizedName = normalize(groupName);
        List<String> members = getMembers(normalizedName);
        if (!members.contains(playerName)) {
            return false;
        }

        members.remove(playerName);

        if (members.isEmpty() || isOwner(normalizedName, playerName)) {
            LocksData.set(GROUPS_PATH + "." + normalizedName, null);
        } else {
            LocksData.set(GROUPS_PATH + "." + normalizedName + ".members", members);
        }
        LocksData.save();
        return true;
    }

    public static boolean disbandGroup(String groupName, String owner) {
        String normalizedName = normalize(groupName);
        if (!isOwner(normalizedName, owner)) {
            return false;
        }

        LocksData.set(GROUPS_PATH + "." + normalizedName, null);
        LocksData.save();
        return true;
    }

    public static boolean shareGroup(String playerOne, String playerTwo) {
        List<String> groups = LocksData.retrieveSubSections(GROUPS_PATH);
        for (String group : groups) {
            List<String> members = getMembers(group);
            if (members.contains(playerOne) && members.contains(playerTwo)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> listGroups(String owner) {
        List<String> ownerGroups = new ArrayList<>();
        List<String> groups = LocksData.retrieveSubSections(GROUPS_PATH);
        for (String group : groups) {
            if (isOwner(group, owner)) {
                ownerGroups.add(group);
            }
        }
        return ownerGroups;
    }

    public static boolean groupExists(String groupName) {
        return LocksData.get(GROUPS_PATH + "." + normalize(groupName)) != null;
    }

    private static boolean isOwner(String groupName, String player) {
        String normalizedName = normalize(groupName);
        String owner = (String) LocksData.get(GROUPS_PATH + "." + normalizedName + ".owner");
        return player.equals(owner);
    }

    @SuppressWarnings("unchecked")
    private static List<String> getMembers(String groupName) {
        String normalizedName = normalize(groupName);
        List<String> members = (List<String>) LocksData.get(GROUPS_PATH + "." + normalizedName + ".members");
        if (members == null) {
            members = new ArrayList<>();
        }
        return new ArrayList<>(new HashSet<>(members));
    }

    private static String normalize(String groupName) {
        return groupName.toLowerCase();
    }
}
