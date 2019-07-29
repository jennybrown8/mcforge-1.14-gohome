package com.codeforanyone.mods.gohome;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.DummyWorldSaveData;

public class GoHomeWorldSavedData extends WorldSavedData {
	public static final GoHomeWorldSavedData INSTANCE = new GoHomeWorldSavedData();

	String valtest;

	public GoHomeWorldSavedData() {
		super("gohome");
	}

	public GoHomeWorldSavedData(String s) {
		super(s);
	}

	/**
	 * This reads the values from the compound and sets them in the in-memory java
	 * object. It's basically gets.
	 */
	@Override
	public void read(CompoundNBT nbt) {
		valtest = nbt.getString("valtest");
	}

	/**
	 * This persists the values from the in-memory java object into the compoundnbt.
	 * It's a bunch of sets.
	 */
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putString("valtest", valtest);
		return compound;
	}

	public String getValtest() {
		return valtest;
	}

	public void setValtest(String valtest) {
		this.valtest = valtest;
		this.markDirty();
	}
	
	

}
