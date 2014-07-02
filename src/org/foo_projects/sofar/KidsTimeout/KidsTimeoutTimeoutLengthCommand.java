package org.foo_projects.sofar.KidsTimeout;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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