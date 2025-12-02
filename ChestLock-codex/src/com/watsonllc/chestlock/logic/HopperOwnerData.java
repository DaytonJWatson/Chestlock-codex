package com.watsonllc.chestlock.logic;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.block.TileState;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import com.watsonllc.chestlock.Main;

public class HopperOwnerData {

        public static final NamespacedKey HOPPER_OWNER_KEY = new NamespacedKey(Main.instance, "hopper_owner");

        public static void tagHopper(Block block, String ownerName) {
                if (!(block.getState() instanceof TileState))
                        return;

                TileState state = (TileState) block.getState();
                setOwner(state, ownerName);
                state.update();
        }

        public static void tagHopperMinecart(HopperMinecart minecart, String ownerName) {
                setOwner(minecart, ownerName);
        }

        public static String getOwner(Hopper hopper) {
                return getOwner((PersistentDataHolder) hopper);
        }

        public static String getOwner(HopperMinecart minecart) {
                return getOwner((PersistentDataHolder) minecart);
        }

        private static void setOwner(PersistentDataHolder holder, String ownerName) {
                holder.getPersistentDataContainer().set(HOPPER_OWNER_KEY, PersistentDataType.STRING, ownerName);
        }

        private static String getOwner(PersistentDataHolder holder) {
                PersistentDataContainer container = holder.getPersistentDataContainer();

                if (!container.has(HOPPER_OWNER_KEY, PersistentDataType.STRING))
                        return null;

                return container.get(HOPPER_OWNER_KEY, PersistentDataType.STRING);
        }
}
