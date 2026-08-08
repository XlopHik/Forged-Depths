package com.forgeddepths.client.render;

import com.forgeddepths.ForgedDepths;
import com.forgeddepths.client.model.AshenSmithModel;
import com.forgeddepths.client.model.EmberWispModel;
import com.forgeddepths.client.model.SlagGolemModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;

@Environment(EnvType.CLIENT)
public final class FDModelLayers {
	private FDModelLayers() {
	}

	public static final EntityModelLayer ASHEN_SMITH = layer("ashen_smith");
	public static final EntityModelLayer EMBER_WISP = layer("ember_wisp");
	public static final EntityModelLayer SLAG_GOLEM = layer("slag_golem");

	private static EntityModelLayer layer(String name) {
		return new EntityModelLayer(ForgedDepths.id(name), "main");
	}

	public static void register() {
		EntityModelLayerRegistry.registerModelLayer(ASHEN_SMITH, AshenSmithModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(EMBER_WISP, EmberWispModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(SLAG_GOLEM, SlagGolemModel::getTexturedModelData);
	}
}
