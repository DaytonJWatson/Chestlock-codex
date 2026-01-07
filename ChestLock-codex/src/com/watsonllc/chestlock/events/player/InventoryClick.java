package com.watsonllc.chestlock.events.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.watsonllc.chestlock.config.Config;
import com.watsonllc.chestlock.gui.AdminMenu;
import com.watsonllc.chestlock.gui.ChestLockMenu;
import com.watsonllc.chestlock.gui.GroupMenu;
import com.watsonllc.chestlock.logic.ChestSorter;

public class InventoryClick implements Listener {
	
	private static boolean enabled = Config.getBoolean("settings.sortInventoryEnabled");
	private static String sortBy = Config.getString("settings.sortBy");
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(!enabled)
			return;

		String title = event.getView().getTitle();
		if (title.equals(ChestLockMenu.TITLE) || title.equals(GroupMenu.TITLE) || title.equals(AdminMenu.TITLE))
			return;
		
		if(sortBy.equalsIgnoreCase("type"))
			ChestSorter.sortByType(event);
		
		if(sortBy.equalsIgnoreCase("alphabetical"))
			ChestSorter.sortByAlphabetical(event);
	}
}
