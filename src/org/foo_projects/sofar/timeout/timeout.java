
//
// timeout.java - timeout plugin
//
// (C) Copyright 2013 - Auke Kok <auke@foo-projects.org>
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; version 3
// of the License.
//

package org.foo_projects.sofar.timeout;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class timeout extends JavaPlugin {

	private Location timeoutLocation;
	private Location releaseLocation;
	private Vector timeoutVector;
	private Vector releaseVector;

	@Override
	public void onEnable() {
		World world;

		getServer().getPluginManager().registerEvents(new timeoutEntityListener(this), this);

		getCommand("timeout").setExecutor(new timeoutTimeoutCommand(this));
		getCommand("release").setExecutor(new releaseTimeoutCommand(this));

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

