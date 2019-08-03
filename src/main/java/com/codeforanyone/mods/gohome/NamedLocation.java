package com.codeforanyone.mods.gohome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.dimension.DimensionType;

public class NamedLocation {
	String name;
	double xpos;
	double ypos;
	double zpos;
	DimensionType dimensionType;

	/**
	 * Pretty basic, just represents a specific block in MC in a specific dimension, but
	 * with a saved name.
	 * @param name
	 * @param xpos
	 * @param ypos
	 * @param zpos
	 * @param dimensionType
	 */
	
	public NamedLocation(String name, double xpos, double ypos, double zpos, DimensionType dimensionType) {
		this.name = name;
		this.xpos = xpos;
		this.ypos = ypos;
		this.zpos = zpos;
		this.dimensionType = dimensionType;
	}
	
	/**
	 * Helper for reading the player's location in the way we need.
	 * @param player
	 * @param locationName
	 */
	public NamedLocation(String locationName, ServerPlayerEntity player) {
		this.name = locationName;
		this.xpos = player.posX;
		this.ypos = player.posY;
		this.zpos = player.posZ;
		this.dimensionType = player.dimension;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getXpos() {
		return xpos;
	}

	public void setXpos(double xpos) {
		this.xpos = xpos;
	}

	public double getYpos() {
		return ypos;
	}

	public void setYpos(double ypos) {
		this.ypos = ypos;
	}

	public double getZpos() {
		return zpos;
	}

	public void setZpos(double zpos) {
		this.zpos = zpos;
	}

	public DimensionType getDimensionType() {
		return dimensionType;
	}

	public void setDimensionType(DimensionType dimensionType) {
		this.dimensionType = dimensionType;
	}
	
	@Override
	public String toString() {
		return "NamedLocation [name=" + name + ", xpos=" + xpos + ", ypos=" + ypos + ", zpos=" + zpos
				+ ", dimensionType=" + dimensionType + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dimensionType == null) ? 0 : dimensionType.getId());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		long temp;
		temp = Double.doubleToLongBits(xpos);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(ypos);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(zpos);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NamedLocation other = (NamedLocation) obj;
		if (dimensionType == null) {
			if (other.dimensionType != null)
				return false;
		} else if (dimensionType.getId() != other.dimensionType.getId())
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(xpos) != Double.doubleToLongBits(other.xpos))
			return false;
		if (Double.doubleToLongBits(ypos) != Double.doubleToLongBits(other.ypos))
			return false;
		if (Double.doubleToLongBits(zpos) != Double.doubleToLongBits(other.zpos))
			return false;
		return true;
	}

	/**
	 * This is a helper class to serialize/deserialize to a String format for easy
	 * persistence via the player DataManager.
	 */
	public static class NamedLocations {
		
		public static final char SERIALIZATION_DELIMITER = '|';
		private static final String KEY_DIMS = "dims";
		private static final String KEY_POS_Z = "posZ";
		private static final String KEY_POS_Y = "posY";
		private static final String KEY_POS_X = "posX";
		private static final String KEY_NAMES = "names";
		public static final List<String> NBT_KEYS = Arrays.asList(KEY_NAMES, KEY_POS_X, KEY_POS_Y, KEY_POS_Z, KEY_DIMS);

		static Map<String, String> serialize(Map<String, NamedLocation> map) {
			List<String> names = new ArrayList<String>();
			List<String> xs = new ArrayList<String>();
			List<String> ys = new ArrayList<String>();
			List<String> zs = new ArrayList<String>();
			List<String> dims = new ArrayList<String>();

			for (String key : map.keySet()) {
				names.add(map.get(key).getName());
				xs.add("" + map.get(key).getXpos());
				ys.add("" + map.get(key).getYpos());
				zs.add("" + map.get(key).getZpos());
				dims.add("" + map.get(key).getDimensionType().getId());
			}

			Map<String, String> stringRepresentation = new HashMap<String, String>();
			if (! map.isEmpty()) {
				stringRepresentation.put(KEY_NAMES, String.join("" + SERIALIZATION_DELIMITER, names));
				stringRepresentation.put(KEY_POS_X, String.join("" + SERIALIZATION_DELIMITER, xs));
				stringRepresentation.put(KEY_POS_Y, String.join("" + SERIALIZATION_DELIMITER, ys));
				stringRepresentation.put(KEY_POS_Z, String.join("" + SERIALIZATION_DELIMITER, zs));
				stringRepresentation.put(KEY_DIMS, String.join("" + SERIALIZATION_DELIMITER, dims));
			}
			return stringRepresentation;
		}

		/**
		 * Splits text into a list, with sensible defaults that Java itself doesn't seem
		 * to have provided.
		 * 
		 * @param text
		 * @return
		 */
		static List<String> splitToList(String text, char delimiter) {
			if (text != null && text.indexOf(delimiter) > -1) {
				StringTokenizer st = new StringTokenizer(text, ""+ delimiter);
				List<String> parts = new ArrayList<String>();
				while(st.hasMoreTokens()) {
					parts.add(st.nextToken());
				}
				return parts;
			}
			List<String> l = new ArrayList<String>();
			if (text != null && !"".equals(text.trim())) {
				l.add(text.trim());
			}
			return l;
		}	

		
		/**
		 * Makes a map printable, basically this is a clumsy toString method.
		 * @param map
		 * @return
		 */
		static String mapToString(Map<String, String> map) {
			StringBuffer sb = new StringBuffer("\n");
			for (String k : map.keySet()) {
				sb.append(k + "\t" + map.get(k) + "\n");
			}
			return sb.toString();
		}

		/**
		 * Reads from our chosen serialization format.
		 * @param map
		 * @return
		 */
		static Map<String, NamedLocation> deserialize(Map<String, String> map) {
			List<String> names = splitToList(map.get(KEY_NAMES), SERIALIZATION_DELIMITER);
			List<String> posX = splitToList(map.get(KEY_POS_X), SERIALIZATION_DELIMITER);
			List<String> posY = splitToList(map.get(KEY_POS_Y), SERIALIZATION_DELIMITER);
			List<String> posZ = splitToList(map.get(KEY_POS_Z), SERIALIZATION_DELIMITER);
			List<String> dims = splitToList(map.get(KEY_DIMS), SERIALIZATION_DELIMITER);

			// Names list is fine as strings. The others need conversion.
			// But we can do it on the fly while constructing.
			Map<String, NamedLocation> namedLocations = new HashMap<String, NamedLocation>();
			for (int i = 0; i < posX.size(); i++) {
				namedLocations.put(names.get(i), 
						new NamedLocation(names.get(i), 
						Double.parseDouble(posX.get(i)), 
						Double.parseDouble(posY.get(i)),
						Double.parseDouble(posZ.get(i)), 
						DimensionType.getById(Integer.parseInt(dims.get(i)))));
			}
			return namedLocations;
		}
		
		/**
		 * This reads the values from the compound nbt, throws them into a map temporarily
		 * for transit, and then gets them deserialized into the Map<String, NamedLocation> that we need.
		 */
		public static Map<String, NamedLocation> read(CompoundNBT nbt) {
			Map<String, String> locs = new HashMap<String, String>();
			if (nbt.contains(NBT_KEYS.get(0))) {
				for (String key : NBT_KEYS) {
					locs.put(key, nbt.getString(key));
				}
				return NamedLocations.deserialize(locs);
			} else {
				return new HashMap<String, NamedLocation>();
			}
		}
		
		/**
		 * This persists the values from the in-memory java object into the compoundnbt.
		 * It takes the Map<String, NamedLocation> we have, serializes them into strings, and
		 * throws them into a Map temporarily for transit back here, where we can write 
		 * them out to the nbt which gets saved to disk.
		 */
		public static CompoundNBT write(CompoundNBT compound, Map<String, NamedLocation> namedLocations) {
			Map<String, String> locs = NamedLocations.serialize(namedLocations);
			for (String key : NBT_KEYS) {
				compound.putString(key, locs.getOrDefault(key, ""));
			}
			return compound;
		}
		
		
	}

}
