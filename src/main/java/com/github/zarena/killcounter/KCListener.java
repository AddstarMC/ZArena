package com.github.zarena.killcounter;

import com.github.customentitylibrary.entities.CustomEntityWrapper;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.PluginManager;

import com.github.zarena.ZArena;

public class KCListener implements Listener
{
	private KillCounter kc;
	
	public void registerEvents(PluginManager pm)
	{
		kc = KillCounter.instance;
		pm.registerEvents(this, ZArena.getInstance());
	}
	
	@EventHandler
	public void onEntityDeath (EntityDeathEvent event)
	{
		Entity entity = event.getEntity();
		if(CustomEntityWrapper.instanceOf(entity))
		{
			CustomEntityWrapper customEnt = CustomEntityWrapper.getCustomEntity(entity);
			Player player = customEnt.getBestAttacker();
			if(player != null)
				kc.addKill(player.getName());
		}
	}
}
