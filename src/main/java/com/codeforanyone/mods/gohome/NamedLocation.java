package com.codeforanyone.mods.gohome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;

public class NamedLocation {
	String name;
	double xpos;
	double ypos;
	double zpos;
	DimensionType dimensionType;

	public NamedLocation(String name, double xpos, double ypos, double zpos, DimensionType dimensionType) {
		super();
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
		this(locationName, player.posX, player.posY, player.posZ, player.dimension);
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
	 * This is a helper class to serializer/deserialize to a String format for easy
	 * persistence via the player DataManager.
	 */
	public static class NamedLocations {
		
		public static Map<String, String> serialize(Collection<NamedLocation> set) {
			List<String> names = new ArrayList<String>();
			List<String> xs = new ArrayList<String>();
			List<String> ys = new ArrayList<String>();
			List<String> zs = new ArrayList<String>();
			List<String> dims = new ArrayList<String>();

			for (NamedLocation key : set) {
				names.add(key.name);
				xs.add("" + key.getXpos());
				ys.add("" + key.getYpos());
				zs.add("" + key.getZpos());
				dims.add("" + key.dimensionType.getId());
			}

			Map<String, String> stringRepresentation = new HashMap<String, String>();
			stringRepresentation.put("names", String.join("|", names));
			stringRepresentation.put("posX", String.join(",", xs));
			stringRepresentation.put("posY", String.join(",", ys));
			stringRepresentation.put("posZ", String.join(",", zs));
			stringRepresentation.put("dims", String.join(",", dims));
			return stringRepresentation;
		}

		/**
		 * Splits text into a list, with sensible defaults that Java itself doesn't seem
		 * to have provided.
		 * 
		 * @param text
		 * @return
		 */
		static List<String> splitToList(String text, String delimiter) {
			if (text != null && text.contains(delimiter)) {
				return Arrays.asList(text.split(delimiter));
			}
			List<String> l = new ArrayList<String>();
			if (text != null) {
				l.add(text);
			}
			return l;
		}

		public static Set<NamedLocation> deserialize(Map<String, String> map) {
			List<String> names = splitToList(map.get("names"), "|");
			List<String> xs = splitToList(map.get("posX"), ",");
			List<String> ys = splitToList(map.get("posY"), ",");
			List<String> zs = splitToList(map.get("posZ"), ",");
			List<String> dims = splitToList(map.get("dims"), ",");

			// Names list is fine as strings. The others need conversion.
			// But we can do it on the fly while constructing.
			Set<NamedLocation> set = new HashSet<NamedLocation>();
			for (int i = 0; i < names.size(); i++) {
				set.add(new NamedLocation(names.get(i), 
						Double.parseDouble(xs.get(i)), 
						Double.parseDouble(ys.get(i)),
						Double.parseDouble(zs.get(i)), 
						DimensionType.getById(Integer.parseInt(dims.get(i)))));
			}
			return set;
		}

	}

}
