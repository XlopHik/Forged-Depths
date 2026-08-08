package com.forgeddepths.client.render;

import com.forgeddepths.ForgedDepths;
import com.forgeddepths.client.model.AshenSmithModel;
import com.forgeddepths.entity.AshenSmithEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class AshenSmithRenderer
		extends BipedEntityRenderer<AshenSmithEntity, BipedEntityRenderState, AshenSmithModel> {
	private static final Identifier TEXTURE = ForgedDepths.id("textures/entity/ashen_smith.png");

	public AshenSmithRenderer(EntityRendererFactory.Context context) {
		super(context, new AshenSmithModel(context.getPart(FDModelLayers.ASHEN_SMITH)), 0.6F);
	}

	@Override
	public BipedEntityRenderState createRenderState() {
		return new BipedEntityRenderState();
	}

	@Override
	public Identifier getTexture(BipedEntityRenderState state) {
		return TEXTURE;
	}
}
