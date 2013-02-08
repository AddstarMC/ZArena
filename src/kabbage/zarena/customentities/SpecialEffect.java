package kabbage.zarena.customentities;

import java.util.ConcurrentModificationException;

import kabbage.customentitylibrary.CustomEntityWrapper;
import kabbage.zarena.utils.StringEnums;
import kabbage.zarena.utils.Utils;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpecialEffect
{
	public static final String POTION = "potion";
	public static final String POTION_RADIUS = "potionradius";
	public static final String FIRE = "fire";
	public static final String ENDER_SIGNAL = "endersignal";
	public static final String SPAWNER_FLAMES = "spawnerflames";
	public static final String SMOKE = "smoke";
	
	private String name;
	private String[] args;
	
	public SpecialEffect(String name, String[] args)
	{
		this.name = name;
		this.args = args;
	}
	
	public void showEffect(LivingEntity entity)
	{
		Location source = entity.getLocation();
		PotionEffectType potionType = null;
		switch(StringEnums.valueOf(name.toUpperCase().replaceAll(" ", "")))
		{
		case POTION:
			potionType = PotionEffectType.getByName(args[0].toUpperCase().replaceAll(" ", "_"));
			if(potionType != null)
				entity.addPotionEffect(new PotionEffect(potionType, Utils.parseInt(args[1], 100), Utils.parseInt(args[2], 1)));
			break;
		case POTIONRADIUS:
			potionType = PotionEffectType.getByName(args[1].toUpperCase().replaceAll(" ", "_"));
			PotionEffect effect = null;
			if(potionType != null)
				effect = new PotionEffect(potionType, Utils.parseInt(args[2], 100), Utils.parseInt(args[3], 1));
			if(potionType == null || effect == null)
				break;
			for(Player p: source.getWorld().getPlayers())
			{
				if(source.distance(p.getLocation()) <= Utils.parseInt(args[0], 16))
				{
					if(!p.hasPotionEffect(potionType))
						p.sendMessage(ChatColor.RED+"A "+CustomEntityWrapper.getCustomEntity(entity).getName()+" is nearby! It's presence begins to effect you.");
					p.removePotionEffect(potionType);
					p.addPotionEffect(effect);
				}
			}
			break;
		case FIRE:
			entity.setFireTicks(Utils.parseInt(args[0], 60));
			break;
		case ENDERSIGNAL:
			for(int i = 0; i <= Utils.parseInt(args[0], 1); i++)
			{
				try
				{
					source.getWorld().playEffect(source, Effect.ENDER_SIGNAL, i);
				} catch(ConcurrentModificationException e)	//This happens sometimes, for whatever reason...it has no ill effects other than an annoying stack trace
				{}	//which is gotten rid of with this catch
			}
			break;
		case SPAWNERFLAMES:
			for(int i = 0; i < Utils.parseInt(args[0], 1); i++)
			{
				try
				{
					source.getWorld().playEffect(source, Effect.MOBSPAWNER_FLAMES, 64, 64);
				} catch(ConcurrentModificationException e)	//This happens sometimes, for whatever reason...it has no ill effects other than an annoying stack trace
				{}	//which is gotten rid of with this catch
			}
			break;
		case SMOKE:
			for(int j = 0; j < Utils.parseInt(args[0], 1); j++)
			{
				for(int i = 0; i < 8; i++)
				{
					try
					{
						source.getWorld().playEffect(source, Effect.SMOKE, i);
					} catch(ConcurrentModificationException e)	//This happens sometimes, for whatever reason...it has no ill effects other than an annoying stack trace
					{}	//which is gotten rid of with this catch
				}
			}
			break;
		default:
		}
	}
}
