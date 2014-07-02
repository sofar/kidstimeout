package org.foo_projects.sofar.KidsTimeout;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

class releaseTask extends BukkitRunnable {
	public Player player;
	public Location releaseLocation;
	public KidsTimeoutTeleportListener illegalTeleportListener;

	public void run() {
		PlayerTeleportEvent.getHandlerList().unregister(illegalTeleportListener);
		HandlerList.unregisterAll(illegalTeleportListener);
		player.sendMessage("You have been released from prison");
		player.teleport(releaseLocation);
	}
}