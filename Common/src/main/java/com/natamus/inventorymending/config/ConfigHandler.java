package com.natamus.inventorymending.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.inventorymending.util.Reference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigHandler extends DuskConfig {
	public static HashMap<String, List<String>> configMetaData = new HashMap<String, List<String>>();

	@Entry public static boolean mendToolbarOnly = false;

	public static void initConfig() {
		configMetaData.put("mendToolbarOnly", Arrays.asList(
			"Whether only mending items in the toolbar should be repaired, instead of the entire inventory."
		));

		DuskConfig.init(Reference.NAME, Reference.MOD_ID, ConfigHandler.class);
	}
}