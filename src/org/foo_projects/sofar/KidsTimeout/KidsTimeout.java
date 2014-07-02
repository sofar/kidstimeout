
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class KidsTimeout extends JavaPlugin {

	private Location timeoutLocation;
	private Location releaseLocation;
	private Vector timeoutVector;
	private Vector releaseVector;
	private List<String> moblist;
	public boolean cancel;

	public static final List<String> defaultMoblist = Collections.unmodifiableList(Arrays.asList("Cow", "Pig", "Sheep", "Chicken", "Villager", "VillagerGolem", "SnowMan", "MooshroomCow", "Wolf", "Ozelot", "EntityHorse"));

	@Override
	public void onEnable() {
		cancel = getConfig().getBoolean("cancel");
		getLogger().info("Kill events will " + (cancel ? ""  : "not ") + "be cancelled");

		getServer().getPluginManager().registerEvents(new KidsTimeoutEntityListener(this, cancel), this);

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
		String name = entity.getType().name().toString();
		for (String mob : moblist) {
			if (mob.equalsIgnoreCase(name))
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
		// must exit any vehicle first, otherwise teleport fails
		player.leaveVehicle();
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

