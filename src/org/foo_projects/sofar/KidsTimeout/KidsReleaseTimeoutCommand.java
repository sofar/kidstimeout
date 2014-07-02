package org.foo_projects.sofar.KidsTimeout;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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