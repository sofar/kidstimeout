
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
import org.bukkit.event.HandlerList;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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
		getCommand("timeoutlength").setExecutor(new KidsTimeoutTimeoutLengthCommand(this));

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
		player.sendMessage("timeout location set to: " + location.getX() + "," + location.getY() + "," + location.getZ());
		Vector vector = location.toVector();
		this.getConfig().set("timeoutlocation", vector);
		this.getConfig().set("timeoutworld", location.getWorld().getName());
		this.saveConfig();
	}

	public void setReleaseLocation(final Player player) {
		Location location = player.getLocation();
		releaseLocation = location;
		player.sendMessage("release location set to: " + location.getX() + "," + location.getY() + "," + location.getZ());
		Vector vector = location.toVector();
		this.getConfig().set("releaselocation", vector);
		this.getConfig().set("releaseworld", location.getWorld().getName());
		this.saveConfig();
	}

	public void setTimeoutLength(final long length) {
		this.getConfig().set("timeoutlength", length);
		this.saveConfig();
	}

	public void doTimeout(final Player player) {
		if (player.hasPermission("kidstimeout") || player.isOp())
			return;

		if ((timeoutLocation.getY() == 0) || (releaseLocation.getY() == 0)) {
			// check if the plugin was configured
			player.sendMessage("You were caught red-handed! The authorities would like to");
			player.sendMessage("imprison you, but a prison was not yet built, so you are");
			player.sendMessage("free to go. You won't be so lucky the next time!");
			return;
		}

		player.sendMessage("You've been caught in your crimes! A prison sentence was passed to you!");
		player.sendMessage("You have been teleported to prison");
		player.teleport(timeoutLocation);
		releaseTask task = new releaseTask();
		task.player = player;
		task.releaseLocation = releaseLocation;
		task.illegalTeleportListener = new KidsTimeoutTeleportListener();
		task.illegalTeleportListener.player = player;
		getServer().getPluginManager().registerEvents(task.illegalTeleportListener, this);
		task.runTaskLater(this, this.getConfig().getLong("timeoutlength") * 20);
	}
}

class releaseTask extends BukkitRunnable {
	public Player player;
	public Location releaseLocation;
	public KidsTimeoutTeleportListener illegalTeleportListener;

	public void run() {
		PlayerTeleportEvent.getHandlerList().unregister(illegalTeleportListener);
		HandlerList.unregisterAll(this.illegalTeleportListener);
		player.sendMessage("You have been released from prison");
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
					case MUSHROOM_COW:
						 killer.sendMessage(killer.getName() + " killed a " + entity.getType().getName() + "!");
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

class KidsTimeoutTimeoutLengthCommand implements CommandExecutor {
	private final KidsTimeout plugin;

	public KidsTimeoutTimeoutLengthCommand(KidsTimeout plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		if (split.length > 1)
			return false;
		
		if (split.length == 1)
			plugin.setTimeoutLength(Long.parseLong(split[0]));

		sender.sendMessage("Time-out length is " + plugin.getConfig().getLong("timeoutlength") + " seconds");
		return true;
	}
}

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

