package com.codeforanyone.mods.gohome;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.codeforanyone.mods.gohome.NamedLocation.NamedLocations;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.SharedConstants;

public class GoHomeWorldSavedData {
	public static GoHomeWorldSavedData INSTANCE = new GoHomeWorldSavedData();

	Map<String, NamedLocation> globalNamedLocations = null;

	public GoHomeWorldSavedData() {
		globalNamedLocations = new HashMap<String, NamedLocation>();
	}
	
	public void load() {
		File fileIn = new File("GoHomeData.nbt");
		CompoundNBT compoundnbt = new CompoundNBT();

		try (FileInputStream fileinputstream = new FileInputStream(fileIn)) {
			compoundnbt = CompressedStreamTools.readCompressed(fileinputstream);
			this.read(compoundnbt.getCompound("data"));
			System.out.println("GoHome LOAD succeeded from " + fileIn.getAbsolutePath());
		} catch (IOException ioexception) {
			System.out.println("GoHome could not load data " + ioexception + " for file " + fileIn.getAbsolutePath());
		}
	}

	public void save() {
		File fileIn = new File("GoHomeData.nbt");
		CompoundNBT compoundnbt = new CompoundNBT();
		compoundnbt.put("data", this.write(new CompoundNBT()));
		compoundnbt.putInt("DataVersion", SharedConstants.getVersion().getWorldVersion());

		try (FileOutputStream fileoutputstream = new FileOutputStream(fileIn)) {
			CompressedStreamTools.writeCompressed(compoundnbt, fileoutputstream);
			System.out.println("GoHome SAVE succeeded writing to " + fileIn.getAbsolutePath());
		} catch (IOException ioexception) {
			System.out.println("GoHome could not save data " + ioexception + " for file " + fileIn.getAbsolutePath());
		}
	}

	public static GoHomeWorldSavedData getInstance() {
		return INSTANCE;
	}

	public void resetAllGlobal() {
		globalNamedLocations.clear();
	}

	/**
	 * Since this class holds and represents global named locations, which is a
	 * collection, we need to provide a proxy to the Map methods, like add, remove,
	 * find, and list. This one provides find.
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
	 * This persists the values from the in-memory java object into the compoundnbt.
	 * It takes the Map<String, NamedLocation> we have, serializes them into
	 * strings, and throws them into a Map temporarily for transit back here, where
	 * we can write them out to the nbt which gets saved to disk.
	 */
	public CompoundNBT write(CompoundNBT compound) {
		return NamedLocations.write(compound, globalNamedLocations);
	}

	/**
	 * This reads the values from the compound nbt, throws them into a map
	 * temporarily for transit, and then gets them deserialized into the Map<String,
	 * NamedLocation> that we need.
	 */
	public void read(CompoundNBT nbt) {
		globalNamedLocations = NamedLocations.read(nbt);
	}

}
