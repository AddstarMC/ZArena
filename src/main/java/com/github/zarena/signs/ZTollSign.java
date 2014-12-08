package com.github.zarena.signs;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.logging.Level;

import net.minecraft.server.v1_7_R4.BlockDoor;
import net.minecraft.server.v1_7_R4.BlockTrapdoor;
import net.minecraft.server.v1_7_R4.EntityPlayer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.github.zarena.ZArena;
import com.github.zarena.ZLevel;
import com.github.zarena.utils.LocationSer;
import com.github.zarena.utils.StringEnums;


public class ZTollSign extends ZSign implements Externalizable
{
	private static final long serialVersionUID = "ZTOLLSIGN".hashCode(); // DO NOT CHANGE
	private static final int VERSION = 4;

	private List<LocationSer> costBlockLocations; //The location(s) of the block that costs money to be used
	private boolean useableOnce;	//Whether or not this sign can only be used once
	private boolean opposite; 	//The sign's costblock starts out open/on, as opposed to closed/off
	private boolean noReset; 	//The sign's costblock doesn't reset, staying the same across multiple games
	private boolean active;
	private String name;

	public List<String> zSpawns = new ArrayList<String>();	//List of zSpawn names that are only active when this sign is active
	public List<LocationSer> oldZSpawns;

	/**
	 * Empty constructor for externalization.
	 */
	public ZTollSign()
	{
		resetCostBlocks();
	}

	public ZTollSign(ZLevel level, Location location, List<LocationSer> costBlockLocations, int price, String name, String[] flags)
	{
		super(level, LocationSer.convertFromBukkitLocation(location), price);
		this.costBlockLocations = costBlockLocations;
		this.name = name;
		active = false;
		for(String flag : flags)
		{
			try
			{
				switch(StringEnums.valueOf(flag.toUpperCase().replaceAll("-", "")))
				{
				case UO: case USABLEONCE:
					setUseableOnce(true);
					break;
				case OP: case OPPOSITE:
					setOpposite(true);
					break;
				case NR: case NORESET:
					setNoReset(true);
					break;
				default:
				}
			//Catch nonexistent flags
			} catch(Exception e){}
		}
	}

	public static ZTollSign attemptCreateSign(ZLevel level, Location location, String[] lines)
	{
		if((lines[3] == null) || (lines[3].isEmpty()))
			return null;

		int price;
		try
		{
			price = Integer.parseInt(lines[1]);
		} catch(NumberFormatException e) //Second line isn't a number, and can't have a price put to it
		{
			return null;
		}
		ZArena.log(Level.INFO, "Adding new TollSign: " + lines[3] + "...");
		List<LocationSer> locs = getTollableBlock(ZSign.getBlockOn(location).getLocation());
		if(locs == null || locs.isEmpty()) {
			ZArena.log(Level.WARNING, "TollSign has no togglable blocks! " + location);
			return null;
		}

		String[] flags = lines[2].split("\\s");

		return new ZTollSign(level, location, locs, price, lines[3], flags);
	}

	private boolean canBeUsed()
	{
		boolean usable = true;
		if(active && !opposite && useableOnce)
			usable = false;
		else if(!active && opposite && useableOnce)
			usable = false;
		return usable;
	}

	public boolean isCostBlock(Location loc)
	{
		Block b1 = loc.getBlock();
		Block b2 = null;
		if((costBlockLocations != null) && (!costBlockLocations.isEmpty())) {
			for (LocationSer cbl : costBlockLocations) {
				b2 = LocationSer.convertToBukkitLocation(cbl).getBlock();
				if (b1.equals(b2))
					return true;
			}
		}
		return false;
	}

	public String getName()
	{
		return name;
	}

