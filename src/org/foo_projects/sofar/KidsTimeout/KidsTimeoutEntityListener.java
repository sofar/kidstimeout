package org.foo_projects.sofar.KidsTimeout;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

class KidsTimeoutEntityListener implements Listener {
	private final KidsTimeout plugin;
	private boolean cancel;

	public KidsTimeoutEntityListener(KidsTimeout plugin, boolean cancel) {
		this.plugin = plugin;
		this.cancel = cancel;
	}

	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		Player killer = null;
		Entity entity = event.getEntity();

		if (entity instanceof Player)
			return;

		if (event.getDamage() < ((Damageable)entity).getHealth())
			return;

		if (!plugin.isMobKillPunishable(entity))
			return;

		// track arrows back to the shooter
		if (event.getDamager() instanceof Projectile) {
			Projectile projectile = (Projectile) event.getDamager();
			ProjectileSource source = (ProjectileSource) projectile.getShooter();
			if (!(source instanceof Player))
				return;
			killer = (Player)source;
		}

		if (event.getDamager() instanceof Player)
			killer = (Player)event.getDamager();

		if (killer != null) {
			if (cancel && (!killer.isOp()))
				event.setCancelled(true);

			plugin.getLogger().info("player " + killer.getName() + " killed a " + entity.getType().name().toString());
			plugin.doTimeout(killer);
		}
	}
}