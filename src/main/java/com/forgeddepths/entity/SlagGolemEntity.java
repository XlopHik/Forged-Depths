package com.forgeddepths.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class SlagGolemEntity extends IronGolemEntity {
	public SlagGolemEntity(EntityType<? extends SlagGolemEntity> type, World world) {
		super(type, world);
		setPersistent();
	}

	public static DefaultAttributeContainer.Builder createSlagGolemAttributes() {
		return IronGolemEntity.createIronGolemAttributes()
				.add(EntityAttributes.MAX_HEALTH, 90.0)
				.add(EntityAttributes.ATTACK_DAMAGE, 12.0)
				.add(EntityAttributes.MOVEMENT_SPEED, 0.2)
				.add(EntityAttributes.ARMOR, 12.0)
				.add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0)
				.add(EntityAttributes.FOLLOW_RANGE, 32.0);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new MeleeAttackGoal(this, 1.0, true));
		goalSelector.add(4, new WanderAroundFarGoal(this, 0.6));
		goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		goalSelector.add(8, new LookAroundGoal(this));
		targetSelector.add(1, new RevengeGoal(this));
		targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	@Override
	public boolean isFireImmune() {
		return true;
	}

	@Override
	public boolean tryAttack(ServerWorld world, Entity target) {
		boolean hit = super.tryAttack(world, target);

		if (hit && target instanceof LivingEntity) {
			target.setOnFireFor(3.0F);
		}

		return hit;
	}

	@Override
	public void tick() {
		super.tick();

		if (getEntityWorld() instanceof ServerWorld world && age % 10 == 0) {
			world.spawnParticles(ParticleTypes.LAVA, getX(), getBodyY(0.4), getZ(), 1, 0.3, 0.3, 0.3, 0.0);
		}
	}
}
