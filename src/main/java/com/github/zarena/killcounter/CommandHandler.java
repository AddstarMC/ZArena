package main.java.com.github.zarena.killcounter;

import java.util.Map.Entry;

import main.java.com.github.zarena.commands.CommandSenderWrapper;
import main.java.com.github.zarena.commands.ECommand;
import main.java.com.github.zarena.utils.ChatHelper;
import main.java.com.github.zarena.utils.Utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandHandler
{
	private KillCounter kc;
	private CommandSenderWrapper senderWrapper;
	private ECommand command;
	
	public CommandHandler(CommandSender sender, ECommand command)
	{
		kc = KillCounter.instance;
		
		senderWrapper = new CommandSenderWrapper(sender);
		this.command = command;
	}
	
	public void addKills(String name, String amountString)
	{
		if(!senderWrapper.canControlKillCounter())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		int amount = Utils.parseInt(amountString, -1);
		if(amount < 0)
			senderWrapper.sendMessage(ChatColor.RED+"The amount to add kills to must be a valid integer greater than 0.");
		kc.setKills(name, kc.getKills(name) + amount);
		senderWrapper.sendMessage(ChatColor.GREEN+name+"'s kills successfully set to "+kc.getKills(name)+".");
	}

	public void sendTopPlayers()
	{
		int playersToShow = Utils.parseInt(command.getArgAtIndex(2), 5);
		if(playersToShow > 30)
			playersToShow = 30;
		senderWrapper.sendMessage(ChatColor.BLUE+"Top killers:");
		for(int i = 0; i < playersToShow; i++)
		{
			Entry<String, Integer> entry = kc.getEntry(i);
			if(entry == null) break;
			senderWrapper.sendMessage(ChatColor.RED+"#"+(i + 1)+": "+entry.getKey()+" - "+entry.getValue()+" kills");
		}
	}

	public void setKills(String name, String amountString)
	{
		if(!senderWrapper.canControlKillCounter())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		int amount = Utils.parseInt(amountString, -1);
		if(amount < 0)
			senderWrapper.sendMessage(ChatColor.RED+"The amount to set kills to must be a valid integer greater than 0.");
		kc.setKills(name, amount);
		senderWrapper.sendMessage(ChatColor.GREEN+name+"'s kills successfully set to "+amount+".");
		
	}

	public void sendPlayer(String name)
	{
		Integer kills = kc.getKills(name);
		if(kills == null)
		{
			senderWrapper.sendMessage(ChatColor.RED+"Player could not be found.");
			return;
		}
		int rank = kc.indexOf(name) + 1;
		senderWrapper.sendMessage(ChatColor.BLUE+"Player: "+ChatColor.WHITE+name);
		senderWrapper.sendMessage(ChatColor.RED+"Total Kills: "+ChatColor.WHITE+kills);
		senderWrapper.sendMessage(ChatColor.RED+"Rank: "+ChatColor.WHITE+rank+"/"+kc.mapSize());
	}
	
	public void subKills(String name, String amountString)
	{
		if(!senderWrapper.canControlKillCounter())
		{
			senderWrapper.sendMessage(ChatHelper.INSUFFICIENT_PERMISSIONS);
			return;
		}
		int amount = Utils.parseInt(amountString, -1);
		if(amount < 0)
			senderWrapper.sendMessage(ChatColor.RED+"The amount to subtract kills from must be a valid integer greater than 0.");
		kc.setKills(name, kc.getKills(name) - amount);
		senderWrapper.sendMessage(ChatColor.GREEN+name+"'s kills successfully set to "+kc.getKills(name)+".");
	}
}