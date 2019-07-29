package com.codeforanyone.mods.gohome;

import java.util.Arrays;
import java.util.List;

public class GoSubcommand {
	String subcommandOrLocation;

	public GoSubcommand(String commandOrLocation) {
		this.subcommandOrLocation = commandOrLocation;
	}

	public static GoSubcommand fromString(String commandOrLocation) {
		if (GoSubcommandArgument.VALID_SUBCOMMANDS.contains(commandOrLocation)) {
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
		return GoSubcommandArgument.VALID_SUBCOMMANDS.contains(subcommandOrLocation);
	}

}
