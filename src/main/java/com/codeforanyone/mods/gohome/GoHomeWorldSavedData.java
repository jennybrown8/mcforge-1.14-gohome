package com.codeforanyone.mods.gohome;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.codeforanyone.mods.gohome.NamedLocation.NamedLocations;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

public class GoHomeWorldSavedData extends WorldSavedData {
	public static final GoHomeWorldSavedData INSTANCE = new GoHomeWorldSavedData();
	
	Map<String, NamedLocation> globalNamedLocations;

	public GoHomeWorldSavedData() {
		super("gohome");
		globalNamedLocations = new HashMap<String, NamedLocation>();
	}

	public GoHomeWorldSavedData(String s) {
		super(s);
		globalNamedLocations = new HashMap<String, NamedLocation>();
	}
	
	public void resetAllGlobal() {
		globalNamedLocations.clear();
	}
	
	/**
	 * Since this class holds and represents global named locations, which is a collection,
	 * we need to provide a proxy to the Map methods, like add, remove, find, and list.
	 * This one provides find.
	 */
	public NamedLocation getNamedLocation(String name) {
		return globalNamedLocations.get(name);
	}
	
	public boolean hasNamedLocation(String name) {
		return globalNamedLocations.containsKey(name);
	}
	
	/**
	 * Adds an item to our set
	 */
	public void addGlobalLocation(NamedLocation nl) {
		globalNamedLocations.put(nl.getName(), nl);
	}
	
	/**
	 * Removes if present, silent otherwise
	 */
	public void removeGlobalLocation(String name) {
		globalNamedLocations.remove(name);
	}
	
	/**
	 * Sorts alphabetically and returns the names
	 */
	public SortedSet<String> listGlobalLocations() {
		SortedSet<String> names = new TreeSet<String>();
		names.addAll(globalNamedLocations.keySet());
		return names;
	}
	
	

	/**
	 * This reads the values from the compound nbt, throws them into a map temporarily
	 * for transit, and then gets them deserialized into the Map<String, NamedLocation> that we need.
	 */
	@Override
	public void read(CompoundNBT nbt) {
		globalNamedLocations = NamedLocations.read(nbt);
	}

	/**
	 * This persists the values from the in-memory java object into the compoundnbt.
	 * It takes the Map<String, NamedLocation> we have, serializes them into strings, and
	 * throws them into a Map temporarily for transit back here, where we can write 
	 * them out to the nbt which gets saved to disk.
	 */
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		return NamedLocations.write(compound, globalNamedLocations);
	}

	
	

}
