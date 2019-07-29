package com.codeforanyone.mods.gohome;

import java.util.Arrays;
import java.util.List;

public class GoSubcommand {
	String subcommandOrLocation;
	
	public GoSubcommand(String commandOrLocation) {
		this.subcommandOrLocation = commandOrLocation;
	}
	public static GoSubcommand fromString(String commandOrLocation) { 
		List<String> vals = Arrays.asList("add", "rm", "add-global", "rm-global", "home");
		if (vals.contains(commandOrLocation)) {
			return new GoSubcommand(commandOrLocation);
		} else {
			return null;
		}
	}

	public String getSubcommandOrLocation() {
		return subcommandOrLocation;
	}

	public void setSubcommandOrLocation(String subcommandOrLocation) {
		this.subcommandOrLocation = subcommandOrLocation;
	}
	
	public boolean isLocation() {
		return !isSubcommand();
	}
	public boolean isSubcommand() {
		if ("add".equalsIgnoreCase(subcommandOrLocation)) {
			return true;
		}
		if ("rm".equalsIgnoreCase(subcommandOrLocation)) {
			return true;
		}
		if ("add-global".equalsIgnoreCase(subcommandOrLocation)) {
			return true;
		}
		if ("rm-global".equalsIgnoreCase(subcommandOrLocation)) {
			return true;
		}
		if ("home".equalsIgnoreCase(subcommandOrLocation)) {
			return true;
		}
		return false;
	}
	
}
