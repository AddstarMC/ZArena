package com.github.zarena;


import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.zarena.utils.Constants;

public class PlayerStats implements Comparable<PlayerStats>
{
	private String player;
	private float money;
	private int points;
	private boolean alive;
	private int wavesSinceDeath;
	
	//Saved pre-game info
	private Location oldLocation;
	private ItemStack[] items;
	private ItemStack[] armor;
	private GameMode oldGameMode;
	private int oldLevel;
	
	public PlayerStats(Player player)
	{
		this.player = player.getName();
		money = 0;
		points = 0;
		alive = false;
		
		oldLocation = player.getLocation();
		items = player.getInventory().getContents();
		armor = player.getInventory().getArmorContents();
		oldGameMode = player.getGameMode();
		oldLevel = player.getLevel();
	}
	
	public void addMoney(double money)
	{
		if(usingVault())
			getEconomy().depositPlayer(player, money);
		else
			this.money += money;
		if(ZArena.getInstance().getConfig().getBoolean(Constants.XP_BAR_IS_MONEY))
			getPlayer().setLevel((int) getMoney());
	}
	
	public void addPoints(int points)
	{
		this.points += points;
	}
	
	public ItemStack[] getInventoryArmor()
	{
		return armor;
	}
	
	public ItemStack[] getInventoryContents()
	{
		return items;
	}
	
	public float getMoney()
	{
		return (float) (usingVault() ? getEconomy().getBalance(player) : money);
	}
	
	public GameMode getOldGameMode()
	{
		return oldGameMode;
	}
	
	public int getOldLevel()
	{
		return oldLevel;
	}
	
	public Location getOldLocation()
	{
		return oldLocation;
	}
	
	public Player getPlayer()
	{
		return Bukkit.getPlayer(player);
	}
	
	public int getPoints()
	{
		return points;
	}
	
	public int getWavesSinceDeath()
	{
		return wavesSinceDeath;
	}
	
	public boolean isAlive()
	{
		return alive;
	}
	
	public void messageStats()
	{
		getPlayer().sendMessage(ChatColor.DARK_GRAY+"Money: "+ChatColor.GRAY+getMoney()+ChatColor.DARK_GRAY+" Points: "+ChatColor.GRAY+getPoints());
	}
	
	public void resetStats()
	{
		money = 0;
		points = 0;
		wavesSinceDeath = 0;
	}
	
	public void setAlive(boolean alive)
	{
		this.alive = alive;
	}
	
	public void setWavesSinceDeath(int wavesSinceDeath)
	{
		this.wavesSinceDeath = wavesSinceDeath;
	}
	
	public void subMoney(double money)
	{
		if(usingVault())
			getEconomy().withdrawPlayer(player, money);
		else
		{
			this.money -= money;
			if(this.money < 0)
				this.money = 0;
		}
		if(ZArena.getInstance().getConfig().getBoolean(Constants.XP_BAR_IS_MONEY))
			getPlayer().setLevel((int) getMoney());
	}
	
	public void subPoints(int points)
	{
		this.points -= points;
	}

	@Override
	public int compareTo(PlayerStats stats)
	{
		if(stats.getPoints() > this.getPoints())
			return 1;
		else if(stats.getPoints() == this.getPoints())
		{
			if(stats.getMoney() > this.getMoney())
				return 1;
			else if(stats.getMoney() == this.getMoney())
			{
				if(this.getPlayer() == null && stats.getPlayer() == null)
					return 0;
				if(this.getPlayer() == null)
					return 1;
				if(stats.getPlayer() == null)
					return -1;
				if(stats.getPlayer().getHealth() > this.getPlayer().getHealth())
					return 1;
				else if(stats.getPlayer().getHealth() == this.getPlayer().getHealth())
					return 0;
				return -1;
			}
			return -1;
		}
		return -1;
	}
	
	private boolean usingVault()
	{
		return ZArena.getInstance().getConfig().getBoolean(Constants.USE_VAULT);
	}
	
	private Economy getEconomy()
	{
		return ZArena.getInstance().getEconomy();
	}
}
