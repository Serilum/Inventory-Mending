package com.serilum.inventorymending;

import com.natamus.collective.check.ShouldLoadCheck;
import com.serilum.inventorymending.util.Reference;
import net.fabricmc.api.ClientModInitializer;

public class ModFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() { 
		if (!ShouldLoadCheck.shouldLoad(Reference.MOD_ID)) {
			return;
		}

		registerEvents();
	}
	
	private void registerEvents() {

	}
}
