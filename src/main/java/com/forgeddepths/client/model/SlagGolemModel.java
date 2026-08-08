package com.forgeddepths.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SlagGolemModel extends EntityModel<LivingEntityRenderState> {
	public static final int TEXTURE_WIDTH = 128;
	public static final int TEXTURE_HEIGHT = 64;

	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public SlagGolemModel(ModelPart root) {
		super(root);
		this.head = root.getChild(EntityModelPartNames.HEAD);
		this.body = root.getChild(EntityModelPartNames.BODY);
		this.rightArm = root.getChild(EntityModelPartNames.RIGHT_ARM);
		this.leftArm = root.getChild(EntityModelPartNames.LEFT_ARM);
		this.rightLeg = root.getChild(EntityModelPartNames.RIGHT_LEG);
		this.leftLeg = root.getChild(EntityModelPartNames.LEFT_LEG);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();

		root.addChild(EntityModelPartNames.HEAD,
				ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
				ModelTransform.origin(0.0F, -10.0F, -1.0F));

		root.addChild(EntityModelPartNames.BODY,
				ModelPartBuilder.create().uv(0, 16).cuboid(-8.0F, 0.0F, -5.0F, 16.0F, 18.0F, 10.0F),
				ModelTransform.origin(0.0F, -10.0F, 0.0F));

		root.addChild(EntityModelPartNames.RIGHT_ARM,
				ModelPartBuilder.create().uv(52, 16).cuboid(-2.5F, -2.0F, -2.5F, 5.0F, 26.0F, 5.0F),
				ModelTransform.origin(-9.5F, -8.0F, 0.0F));

		root.addChild(EntityModelPartNames.LEFT_ARM,
				ModelPartBuilder.create().uv(52, 16).mirrored().cuboid(-2.5F, -2.0F, -2.5F, 5.0F, 26.0F, 5.0F),
				ModelTransform.origin(9.5F, -8.0F, 0.0F));

		root.addChild(EntityModelPartNames.RIGHT_LEG,
				ModelPartBuilder.create().uv(72, 16).cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 16.0F, 6.0F),
				ModelTransform.origin(-4.0F, 8.0F, 0.0F));

		root.addChild(EntityModelPartNames.LEFT_LEG,
				ModelPartBuilder.create().uv(72, 16).mirrored().cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 16.0F, 6.0F),
				ModelTransform.origin(4.0F, 8.0F, 0.0F));

		return TexturedModelData.of(data, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}

	@Override
	public void setAngles(LivingEntityRenderState state) {
		super.setAngles(state);

		head.yaw = state.relativeHeadYaw * (float) (Math.PI / 180.0);
		head.pitch = state.pitch * (float) (Math.PI / 180.0);

		float swing = state.limbSwingAnimationProgress;
		float amplitude = Math.min(state.limbSwingAmplitude, 1.0F);

		rightLeg.pitch = -1.1F * triangleWave(swing, 13.0F) * amplitude;
		leftLeg.pitch = 1.1F * triangleWave(swing, 13.0F) * amplitude;
		rightArm.pitch = 0.9F * triangleWave(swing, 13.0F) * amplitude;
		leftArm.pitch = -0.9F * triangleWave(swing, 13.0F) * amplitude;

		rightArm.roll = 0.08F + MathHelper.cos(swing * 0.5F) * 0.05F * amplitude;
		leftArm.roll = -0.08F - MathHelper.cos(swing * 0.5F) * 0.05F * amplitude;
		body.roll = MathHelper.cos(swing * 0.5F) * 0.04F * amplitude;
	}

	private static float triangleWave(float progress, float scale) {
		return (Math.abs(progress % scale - scale * 0.5F) - scale * 0.25F) / (scale * 0.25F);
	}
}
