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
public class EmberWispModel extends EntityModel<LivingEntityRenderState> {
	public static final int TEXTURE_WIDTH = 64;
	public static final int TEXTURE_HEIGHT = 32;

	private static final int SHARD_COUNT = 4;
	private static final float ORBIT_RADIUS = 5.5F;

	private final ModelPart core;
	private final ModelPart crown;
	private final ModelPart[] shards = new ModelPart[SHARD_COUNT];

	public EmberWispModel(ModelPart root) {
		super(root);
		this.core = root.getChild(EntityModelPartNames.HEAD);
		this.crown = root.getChild("crown");

		for (int i = 0; i < SHARD_COUNT; i++) {
			this.shards[i] = root.getChild(shardName(i));
		}
	}

	private static String shardName(int index) {
		return "shard" + index;
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();

		root.addChild(EntityModelPartNames.HEAD,
				ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
				ModelTransform.origin(0.0F, 0.0F, 0.0F));

		root.addChild("crown",
				ModelPartBuilder.create().uv(24, 0).cuboid(-4.0F, -1.0F, -4.0F, 8.0F, 2.0F, 8.0F),
				ModelTransform.origin(0.0F, -6.0F, 0.0F));

		ModelPartBuilder shard = ModelPartBuilder.create().uv(0, 12).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F);

		for (int i = 0; i < SHARD_COUNT; i++) {
			double angle = i * Math.PI * 2.0 / SHARD_COUNT;
			root.addChild(shardName(i), shard, ModelTransform.origin(
					(float) Math.cos(angle) * ORBIT_RADIUS, 1.0F, (float) Math.sin(angle) * ORBIT_RADIUS));
		}

		return TexturedModelData.of(data, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}

	@Override
	public void setAngles(LivingEntityRenderState state) {
		super.setAngles(state);

		core.yaw = state.relativeHeadYaw * (float) (Math.PI / 180.0);
		core.pitch = state.pitch * (float) (Math.PI / 180.0);

		crown.yaw = state.age * 0.06F;
		crown.originY = -6.0F + MathHelper.cos(state.age * 0.15F) * 0.6F;

		float spin = state.age * 0.12F;

		for (int i = 0; i < SHARD_COUNT; i++) {
			float angle = spin + i * (float) (Math.PI * 2.0 / SHARD_COUNT);
			shards[i].originX = MathHelper.cos(angle) * ORBIT_RADIUS;
			shards[i].originZ = MathHelper.sin(angle) * ORBIT_RADIUS;
			shards[i].originY = 1.0F + MathHelper.sin(state.age * 0.18F + i) * 1.6F;
			shards[i].yaw = -angle;
			shards[i].roll = MathHelper.sin(state.age * 0.1F + i) * 0.35F;
		}
	}
}