	private static List<LocationSer> getTollableBlock(Location pos)
	{
		List<LocationSer> locs = new ArrayList<LocationSer>();
		double radius = 3.0;
		
		Block block = null;
		Location loc = new Location(pos.getWorld(), pos.getX(), pos.getY(), pos.getZ());
		Location loc1 = new Location(pos.getWorld(), pos.getX()-radius, pos.getY()-radius, pos.getZ()-radius);
		Location loc2 = new Location(pos.getWorld(), pos.getX()+radius, pos.getY()+radius, pos.getZ()+radius);

		boolean addblock = false;
		for(double x = loc1.getX(); x <= loc2.getX(); x++) {
			loc.setX(x);
            for(double y = loc1.getY(); y <= loc2.getY(); y++) {
            	loc.setY(y);
            	for(double z = loc1.getZ(); z <= loc2.getZ(); z++) {
            		loc.setZ(z);
                    block = loc.getBlock();
            		addblock = false;

            		if (block.getType() == Material.LEVER
    						|| block.getType() == Material.TRAP_DOOR
    						|| block.getType() == Material.WOOD_BUTTON
    						|| block.getType() == Material.STONE_BUTTON)
    					addblock = true;
    				else if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.IRON_DOOR_BLOCK) {
    					// We only want to add each door once (only top or bottom block, not both)
    					addblock = true;
    				}

    				if (addblock) {
	    				ZArena.log(Level.INFO, "Adding " + block.getType() + " ("
	    						+ block.getLocation().getBlockX() + ", " 
	    						+ block.getLocation().getBlockY() + ", "
	    						+ block.getLocation().getBlockZ() + ")");
	    					locs.add(LocationSer.convertFromBukkitLocation(loc));
    				}
                }
            }
        }
		return locs;
	}

	@Override
	public boolean executeClick(Player player)
	{
		for (LocationSer cbl : costBlockLocations) {
			Block b = LocationSer.convertToBukkitLocation(cbl).getBlock();
			if (!toggleCostBlock(player, b)) {
				return false;
			}
		}
		active = !active;	// Toggle state
		return true;
	}

	private boolean toggleCostBlock(Player player, Block costBlock) {
		net.minecraft.server.v1_7_R4.World nmsWorld = ((CraftWorld) costBlock.getWorld()).getHandle();
		net.minecraft.server.v1_7_R4.Block nmsBlock = nmsWorld.getType(costBlock.getX(), costBlock.getY(), costBlock.getZ());
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		switch(costBlock.getType())
		{
		case WOODEN_DOOR: case IRON_DOOR: case IRON_DOOR_BLOCK:
			if(!active && canBeUsed())
				((BlockDoor) nmsBlock).setDoor(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), true);
			else if(active && canBeUsed())
				((BlockDoor) nmsBlock).setDoor(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), false);
			else
				return false;
			return true;
		case TRAP_DOOR:
			if(!active && canBeUsed())
				((BlockTrapdoor) nmsBlock).setOpen(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), true);
			else if(active && canBeUsed())
				((BlockTrapdoor) nmsBlock).setOpen(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), false);
			else
				return false;
			return true;
		case LEVER:
			if(!active && canBeUsed())
				nmsBlock.interact(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), nmsPlayer, 0, 0f, 0f, 0f);
			else if(active && canBeUsed())
				nmsBlock.interact(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), nmsPlayer, 0, 0f, 0f, 0f);
			else
				return false;
			return true;
		case STONE_BUTTON: case WOOD_BUTTON:
			if(canBeUsed())
				nmsBlock.interact(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), nmsPlayer, 0, 0f, 0f, 0f);
			else
				return false;
			return true;
		default:
			return false;
		}
	}
	
	public boolean isActive()
	{
		return active;
	}

	public boolean isOpposite()
	{
		return opposite;
	}

	public boolean isNoReset()
	{
		return noReset;
	}

	public boolean isUseableOnce()
	{
		return useableOnce;
	}

	public void reload()
	{
		ZArena.log(Level.INFO, "Checking TollSign: " + name + "...");
		if(!(getLocation().getBlock().getState() instanceof Sign))
		{
			ZArena.log(Level.INFO, "The sign at "+location.toString()+" has been removed due to it's sign having been removed;");
			getLevel().removeZSign(this);
			return;
		}
		if((costBlockLocations == null) || (costBlockLocations.isEmpty()))
		{
			costBlockLocations = getTollableBlock(ZSign.getBlockOn(getSign().getLocation()).getLocation());
			if((costBlockLocations == null) || (costBlockLocations.isEmpty()))
			{
				ZArena.log(Level.INFO, "The sign at "+location.toString()+" has been removed due to the block it tolls having been removed.");
				getLevel().removeZSign(this);
			}
		}
	}

	public void resetCostBlocks()
	{
		if (costBlockLocations != null) {
			for (LocationSer cbl : costBlockLocations) {
				Block b = LocationSer.convertToBukkitLocation(cbl).getBlock();
				if (b != null)
					resetCostBlock(b);
			}
			active = opposite;
			ZArena.log(Level.INFO, "Reset CostBlocks for TollSign: " + name);
		}
	}

	private void resetCostBlock(Block costBlock) {
		if(noReset)
			return;

		if(costBlock == null)
			return;

		net.minecraft.server.v1_7_R4.World nmsWorld = ((CraftWorld) costBlock.getWorld()).getHandle();
		net.minecraft.server.v1_7_R4.Block nmsBlock = nmsWorld.getType(costBlock.getX(), costBlock.getY(), costBlock.getZ());
		switch(costBlock.getType())
		{
		case WOODEN_DOOR: case IRON_DOOR: case IRON_DOOR_BLOCK:
			((BlockDoor) nmsBlock).setDoor(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), opposite);
			break;
		case TRAP_DOOR:
			((BlockTrapdoor) nmsBlock).setOpen(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), opposite);
			break;
		case LEVER:
			if(8 - (nmsWorld.getData(costBlock.getX(), costBlock.getY(), costBlock.getZ()) & 8) != 8 && !opposite)
				nmsBlock.interact(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), null, 0, 0f, 0f, 0f);
			else if(8 - (nmsWorld.getData(costBlock.getX(), costBlock.getY(), costBlock.getZ()) & 8) == 8 && opposite)
				nmsBlock.interact(nmsWorld, costBlock.getX(), costBlock.getY(), costBlock.getZ(), null, 0, 0f, 0f, 0f);
			break;
		case STONE_BUTTON: case WOOD_BUTTON:
			break;
		default:
		}
	}

	public void setNoReset(boolean noReset)
	{
		this.noReset = noReset;
	}

	public void setOpposite(boolean opposite)
	{
		this.opposite = opposite;
	}

	public void setUseableOnce(boolean usable)
	{
		useableOnce = usable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		int ver = in.readInt();

		if(ver == 0)
		{
			costBlockLocations = new ArrayList<LocationSer>();
			active = false;
			name = generateString(new Random(), "abcdefghigsadjas", 10);
			zSpawns = new ArrayList<String>();
			useableOnce = false;
		}
		else if(ver == 1)
		{
			//costBlockLocation = (LocationSer) in.readObject();
			costBlockLocations = new ArrayList<LocationSer>();
			active = in.readBoolean();
			name = in.readUTF();
			zSpawns = new ArrayList<String>();
			useableOnce = false;
		}
		else if(ver == 2)
		{
			costBlockLocations = new ArrayList<LocationSer>();
			active = in.readBoolean();
			name = in.readUTF();
			oldZSpawns = (List<LocationSer>) in.readObject();
			zSpawns = new ArrayList<String>();
			useableOnce = false;
		}
		else if(ver == 3)
		{
			costBlockLocations = new ArrayList<LocationSer>();
			active = in.readBoolean();
			name = in.readUTF();
			oldZSpawns = (List<LocationSer>) in.readObject();
			zSpawns = new ArrayList<String>();
			useableOnce = in.readBoolean();
		}
		else if(ver == 4)
		{
			costBlockLocations = new ArrayList<LocationSer>();
			active = in.readBoolean();
			name = in.readUTF();
			zSpawns = (List<String>) in.readObject();
			useableOnce = in.readBoolean();
		}
		else
		{
			ZArena.log(Level.WARNING, "An unsupported version of a ZTollSign failed to load.");
			ZArena.log(Level.WARNING, "The ZSign at: "+location.toString()+" may not be operational.");
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeInt(VERSION);

		out.writeObject(costBlockLocations);
		out.writeBoolean(active);
		out.writeUTF(name);
		out.writeObject(zSpawns);
		out.writeBoolean(useableOnce);
	}

	private static String generateString(Random rng, String characters, int length)
	{
	    char[] text = new char[length];
	    for (int i = 0; i < length; i++)
	    {
	        text[i] = characters.charAt(rng.nextInt(characters.length()));
	    }
	    return new String(text);
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = super.serialize();
		map.put("Name", name);
		map.put("Tolled Block Locations", costBlockLocations);
		map.put("Useable Once", useableOnce);
		map.put("Opposite", opposite);
		map.put("No Reset", noReset);
		map.put("Active", active);

		String allZSpawns = "";
		for(String name : zSpawns)
		{
			allZSpawns += name + ",";
		}
		if(!allZSpawns.isEmpty())
			allZSpawns = allZSpawns.substring(0, allZSpawns.length() - 1);
		map.put("ZSpawns", allZSpawns);

		map.put("Class", ZTollSign.class.getName());

		return map;
	}

	@SuppressWarnings("unchecked")
	public static ZTollSign deserialize(Map<String, Object> map)
	{
		ZTollSign tollSign = new ZTollSign();
		tollSign.level = (String) map.get("Level");
		tollSign.location = (LocationSer) map.get("Location");
		tollSign.price = (Integer) map.get("Price");

		tollSign.name = (String) map.get("Name");
		tollSign.costBlockLocations = (List<LocationSer>) map.get("Tolled Block Locations");
		tollSign.useableOnce = (Boolean) map.get("Useable Once");
		tollSign.opposite = (Boolean) map.get("Opposite");
		tollSign.noReset = (Boolean) map.get("No Reset");
		tollSign.active = (Boolean) map.get("Active");

		String allZSpawns = (String) map.get("ZSpawns");
		for(String zSpawn : allZSpawns.split(","))
		{
			tollSign.zSpawns.add(zSpawn);
		}

		return tollSign;
	}
}
