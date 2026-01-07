package com.watsonllc.chestlock.commands.admin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.Utils;
import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.logic.HopperCache;
import com.watsonllc.chestlock.logic.HopperOwnerData;

public class TagHoppers {

        private static final int CHUNKS_PER_TICK = 8;

        public static boolean logic(CommandSender sender, String[] args) {
                if (!sender.hasPermission("chestlock.taghoppers")) {
                        sender.sendMessage(Config.getString("messages.noPermission"));
                        return false;
                }

                if (args.length < 3) {
                        sender.sendMessage(Config.getString("messages.invalidScope"));
                        sender.sendMessage("Usage: /cl taghoppers <SERVER|player> <radius|world> [minecarts]");
                        return false;
                }

                String ownerName = args[1];
                String scope = args[2];
                boolean includeMinecarts = args.length > 3 && args[3].equalsIgnoreCase("minecarts");

                Integer radius = parseRadius(scope);
                if (radius != null && radius <= 0) {
                        sender.sendMessage(Config.getString("messages.invalidRadius"));
                        return false;
                }

                World targetWorld = resolveWorld(sender, scope);
                if (targetWorld == null) {
                        sender.sendMessage(Config.getString("messages.invalidWorld"));
                        return false;
                }

                Location center = null;
                if (radius != null) {
                        if (!(sender instanceof Player)) {
                                sender.sendMessage(Config.getString("messages.invalidInstance"));
                                return false;
                        }
                        center = ((Player) sender).getLocation();
                }

                List<Chunk> matchingChunks = selectChunks(targetWorld, center, radius);

                if (matchingChunks.isEmpty()) {
                        sender.sendMessage(Config.getString("messages.invalidScope"));
                        return true;
                }

                HopperCache.invalidate();
                scheduleTagging(sender, ownerName, matchingChunks, includeMinecarts);
                return true;
        }

        private static World resolveWorld(CommandSender sender, String scope) {
                Integer radius = parseRadius(scope);
                if (radius != null) {
                        if (!(sender instanceof Player))
                                return null;
                        return ((Player) sender).getWorld();
                }

                return Bukkit.getWorld(scope);
        }

        private static Integer parseRadius(String scope) {
                try {
                        return Integer.parseInt(scope);
                } catch (NumberFormatException ex) {
                        return null;
                }
        }

        private static List<Chunk> selectChunks(World world, Location center, Integer radius) {
                List<Chunk> result = new ArrayList<>();
                int radiusSquared = radius == null ? 0 : radius * radius;

                for (Chunk chunk : world.getLoadedChunks()) {
                        if (center != null && radius != null) {
                                double dx = (chunk.getX() << 4) + 8 - center.getBlockX();
                                double dz = (chunk.getZ() << 4) + 8 - center.getBlockZ();
                                if ((dx * dx + dz * dz) > (radiusSquared))
                                        continue;
                        }
                        result.add(chunk);
                }

                return result;
        }

        private static void scheduleTagging(CommandSender sender, String ownerName, List<Chunk> chunks, boolean includeMinecarts) {
                final String normalizedOwner = ownerName.toUpperCase(Locale.ENGLISH).equals("SERVER") ? "SERVER" : ownerName;
                final Deque<Chunk> queue = new ArrayDeque<>(chunks);

                String started = Config.getString("messages.taghoppersStarted");
                started = started.replace("%chunks%", String.valueOf(queue.size()));
                sender.sendMessage(started);

                final int[] totals = new int[] { 0, 0 };
                final int[] ticks = new int[] { 0 };

                Bukkit.getScheduler().runTaskTimer(Main.instance, task -> {
                        int processed = 0;

                        while (!queue.isEmpty() && processed < CHUNKS_PER_TICK) {
                                Chunk chunk = queue.poll();
                                processed++;

                                for (BlockState state : chunk.getTileEntities()) {
                                        if (!(state instanceof Hopper))
                                                continue;

                                        Hopper hopper = (Hopper) state;
                                        if (HopperOwnerData.getOwner(hopper) != null)
                                                continue;

                                        HopperOwnerData.tagHopper(hopper.getBlock(), normalizedOwner);
                                        totals[0]++;
                                }

                                if (includeMinecarts) {
                                        for (Entity entity : chunk.getEntities()) {
                                                if (!(entity instanceof HopperMinecart))
                                                        continue;

                                                HopperMinecart minecart = (HopperMinecart) entity;

                                                if (HopperOwnerData.getOwner(minecart) != null)
                                                        continue;

                                                HopperOwnerData.tagHopperMinecart(minecart, normalizedOwner);
                                                totals[1]++;
                                        }
                                }
                        }

                        ticks[0]++;
                        if (ticks[0] % 20 == 0) {
                                String progress = Config.getString("messages.taghoppersProgress");
                                progress = progress.replace("%hoppers%", String.valueOf(totals[0]));
                                progress = progress.replace("%minecarts%", String.valueOf(totals[1]));
                                sender.sendMessage(progress);
                        }

                        if (queue.isEmpty()) {
                                String minecartsSegment = includeMinecarts ? Utils.color(" &7and &e" + totals[1] + " &7hopper minecarts") : "";
                                String done = Config.getString("messages.taghoppersDone");
                                done = done.replace("%hoppers%", String.valueOf(totals[0]));
                                done = done.replace("%minecarts%", minecartsSegment);
                                sender.sendMessage(done);
                                task.cancel();
                        }
                }, 0L, 1L);
        }
}
