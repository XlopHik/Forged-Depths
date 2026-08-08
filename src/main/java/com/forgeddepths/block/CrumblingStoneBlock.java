package com.forgeddepths.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class CrumblingStoneBlock extends Block {
	public static final MapCodec<CrumblingStoneBlock> CODEC = createCodec(CrumblingStoneBlock::new);

	public static final IntProperty STAGE = IntProperty.of("stage", 0, 2);
	private static final int TICK_DELAY = 8;

	public CrumblingStoneBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(STAGE, 0));
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(STAGE);
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
		if (world.isClient() || !(entity instanceof LivingEntity)) {
			return;
		}

		if (!world.getBlockTickScheduler().isQueued(pos, this)) {
			world.scheduleBlockTick(pos, this, TICK_DELAY);
			world.playSound(null, pos, SoundEvents.BLOCK_DEEPSLATE_TILES_HIT, SoundCategory.BLOCKS, 0.9F, 0.5F);
		}
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		int stage = state.get(STAGE);

		world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
				pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 12, 0.35, 0.05, 0.35, 0.0);

		if (stage < 2) {
			world.setBlockState(pos, state.with(STAGE, stage + 1), Block.NOTIFY_ALL);
			world.playSound(null, pos, SoundEvents.BLOCK_DEEPSLATE_TILES_HIT, SoundCategory.BLOCKS, 1.0F,
					0.5F + stage * 0.2F);
			world.scheduleBlockTick(pos, this, TICK_DELAY);
			return;
		}

		world.playSound(null, pos, SoundEvents.BLOCK_DEEPSLATE_TILES_BREAK, SoundCategory.BLOCKS, 1.0F, 0.7F);
		world.breakBlock(pos, false);
	}
}
