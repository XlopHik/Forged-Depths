package com.forgeddepths;

import com.forgeddepths.command.ForgedDepthsCommands;
import com.forgeddepths.heat.HeatManager;
import com.forgeddepths.network.FDNetworking;
import com.forgeddepths.registry.FDBlocks;
import com.forgeddepths.registry.FDEntities;
import com.forgeddepths.registry.FDItemGroups;
import com.forgeddepths.registry.FDItems;
import com.forgeddepths.registry.FDStructures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;

public class ForgedDepths implements ModInitializer {
	public static final String MOD_ID = "forged_depths";

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		FDBlocks.register();
		FDItems.register();
		FDEntities.register();
		FDStructures.register();
		FDItemGroups.register();
		FDNetworking.registerCommon();
		HeatManager.register();

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> ForgedDepthsCommands.register(dispatcher));
	}
}
