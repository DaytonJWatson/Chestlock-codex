package com.watsonllc.chestlock.logic;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.config.LocksData;

public class LockController {
        private static final BlockFace[] CHEST_FACES = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };

        public String getLockID(Location location) {
                List<String> activeLocks = LocksData.retrieveSubSections("Locks");

                for (int i = 0; i < activeLocks.size(); i++) {
                        String lockID = activeLocks.get(i);
                        String path = "Locks." + lockID;
                        if(matchesLocation(path, location))
                                return lockID;
                }
                return null;
        }

        private boolean matchesLocation(String path, Location target) {
                List<Location> locations = getLockLocations(path);
                for (Location loc : locations) {
                        if (target.equals(loc)) {
                                return true;
                        }
                }
                return false;
        }

        public boolean naturalBlock(Location location) {
                return getLockID(location) == null;
        }

        public List<String> getAllowedPlayers(Location location) {
                List<String> activeLocks = LocksData.retrieveSubSections("Locks");
		
		for (int i = 0; i < activeLocks.size(); i++) {
                        String lockID = activeLocks.get(i);
                        String path = "Locks." + lockID;

                        String world = (String) LocksData.get(path + ".location.world");
                        int x = (int) LocksData.get(path + ".location.x");
                        int y = (int) LocksData.get(path + ".location.y");
                        int z = (int) LocksData.get(path + ".location.z");
                        Location loc = new Location(Bukkit.getWorld(world), x, y, z);

                        @SuppressWarnings("unchecked")
                        List<String> allowedPlayers = (List<String>) LocksData.get(path + ".allowed");

                        if(location.equals(loc))
                                return allowedPlayers;
                        List<Location> locations = getLockLocations(path);
                        if(locations.contains(location))
                                return allowedPlayers;
                }

                return null;
        }

	public String getOwner(Location location){
		return getAllowedPlayers(location).get(0).toString();
	}

	public List<String> getLocks(Player player) {
		return LocksData.retrieveSubSections("Locks");
	}
	
	public boolean isPublic(Location location) {
		String LockID = getLockID(location);
		boolean isPublic = (boolean) LocksData.get("Locks." + LockID + ".public");
		
		return isPublic;
	}
	
	public void changePublicMode(Location location) {
		String LockID = getLockID(location);
		
		if(!isPublic(location)) {
			LocksData.set("Locks." + LockID + ".public", true);
			LocksData.save();
		} else {
			LocksData.set("Locks." + LockID + ".public", false);
			LocksData.save();
		}
	}
	
	public String getLockType(Location location) {
		String LockID = getLockID(location);
		String itemType = (String) LocksData.get("Locks." + LockID + ".type");
		
		return itemType;
	}
	
	public String getLockWorld(Location location) {
		String LockID = getLockID(location);
		String lockWorld = (String) LocksData.get("Locks." + LockID + ".world");
		
		return lockWorld;
	}
	
	public int getLockX(Location location) {
		String LockID = getLockID(location);
		int lockX = (int) LocksData.get("Locks." + LockID + ".x");
		
		return lockX;
	}
	
	public int getLockY(Location location) {
		String LockID = getLockID(location);
		int lockY = (int) LocksData.get("Locks." + LockID + ".y");
		
		return lockY;
	}
	
	public int getLockZ(Location location) {
		String LockID = getLockID(location);
		int lockZ = (int) LocksData.get("Locks." + LockID + ".z");
		
		return lockZ;
	}
	
	public boolean getAlertMode(Location location) {
		String LockID = getLockID(location);
		boolean alertMode = (boolean) LocksData.get("Locks." + LockID + ".intrusionAlert");
		
		return alertMode;
	}
	
	public void changeAlertMode(Location location) {
		String LockID = getLockID(location);
		
		if(getAlertMode(location) == false) {
			LocksData.set("Locks." + LockID + ".intrusionAlert", true);
			LocksData.save();
		} else {
			LocksData.set("Locks." + LockID + ".intrusionAlert", false);
			LocksData.save();
		}
	}

        public void createLock(Player player, Block block) {
                if(tryMergeDoubleChest(player, block))
                        return;

                String lockID = createLockID();

                String name = player.getName();

		List<String> allowedUsers = new ArrayList<>();
		allowedUsers.add(name);

		String type = block.getType().toString();
		Location blockLoc = block.getLocation();
		String world = blockLoc.getWorld().getName();
		int x = blockLoc.getBlockX();
		int y = blockLoc.getBlockY();
		int z = blockLoc.getBlockZ();

		String path = "Locks." + lockID;

		LocksData.set(path + ".public", false);
                LocksData.set(path + ".allowed", allowedUsers);
                LocksData.set(path + ".type", type);
                LocksData.set(path + ".intrusionAlert", false);
                LocksData.set(path + ".location.world", world);
                LocksData.set(path + ".location.x", x);
                LocksData.set(path + ".location.y", y);
                LocksData.set(path + ".location.z", z);
                List<String> storedLocations = new ArrayList<>();
                storedLocations.add(serializeLocation(blockLoc));
                LocksData.set(path + ".locations", storedLocations);

                LocksData.save();
        }

        private boolean tryMergeDoubleChest(Player player, Block block) {
                if (!block.getType().toString().contains("CHEST"))
                        return false;

                for (BlockFace face : CHEST_FACES) {
                        Block neighbor = block.getRelative(face);
                        if (!neighbor.getType().equals(block.getType()))
                                continue;

                        String lockID = getLockID(neighbor.getLocation());
                        if (lockID == null)
                                continue;

                        List<String> allowedPlayers = getAllowedPlayers(neighbor.getLocation());
                        if (allowedPlayers == null)
                                continue;

                        if (!allowedPlayers.contains(player.getName()) && !Main.bypassLocks.containsKey(player))
                                continue;

                        addLocationToLock(lockID, block.getLocation());
                        return true;
                }
                return false;
        }

        public void removeLock(String lockID) {
                LocksData.set("Locks." + lockID, null);
                LocksData.save();
        }
	
	public void removeOwner(Player owner, String target, Location location) {
		String LockID = getLockID(location);
		String path = "Locks." + LockID;
		
		if(getAllowedPlayers(location).contains(owner.getName())) {
			List<String> newOwners = getAllowedPlayers(location);
			newOwners.remove(target);
			LocksData.set(path + ".allowed", newOwners);
			LocksData.save();
		}
	}
	
	public void addOwner(Player owner, String target, Location location) {
		String LockID = getLockID(location);
		String path = "Locks." + LockID;
		
		if(getAllowedPlayers(location).contains(owner.getName())) {
			if(getAllowedPlayers(location).contains(target))
				return;
			List<String> newOwners = getAllowedPlayers(location);
			newOwners.add(target);
			LocksData.set(path + ".allowed", newOwners);
			LocksData.save();
		}
	}

        private static final String CHARACTERS = Config.getString("settings.lockID-characters");
        private static final int LENGTH = Config.getInt("settings.lockID-size");
        private static SecureRandom random = new SecureRandom();

	private String createLockID() {
		StringBuilder sb = new StringBuilder(LENGTH);
		for (int i = 0; i < LENGTH; i++) {
			sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
                }
                return sb.toString();
        }

        public void addLocationToLock(String lockID, Location location) {
                String path = "Locks." + lockID;
                List<String> storedLocations = getStoredLocationStrings(path);

                String serializedLocation = serializeLocation(location);
                if (!storedLocations.contains(serializedLocation))
                        storedLocations.add(serializedLocation);

                LocksData.set(path + ".locations", storedLocations);

                Location primary = deserializeLocation(storedLocations.get(0));
                if (primary != null) {
                        LocksData.set(path + ".location.world", primary.getWorld().getName());
                        LocksData.set(path + ".location.x", primary.getBlockX());
                        LocksData.set(path + ".location.y", primary.getBlockY());
                        LocksData.set(path + ".location.z", primary.getBlockZ());
                }
                LocksData.save();
        }

        private List<String> getStoredLocationStrings(String path) {
                @SuppressWarnings("unchecked")
                List<String> storedLocations = (List<String>) LocksData.get(path + ".locations");
                if (storedLocations == null) {
                        storedLocations = new ArrayList<>();
                        String world = (String) LocksData.get(path + ".location.world");
                        Object xObj = LocksData.get(path + ".location.x");
                        Object yObj = LocksData.get(path + ".location.y");
                        Object zObj = LocksData.get(path + ".location.z");
                        if (world != null && xObj != null && yObj != null && zObj != null) {
                                int x = (int) xObj;
                                int y = (int) yObj;
                                int z = (int) zObj;
                                storedLocations.add(world + ":" + x + ":" + y + ":" + z);
                        }
                }
                return storedLocations;
        }

        private List<Location> getLockLocations(String path) {
                List<Location> locations = new ArrayList<>();
                List<String> storedLocations = getStoredLocationStrings(path);
                for (String serialized : storedLocations) {
                        Location loc = deserializeLocation(serialized);
                        if (loc != null) {
                                locations.add(loc);
                        }
                }
                return locations;
        }

        private String serializeLocation(Location location) {
                return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
        }

        private Location deserializeLocation(String serialized) {
                String[] parts = serialized.split(":");
                if (parts.length != 4)
                        return null;

                String worldName = parts[0];
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                return new Location(Bukkit.getWorld(worldName), x, y, z);
        }
}