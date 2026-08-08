package com.forgeddepths.block;

import com.forgeddepths.registry.FDEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SoulSpikeBlock extends Block {
	public static final MapCodec<SoulSpikeBlock> CODEC = createCodec(SoulSpikeBlock::new);

	private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 10, 16);
	private static final float DAMAGE = 4.0F;
	private static final int HIT_INTERVAL = 10;

	public SoulSpikeBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity,
			EntityCollisionHandler handler, boolean fromVehicle) {
		if (!(world instanceof ServerWorld serverWorld) || !(entity instanceof LivingEntity living)) {
			return;
		}

		if (entity.age % HIT_INTERVAL != 0 || FDEntities.isForgeGuardian(entity)) {
			return;
		}

		living.damage(serverWorld, serverWorld.getDamageSources().cactus(), DAMAGE);
		living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 1));
		living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));

		serverWorld.playSound(null, pos, SoundEvents.BLOCK_SOUL_SAND_BREAK, SoundCategory.BLOCKS, 0.7F, 0.6F);
		serverWorld.spawnParticles(ParticleTypes.SOUL, entity.getX(), entity.getY() + 0.3, entity.getZ(),
				8, 0.2, 0.2, 0.2, 0.02);
	}
}
