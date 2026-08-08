package com.forgeddepths.block;

import com.forgeddepths.registry.FDEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionBehavior;

import java.util.List;

public class RuneTrapBlock extends Block {
	public static final MapCodec<RuneTrapBlock> CODEC = createCodec(RuneTrapBlock::new);

	public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;

	private static final int RECHARGE_TICKS = 300;
	private static final int EFFECT_COUNT = 6;

	private static final net.minecraft.util.shape.VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 1, 16);

	public RuneTrapBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(TRIGGERED, false));
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(TRIGGERED);
	}

	@Override
	protected net.minecraft.util.shape.VoxelShape getOutlineShape(BlockState state, net.minecraft.world.BlockView world,
			BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
		if (!(world instanceof ServerWorld serverWorld) || !(entity instanceof LivingEntity)) {
			return;
		}

		if (state.get(TRIGGERED) || FDEntities.isForgeGuardian(entity)) {
			return;
		}

		serverWorld.setBlockState(pos, state.with(TRIGGERED, true), Block.NOTIFY_ALL);
		serverWorld.scheduleBlockTick(pos, this, RECHARGE_TICKS);
		serverWorld.playSound(null, pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.4F);
		serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.2,
				pos.getZ() + 0.5, 30, 0.5, 0.1, 0.5, 0.05);

		trigger(serverWorld, pos, (LivingEntity) entity, serverWorld.getRandom().nextInt(EFFECT_COUNT));
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		world.setBlockState(pos, state.with(TRIGGERED, false), Block.NOTIFY_ALL);
		world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 0.4F, 0.8F);
	}

	private void trigger(ServerWorld world, BlockPos pos, LivingEntity victim, int effect) {
		switch (effect) {
			case 0 -> summonGuards(world, pos);
			case 1 -> curse(world, victim);
			case 2 -> detonate(world, pos);
			case 3 -> arrowVolley(world, pos, victim);
			case 4 -> ignite(world, pos, victim);
			default -> gasCloud(world, pos);
		}
	}

	private void summonGuards(ServerWorld world, BlockPos pos) {
		for (int i = 0; i < 2; i++) {
			spawnAt(world, FDEntities.EMBER_WISP, pos.up().east(i == 0 ? 1 : -1));
		}

		world.playSound(null, pos, SoundEvents.ENTITY_BLAZE_AMBIENT, SoundCategory.HOSTILE, 1.0F, 0.8F);
	}

	private void curse(ServerWorld world, LivingEntity victim) {
		victim.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0));
		victim.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 240, 0));
		victim.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 400, 1));
		world.playSound(null, victim.getBlockPos(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE,
				0.8F, 1.2F);
	}

	private void detonate(ServerWorld world, BlockPos pos) {
		world.createExplosion(null, null, (ExplosionBehavior) null,
				pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
				2.6F, false, World.ExplosionSourceType.TRIGGER);
	}

	private void arrowVolley(ServerWorld world, BlockPos pos, LivingEntity victim) {
		for (int i = 0; i < 8; i++) {
			double angle = i * Math.PI / 4.0;
			double sx = pos.getX() + 0.5 + Math.cos(angle) * 4.0;
			double sz = pos.getZ() + 0.5 + Math.sin(angle) * 4.0;

			ArrowEntity arrow = new ArrowEntity(world, sx, pos.getY() + 1.2, sz,
					new net.minecraft.item.ItemStack(Items.ARROW), null);
			Vec3d dir = victim.getEntityPos().add(0, 0.8, 0).subtract(arrow.getEntityPos()).normalize();
			arrow.setVelocity(dir.x, dir.y, dir.z, 1.8F, 1.0F);
			arrow.setDamage(3.5);
			world.spawnEntity(arrow);
		}

		world.playSound(null, pos, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.BLOCKS, 1.0F, 0.9F);
	}

	private void ignite(ServerWorld world, BlockPos pos, LivingEntity victim) {
		victim.setOnFireFor(8.0F);
		world.spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				60, 1.2, 0.4, 1.2, 0.08);

		Box area = new Box(pos).expand(2.0, 1.0, 2.0);

		for (Entity entity : world.getOtherEntities(null, area, RuneTrapBlock::isVictim)) {
			entity.setOnFireFor(5.0F);
		}

		world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.2F, 0.7F);
	}

	private void gasCloud(ServerWorld world, BlockPos pos) {
		Box area = new Box(pos).expand(3.0, 2.0, 3.0);
		List<Entity> victims = world.getOtherEntities(null, area, RuneTrapBlock::isVictim);

		for (Entity entity : victims) {
			LivingEntity living = (LivingEntity) entity;
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 200, 1));
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 300, 1));
		}

		world.spawnParticles(ParticleTypes.SNEEZE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
				80, 2.0, 1.0, 2.0, 0.01);
		world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 0.5F);
	}

	private static boolean isVictim(Entity entity) {
		return entity instanceof LivingEntity && !FDEntities.isForgeGuardian(entity);
	}

	private static void spawnAt(ServerWorld world, EntityType<? extends MobEntity> type, BlockPos pos) {
		MobEntity mob = type.create(world, SpawnReason.TRIGGERED);

		if (mob == null) {
			return;
		}

		mob.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.getRandom().nextFloat() * 360F, 0F);
		mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.TRIGGERED, null);
		world.spawnEntity(mob);
	}
}
