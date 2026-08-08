package com.forgeddepths.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;

@Environment(EnvType.CLIENT)
public class AshenSmithModel extends BipedEntityModel<BipedEntityRenderState> {
	public static final int TEXTURE_WIDTH = 64;
	public static final int TEXTURE_HEIGHT = 64;

	public AshenSmithModel(ModelPart root) {
		super(root);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();

		ModelPartData head = root.addChild(EntityModelPartNames.HEAD,
				ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
				ModelTransform.origin(0.0F, -1.0F, 0.0F));

		head.addChild(EntityModelPartNames.HAT,
				ModelPartBuilder.create().uv(32, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.6F)),
				ModelTransform.NONE);

		root.addChild(EntityModelPartNames.BODY,
				ModelPartBuilder.create().uv(0, 16).cuboid(-5.0F, 0.0F, -3.0F, 10.0F, 12.0F, 6.0F),
				ModelTransform.origin(0.0F, -1.0F, 0.0F));

		root.addChild(EntityModelPartNames.RIGHT_ARM,
				ModelPartBuilder.create().uv(32, 16).cuboid(-4.0F, -2.0F, -2.5F, 5.0F, 12.0F, 5.0F),
				ModelTransform.origin(-6.0F, 1.0F, 0.0F));

		root.addChild(EntityModelPartNames.LEFT_ARM,
				ModelPartBuilder.create().uv(0, 34).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
				ModelTransform.origin(6.0F, 1.0F, 0.0F));

		root.addChild(EntityModelPartNames.RIGHT_LEG,
				ModelPartBuilder.create().uv(16, 34).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
				ModelTransform.origin(-2.2F, 12.0F, 0.0F));

		root.addChild(EntityModelPartNames.LEFT_LEG,
				ModelPartBuilder.create().uv(32, 34).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
				ModelTransform.origin(2.2F, 12.0F, 0.0F));

		return TexturedModelData.of(data, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}

	@Override
	public void setAngles(BipedEntityRenderState state) {
		super.setAngles(state);
		body.pitch += 0.12F;
		head.pitch += 0.10F;
	}
}
