
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
	private List<String> moblist;
	
	public static final List<String> defaultMoblist = Collections.unmodifiableList(Arrays.asList("Cow", "Pig", "Sheep", "Chicken", "Villager", "VillagerGolem", "SnowMan", "MooshroomCow", "Wolf", "Ozelot", "EntityHorse"));

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new KidsTimeoutEntityListener(this), this);

		getCommand("timeout").setExecutor(new KidsTimeoutTimeoutCommand(this));
		getCommand("release").setExecutor(new KidsReleaseTimeoutCommand(this));
		getCommand("timeoutlength").setExecutor(new KidsTimeoutTimeoutLengthCommand(this));

		this.saveDefaultConfig();
		World world = getServer().getWorld(getConfig().getString("timeoutworld"));
		timeoutVector = getConfig().getVector("timeoutlocation", timeoutVector);
		timeoutLocation = timeoutVector.toLocation(world);

		world = getServer().getWorld(getConfig().getString("releaseworld"));
		releaseVector = getConfig().getVector("releaselocation", releaseVector);
		releaseLocation = releaseVector.toLocation(world);

		moblist = getConfig().getStringList("mobs");
		if (moblist.size() <= 0) {
			getConfig().set("mobs", defaultMoblist);
			saveConfig();
		}
		String msg = "KidsTimeout: tracking deaths for:";
		for (String mob : moblist)
			msg = msg + " " + mob;
		getLogger().info(msg);
	}

	public void setTimeoutLocation(final Player player) {
		Location location = player.getLocation();
		timeoutLocation = location;
		player.sendMessage("timeout location set to: " + location.getX() + "," + location.getY() + "," + location.getZ());
		Vector vector = location.toVector();
		getConfig().set("timeoutlocation", vector);
		getConfig().set("timeoutworld", location.getWorld().getName());
		saveConfig();
	}

	public void setReleaseLocation(final Player player) {
		Location location = player.getLocation();
		releaseLocation = location;
		player.sendMessage("release location set to: " + location.getX() + "," + location.getY() + "," + location.getZ());
		Vector vector = location.toVector();
		getConfig().set("releaselocation", vector);
		getConfig().set("releaseworld", location.getWorld().getName());
		saveConfig();
	}

	public void setTimeoutLength(final long length) {
		getConfig().set("timeoutlength", length);
		saveConfig();
	}
	
	public boolean isMobKillPunishable(Entity entity) {
		String name = entity.getType().getName();
		for (String mob : moblist) {
			if (mob.equals(name))
				return true;
		}
		return false;
	}

	public void doTimeout(final Player player) {
		if (player.hasPermission("kidstimeout") || player.isOp()) {
			player.sendMessage("You got away this time, next time they won't go so easy on you!");
			return;
		}
			
		if ((timeoutLocation.getY() == 0) || (releaseLocation.getY() == 0)) {
			// check if the plugin was configured
			player.sendMessage("You were caught red-handed! The authorities would like to");
			player.sendMessage("imprison you, but a prison was not yet built, so you are");
			player.sendMessage("free to go. You won't be so lucky the next time!");
			return;
		}

		player.sendMessage("You were caught red-handed! A prison sentence was passed to you!");
		player.sendMessage("You have been put in prison. Think about what you did.");
		player.teleport(timeoutLocation);
		releaseTask task = new releaseTask();
		task.player = player;
		task.releaseLocation = releaseLocation;
		task.illegalTeleportListener = new KidsTimeoutTeleportListener();
		task.illegalTeleportListener.player = player;
		getServer().getPluginManager().registerEvents(task.illegalTeleportListener, this);
		task.runTaskLater(this, getConfig().getLong("timeoutlength") * 20);
	}
}

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

class KidsTimeoutEntityListener implements Listener {
	private final KidsTimeout plugin;

	public KidsTimeoutEntityListener(KidsTimeout plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		Player killer = null;
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entity.getLastDamageCause();
				// track arrows back to the shooter
				if (entityDamageByEntityEvent.getDamager() instanceof Projectile) {
					Projectile projectile = (Projectile) entityDamageByEntityEvent.getDamager();
					Entity shooter = projectile.getShooter();
					if (shooter instanceof Player)
						killer = (Player)shooter;
				}
				if (entityDamageByEntityEvent.getDamager() instanceof Player)
					killer = (Player)entityDamageByEntityEvent.getDamager();
				if (killer != null) {
					if (plugin.isMobKillPunishable(entity)) {
						 plugin.getLogger().info("player " + killer.getName() + " killed a " + entity.getType().getName());
						 plugin.doTimeout(killer);
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
		if (split.length == 1) {
			Player baddie = plugin.getServer().getPlayer(split[0]);
			if (baddie == null) {
				sender.sendMessage("No such player!");
				return false;
			}
			plugin.doTimeout(baddie);
			return true;
		}

		if (!(sender instanceof Player))
			return false;

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

