package org.foo_projects.sofar.KidsTimeout;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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