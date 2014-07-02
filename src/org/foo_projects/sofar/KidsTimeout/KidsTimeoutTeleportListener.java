package org.foo_projects.sofar.KidsTimeout;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

class KidsTimeoutTeleportListener implements Listener {
	public Player player;
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void illegalTeleport(PlayerTeleportEvent event) {
		if (!event.getPlayer().getName().equals(player.getName()))
			return;
		if (event.isCancelled() == true)
			return;
		if (player.hasPermission("kidstimeout"))
			return;
		player.sendMessage("Your attempt to escape prison was foiled!");
		event.setCancelled(true);
		return;
	}
}