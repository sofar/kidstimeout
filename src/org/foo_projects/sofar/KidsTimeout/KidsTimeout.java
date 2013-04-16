
/*
 * KidsTimeout.java - Kids timeout plugin
 *
 * (C) Copyright 2013 - Auke Kok <auke@foo-projects.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3
 * of the License.
 */

package org.foo_projects.sofar.KidsTimeout;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public final class KidsTimeout extends JavaPlugin {

	private Location timeoutLocation;
	private Location releaseLocation;
	private Vector timeoutVector;
	private Vector releaseVector;

	@Override
	public void onEnable() {
		World world;

		getServer().getPluginManager().registerEvents(new KidsTimeoutEntityListener(this), this);

		getCommand("timeout").setExecutor(new KidsTimeoutTimeoutCommand(this));
		getCommand("release").setExecutor(new KidsReleaseTimeoutCommand(this));

		this.saveDefaultConfig();
		world = this.getServer().getWorld(this.getConfig().getString("timeoutworld"));
		timeoutVector = this.getConfig().getVector("timeoutlocation", timeoutVector);
		timeoutLocation = timeoutVector.toLocation(world);

		world = this.getServer().getWorld(this.getConfig().getString("releaseworld"));
		releaseVector = this.getConfig().getVector("releaselocation", releaseVector);
		releaseLocation = releaseVector.toLocation(world);

		getLogger().info("timeout enabled.");
	}

	public void setTimeoutLocation(final Player player) {
		Location location = player.getLocation();
		timeoutLocation = location;
		player.chat("timeout location set to: " + location.getX() + "," + location.getY() + "," + location.getZ());
		Vector vector = location.toVector();
		this.getConfig().set("timeoutlocation", vector);
		this.getConfig().set("timeoutworld", location.getWorld().getName());
		this.saveConfig();
	}

	public void setReleaseLocation(final Player player) {
		Location location = player.getLocation();
		releaseLocation = location;
		player.chat("release location set to: " + location.getX() + "," + location.getY() + "," + location.getZ());
		Vector vector = location.toVector();
		this.getConfig().set("releaselocation", vector);
		this.getConfig().set("releaseworld", location.getWorld().getName());
		this.saveConfig();
	}

	public void doTimeout(final Player player) {
		player.chat("You've been caught in your crimes! A prison sentence was passed to you!");
		player.chat("You have been teleported to prison");
		player.teleport(timeoutLocation);
		releaseTask task = new releaseTask();
		task.player = player;
		task.releaseLocation = releaseLocation;
		task.runTaskLater(this, this.getConfig().getLong("timeoutlength") * 20);
	}
}

class releaseTask extends BukkitRunnable {
	public Player player;
	public Location releaseLocation;

	public void run() {
		player.chat("You have been released from prison");
		player.teleport(releaseLocation);
	}
}

class KidsTimeoutEntityListener implements Listener {
	private final KidsTimeout plugin;

	public KidsTimeoutEntityListener(KidsTimeout plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {

		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entity.getLastDamageCause();
				if (entityDamageByEntityEvent.getDamager() instanceof Player) {
					Player killer = (Player)entityDamageByEntityEvent.getDamager();
 
					switch (entity.getType()) {
					case PIG:
					case CHICKEN:
					case COW:
					case SHEEP:
					case OCELOT:
					case WOLF:
					case VILLAGER:
					case SNOWMAN:
					case IRON_GOLEM:
						 killer.chat(killer.getName() + " killed a friendly entity!");
						 plugin.doTimeout(killer);
						 return;
					default:
						 return;
					}
				}
			}
		}
	}
}

class KidsReleaseTimeoutCommand implements CommandExecutor {
	private final KidsTimeout plugin;

	public KidsReleaseTimeoutCommand(KidsTimeout plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
	if (!(sender instanceof Player)) {
		return false;
	}

	Player player = (Player) sender;
	plugin.setReleaseLocation(player);
	return true;
	}

}

class KidsTimeoutTimeoutCommand implements CommandExecutor {
	private final KidsTimeout plugin;

	public KidsTimeoutTimeoutCommand(KidsTimeout plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
	if (!(sender instanceof Player)) {
		return false;
	}

	Player player = (Player) sender;
	plugin.setTimeoutLocation(player);
	return true;
	}

}

