package com.forgeddepths.client.render;

import com.forgeddepths.ForgedDepths;
import com.forgeddepths.client.model.SlagGolemModel;
import com.forgeddepths.entity.SlagGolemEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SlagGolemRenderer
		extends MobEntityRenderer<SlagGolemEntity, LivingEntityRenderState, SlagGolemModel> {
	private static final Identifier TEXTURE = ForgedDepths.id("textures/entity/slag_golem.png");

	public SlagGolemRenderer(EntityRendererFactory.Context context) {
		super(context, new SlagGolemModel(context.getPart(FDModelLayers.SLAG_GOLEM)), 0.9F);
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
