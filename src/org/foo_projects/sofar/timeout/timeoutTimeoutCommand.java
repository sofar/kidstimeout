
//
// timeoutTimeoutCommand.java - configure the timeout location
//
// (C) Copyright 2013 - Auke Kok <auke@foo-projects.org>
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; version 3
// of the License.
//

package org.foo_projects.sofar.timeout;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class timeoutTimeoutCommand implements CommandExecutor {
	private final timeout plugin;

	public timeoutTimeoutCommand(timeout plugin) {
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
