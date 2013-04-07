package com.github.zarena.spout;


import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.GenericButton;

import com.github.zarena.ZArena;
import com.github.zarena.utils.StringEnums;

public class ZOptionsButton extends GenericButton
{
	private ZArena plugin;
	public ZOptionsButton(String text)
	{
		super(text);
		plugin = ZArena.getInstance();
	}
	
	@Override
	public void onButtonClick(final ButtonClickEvent event)
	{
		PlayerOptions options = plugin.getPlayerOptionsHandler().getOptions(event.getPlayer().getName());
		Button button = event.getButton();
		boolean enableDisable = button.getText().endsWith("Enabled") ? false : true;
		switch(StringEnums.valueOf(button.getText().replaceAll("(Enabled|Disabled|\\W)", "").trim().replaceAll(" ", "_").toUpperCase()))
		{
		case VOTING_POPUP:
			options.votingScreenEnabled = enableDisable;
			break;
		case ZOMBIE_TEXTURES:
			options.zombieTexturesEnabled = enableDisable;
			break;
		case WAVE_COUNTER:
			options.waveCounterEnabled = enableDisable;
			break;
		default:
			return;
		}
		button.setText((enableDisable) ? "Disabled" : "Enabled");
		//Close and open to update the button change.
		event.getPlayer().getMainScreen().getActivePopup().close();
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				PlayerOptions options2 = plugin.getPlayerOptionsHandler().getOptions(event.getPlayer().getName());
				options2.openOptions();
			}
		},10L);
		
	}
}
