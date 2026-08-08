package com.forgeddepths.client;

import com.forgeddepths.ForgedDepths;
import com.forgeddepths.network.HeatStatusPayload;
import com.forgeddepths.registry.FDBlocks;
import com.forgeddepths.registry.FDEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import com.forgeddepths.client.render.AshenSmithRenderer;
import com.forgeddepths.client.render.EmberWispRenderer;
import com.forgeddepths.client.render.FDModelLayers;
import com.forgeddepths.client.render.SlagGolemRenderer;
import net.minecraft.client.render.BlockRenderLayer;

@Environment(EnvType.CLIENT)
public class ForgedDepthsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.putBlocks(BlockRenderLayer.CUTOUT,
				FDBlocks.SOUL_SPIKES,
				FDBlocks.RUNE_TRAP);

		FDModelLayers.register();
		EntityRendererRegistry.register(FDEntities.ASHEN_SMITH, AshenSmithRenderer::new);
		EntityRendererRegistry.register(FDEntities.EMBER_WISP, EmberWispRenderer::new);
		EntityRendererRegistry.register(FDEntities.SLAG_GOLEM, SlagGolemRenderer::new);

		ClientPlayNetworking.registerGlobalReceiver(HeatStatusPayload.ID, (payload, context) ->
				context.client().execute(() ->
						HeatHudElement.update(payload.inside(), payload.protection(), payload.secondsLeft())));

		HudElementRegistry.attachElementAfter(VanillaHudElements.STATUS_EFFECTS,
				ForgedDepths.id("heat_overlay"), HeatHudElement::render);
	}
}
