package com.forgeddepths.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;

public class MagmaVentBlock extends Block {
	public static final MapCodec<MagmaVentBlock> CODEC = createCodec(MagmaVentBlock::new);

	public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

	private static final int JET_HEIGHT = 3;
	private static final float JET_DAMAGE = 3.0F;
	private static final int ACTIVE_TICKS = 40;
	private static final int MIN_IDLE_TICKS = 60;
	private static final int MAX_EXTRA_IDLE_TICKS = 80;

	public MagmaVentBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(ACTIVE, false));
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!world.isClient() && !world.getBlockTickScheduler().isQueued(pos, this)) {
			world.scheduleBlockTick(pos, this, MIN_IDLE_TICKS + world.getRandom().nextInt(MAX_EXTRA_IDLE_TICKS));
		}
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		boolean wasActive = state.get(ACTIVE);
		boolean nowActive = !wasActive;

		world.setBlockState(pos, state.with(ACTIVE, nowActive), Block.NOTIFY_ALL);

		if (nowActive) {
			erupt(world, pos);
			world.scheduleBlockTick(pos, this, ACTIVE_TICKS);
		} else {
			world.scheduleBlockTick(pos, this, MIN_IDLE_TICKS + random.nextInt(MAX_EXTRA_IDLE_TICKS));
		}
	}

	private void erupt(ServerWorld world, BlockPos pos) {
		world.playSound(null, pos, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 1.2F, 0.6F);
		world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.8F, 0.7F);

		for (int dy = 1; dy <= JET_HEIGHT; dy++) {
			world.spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + dy, pos.getZ() + 0.5,
					10, 0.18, 0.3, 0.18, 0.06);
			world.spawnParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + dy, pos.getZ() + 0.5,
					3, 0.2, 0.2, 0.2, 0.01);
		}

		Box jet = new Box(pos.getX(), pos.getY() + 1.0, pos.getZ(),
				pos.getX() + 1.0, pos.getY() + 1.0 + JET_HEIGHT, pos.getZ() + 1.0);

		List<Entity> caught = world.getOtherEntities(null, jet, e -> e instanceof LivingEntity);

		for (Entity entity : caught) {
			entity.setOnFireFor(6.0F);
			((LivingEntity) entity).damage(world, world.getDamageSources().inFire(), JET_DAMAGE);
		}
	}
}
