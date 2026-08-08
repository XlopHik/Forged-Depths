package com.forgeddepths.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class EmberWispEntity extends BlazeEntity {
	public EmberWispEntity(EntityType<? extends EmberWispEntity> type, World world) {
		super(type, world);
	}

	public static DefaultAttributeContainer.Builder createEmberWispAttributes() {
		return BlazeEntity.createBlazeAttributes()
				.add(EntityAttributes.MAX_HEALTH, 14.0)
				.add(EntityAttributes.ATTACK_DAMAGE, 5.0)
				.add(EntityAttributes.MOVEMENT_SPEED, 0.32)
				.add(EntityAttributes.FOLLOW_RANGE, 36.0)
				.add(EntityAttributes.SCALE, 0.65);
	}

	@Override
	public void tick() {
		super.tick();

		if (getEntityWorld() instanceof ServerWorld world && age % 3 == 0) {
			world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getBodyY(0.5), getZ(),
					2, 0.15, 0.15, 0.15, 0.01);
		}
	}
}
