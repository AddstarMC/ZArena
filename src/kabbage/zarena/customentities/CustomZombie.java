package kabbage.zarena.customentities;

import java.lang.reflect.Field;

import kabbage.customentitylibrary.CustomEntityMoveEvent;
import kabbage.customentitylibrary.CustomEntityWrapper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.minecraft.server.v1_5_R2.Enchantment;
import net.minecraft.server.v1_5_R2.EnchantmentManager;
import net.minecraft.server.v1_5_R2.EntityArrow;
import net.minecraft.server.v1_5_R2.EntityHuman;
import net.minecraft.server.v1_5_R2.EntityLiving;
import net.minecraft.server.v1_5_R2.IRangedEntity;
import net.minecraft.server.v1_5_R2.ItemStack;
import net.minecraft.server.v1_5_R2.PathfinderGoalBreakDoor;
import net.minecraft.server.v1_5_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_5_R2.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_5_R2.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_5_R2.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_5_R2.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_5_R2.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_5_R2.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_5_R2.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_5_R2.PathfinderGoalSelector;
import net.minecraft.server.v1_5_R2.EntityZombie;

import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_5_R2.util.UnsafeList;

public class CustomZombie extends EntityZombie implements IRangedEntity
{
	private EntityTypeConfiguration type;
	
	private CustomZombie(World world, Location location, EntityTypeConfiguration type)
	{
		super(((CraftWorld) world).getHandle());
		this.setPosition(location.getX(), location.getY(), location.getZ());
		this.type = type;
	}
	
	@Override
	public void move(double d0, double d1, double d2)
	{
		CustomEntityMoveEvent event = new CustomEntityMoveEvent(this.myOwnDamnGetBukkitEntityMethodWithBlackjackAndHookers(), new Location(this.world.getWorld(), lastX, lastY, lastZ), new Location(this.world.getWorld(), locX, locY, locZ));
		if(event != null)
			Bukkit.getServer().getPluginManager().callEvent(event);
		super.move(d0, d1, d2);
	}

    //Do not drop loot
    protected int getLootId()
    {
        return -1;
    }

    //Do not drop rare items
    protected ItemStack l(int i)
    {
        return null;
    }
    
    @SuppressWarnings("rawtypes")
	private void resetPathfinders()
	{
		try
		{
			//Enable PathfinderGoalSelector's "a" field to be editable
			Field gsa = PathfinderGoalSelector.class.getDeclaredField("a");
			gsa.setAccessible(true);

			//Now take the instances goals/targets and set them as new lists so they can be rewritten
			gsa.set(this.goalSelector, new UnsafeList());
			gsa.set(this.targetSelector, new UnsafeList());
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		this.goalSelector.a(0, new PathfinderGoalFloat(this));
		this.goalSelector.a(1, new PathfinderGoalBreakDoor(this));
		if(type.useRanged())
			this.goalSelector.a(2, new PathFinderGoalCustomArrowAttack(this, this.bI, type.getShootDelay(), 1));
		if(type.useMelee())
			this.goalSelector.a(3, new PathfinderGoalMeleeAttack(this, EntityHuman.class, this.bI, false));
		this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, this.bI));
		this.goalSelector.a(5, new PathFinderGoalMoveToEntity(this, EntityHuman.class, this.bI, type.getRange()));
		this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, this.bI));
		this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 16.0F, 0, true));
	}
    
    public static CustomEntityWrapper spawn(Location location, EntityTypeConfiguration type)
	{
    	CustomZombie ent = new CustomZombie(location.getWorld(), location, type);
		if(((CraftWorld) location.getWorld()).getHandle().addEntity(ent, SpawnReason.CUSTOM))
		{
			CustomEntityWrapper wrapper = CustomEntityWrapper.spawnCustomEntity(ent, location, type);
			ent.resetPathfinders();
			return wrapper;
		}
		return null;
	}

	@Override
	public void a(EntityLiving arg0, float f1)
	{
		//Copied from EntitySkeleton class
		EntityArrow entityarrow = new EntityArrow(this.world, this, arg0, 1.6F, 12.0F);
        int i = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_DAMAGE.id, this.bG());
        int j = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK.id, this.bG());

        if (i > 0)
            entityarrow.b(entityarrow.c() + (double) i * 0.5D + 0.5D);

        if (j > 0)
            entityarrow.a(j);

        if (EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_FIRE.id, this.bG()) > 0)
            entityarrow.setOnFire(100);

        this.makeSound("random.bow", 1.0F, 1.0F / (this.aE().nextFloat() * 0.4F + 0.8F));
        this.world.addEntity(entityarrow);
	}
	
	//The actual getBukkitEntity method is being a whiny bitch and keeps insisting it doesn't exist...
	public CraftEntity myOwnDamnGetBukkitEntityMethodWithBlackjackAndHookers()
	{
		return CraftEntity.getEntity(this.world.getServer(), this);
	}
}
