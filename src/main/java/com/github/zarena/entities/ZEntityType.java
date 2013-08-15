package com.github.zarena.entities;

import com.github.customentitylibrary.entities.EntityType;

public interface ZEntityType extends EntityType
{
	public double getWorthModifier();
	
	public double getHealthModifier();
	
	public int getMinimumSpawnWave();

	public int getMaximumSpawnWave();
	
	public double getSpawnPriority();

	public String getBroadcastOnSpawn();
}
