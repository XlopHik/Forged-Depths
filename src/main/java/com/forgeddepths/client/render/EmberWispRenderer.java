package com.forgeddepths.client.render;

import com.forgeddepths.ForgedDepths;
import com.forgeddepths.client.model.EmberWispModel;
import com.forgeddepths.entity.EmberWispEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class EmberWispRenderer
		extends MobEntityRenderer<EmberWispEntity, LivingEntityRenderState, EmberWispModel> {
	private static final Identifier TEXTURE = ForgedDepths.id("textures/entity/ember_wisp.png");

	public EmberWispRenderer(EntityRendererFactory.Context context) {
		super(context, new EmberWispModel(context.getPart(FDModelLayers.EMBER_WISP)), 0.4F);
	}

	@Override
	protected int getBlockLight(EmberWispEntity entity, BlockPos pos) {
		return 15;
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	public Identifier getTexture(LivingEntityRenderState state) {
		return TEXTURE;
	}
}
