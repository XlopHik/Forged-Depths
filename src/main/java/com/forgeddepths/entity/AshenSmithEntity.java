package com.forgeddepths.entity;

import com.forgeddepths.registry.FDItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

public class AshenSmithEntity extends ZombieEntity {
	public AshenSmithEntity(EntityType<? extends AshenSmithEntity> type, World world) {
		super(type, world);
		setPersistent();
	}

	public static DefaultAttributeContainer.Builder createAshenSmithAttributes() {
		return ZombieEntity.createZombieAttributes()
				.add(EntityAttributes.MAX_HEALTH, 60.0)
				.add(EntityAttributes.ATTACK_DAMAGE, 9.0)
				.add(EntityAttributes.MOVEMENT_SPEED, 0.26)
				.add(EntityAttributes.ARMOR, 10.0)
				.add(EntityAttributes.ARMOR_TOUGHNESS, 4.0)
				.add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.7)
				.add(EntityAttributes.FOLLOW_RANGE, 42.0);
	}

	@Override
	protected void initCustomGoals() {
		goalSelector.add(3, new ZombieAttackGoal(this, 1.0, false));
		goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
		targetSelector.add(1, new RevengeGoal(this));
		targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	@Override
	protected boolean burnsInDaylight() {
		return false;
	}

	@Override
	protected boolean canConvertInWater() {
		return false;
	}

	@Override
	public boolean isFireImmune() {
		return true;
	}

	@Override
	protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
		equipStack(EquipmentSlot.MAINHAND, new ItemStack(FDItems.ASHEN_HAMMER));
		equipStack(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
		equipStack(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));

		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR || slot == EquipmentSlot.MAINHAND) {
				setEquipmentDropChance(slot, 0.0F);
			}
		}
	}

	@Override
	public boolean tryAttack(ServerWorld world, Entity target) {
		boolean hit = super.tryAttack(world, target);

		if (hit && target instanceof LivingEntity) {
			target.setOnFireFor(5.0F);
			world.spawnParticles(ParticleTypes.LAVA, target.getX(), target.getBodyY(0.6), target.getZ(),
					6, 0.2, 0.2, 0.2, 0.01);
		}

		return hit;
	}

	@Override
	public void tick() {
		super.tick();

		if (getEntityWorld() instanceof ServerWorld world && age % 6 == 0) {
			world.spawnParticles(ParticleTypes.SMOKE, getX(), getBodyY(0.9), getZ(), 1, 0.2, 0.2, 0.2, 0.0);
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_ZOMBIE_VILLAGER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(net.minecraft.entity.damage.DamageSource source) {
		return SoundEvents.BLOCK_ANVIL_LAND;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.BLOCK_ANVIL_DESTROY;
	}
}
