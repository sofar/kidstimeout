
//
// timeoutEntityListener.java - monitor entity death events
//
// (C) Copyright 2013 - Auke Kok <auke@foo-projects.org>
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; version 3
// of the License.
//

package org.foo_projects.sofar.timeout;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class timeoutEntityListener implements Listener {
	private final timeout plugin;

	public timeoutEntityListener(timeout plugin) {
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
