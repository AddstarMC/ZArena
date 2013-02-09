package kabbage.killcounter;

import java.util.Map.Entry;

import kabbage.zarena.commands.utils.CommandSenderWrapper;
import kabbage.zarena.commands.utils.ECommand;
import kabbage.zarena.utils.Utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandHandler
{
	private KillCounter plugin;
	private CommandSenderWrapper senderWrapper;
	private ECommand command;
	
	public CommandHandler(CommandSender sender, ECommand command)
	{
		plugin = KillCounter.instance;
		
		senderWrapper = new CommandSenderWrapper(sender);
		this.command = command;
	}

	public void sendTopPlayers()
	{
		int playersToShow = Utils.parseInt(command.getArgAtIndex(2), 5);
		senderWrapper.sendMessage(ChatColor.BLUE+"Top killers:");
		for(int i = 0; i < playersToShow; i++)
		{
			Entry<String, Integer> entry = plugin.getEntry(i);
			senderWrapper.sendMessage(ChatColor.RED+"#"+(i + 1)+": "+entry.getKey()+" - "+entry.getValue()+" kills");
		}
	}

	public void setKills(String name, String amountString)
	{
		int amount = Utils.parseInt(name, -1);
		if(amount < 0)
			senderWrapper.sendMessage(ChatColor.RED+"The amount to set kills to must be a valid integer greater than 0.");
		plugin.setKills(name, amount);
		senderWrapper.sendMessage(ChatColor.GREEN+name+"'s kills successfully set to "+amount+".");
		
	}

	public void sendPlayer(String name)
	{
		int kills = plugin.getKills(name);
		int rank = plugin.indexOf(name);
		senderWrapper.sendMessage(ChatColor.BLUE+"Player: "+ChatColor.WHITE+name);
		senderWrapper.sendMessage(ChatColor.RED+"Total Kills: "+ChatColor.WHITE+kills);
		senderWrapper.sendMessage(ChatColor.RED+"Rank: "+ChatColor.WHITE+rank+"/"+plugin.mapSize());
	}
}