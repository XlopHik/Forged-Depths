package com.forgeddepths.block;

import com.forgeddepths.registry.FDBlocks;
import com.forgeddepths.registry.FDEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class TrialAltarBlock extends Block {
	public static final MapCodec<TrialAltarBlock> CODEC = createCodec(TrialAltarBlock::new);

	public static final IntProperty WAVE = IntProperty.of("wave", 0, 3);
	public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

	public static final int TOTAL_WAVES = 3;

	private static final int CHECK_TICKS = 40;
	private static final int ROOM_RADIUS = 7;
	private static final int DOORWAY_HEIGHT = 4;

	private static final double ABANDON_DISTANCE = 14.0;

	private static final int REWARD_DX = -5;
	private static final int REWARD_DZ = -5;

	public TrialAltarBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(WAVE, 0).with(ACTIVE, false));
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WAVE, ACTIVE);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
			BlockHitResult hit) {
		if (state.get(ACTIVE)) {
			if (!world.isClient()) {
				player.sendMessage(Text.translatable("message.forged_depths.trial.running",
						state.get(WAVE), TOTAL_WAVES).formatted(Formatting.GOLD), true);
			}

			return ActionResult.SUCCESS;
		}

		if (state.get(WAVE) >= TOTAL_WAVES) {
			if (!world.isClient()) {
				player.sendMessage(Text.translatable("message.forged_depths.trial.done")
						.formatted(Formatting.DARK_GRAY), true);
			}

			return ActionResult.SUCCESS;
		}

		if (!(world instanceof ServerWorld serverWorld)) {
			return ActionResult.SUCCESS;
		}

		closeGates(serverWorld, pos);
		spawnWave(serverWorld, pos, 1);
		serverWorld.setBlockState(pos, state.with(ACTIVE, true).with(WAVE, 1), Block.NOTIFY_ALL);
		serverWorld.scheduleBlockTick(pos, this, CHECK_TICKS);

		serverWorld.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 0.5F, 1.6F);
		player.sendMessage(Text.translatable("message.forged_depths.trial.started", TOTAL_WAVES)
				.formatted(Formatting.RED), false);

		return ActionResult.SUCCESS;
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (!state.get(ACTIVE)) {
			return;
		}

		if (world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				ABANDON_DISTANCE, false) == null) {
			openGates(world, pos);
			world.setBlockState(pos, state.with(ACTIVE, false).with(WAVE, 0), Block.NOTIFY_ALL);
			return;
		}

		if (guardiansAlive(world, pos) > 0) {
			world.scheduleBlockTick(pos, this, CHECK_TICKS);
			return;
		}

		int wave = state.get(WAVE);

		if (wave >= TOTAL_WAVES) {
			finish(world, pos, state);
			return;
		}

		spawnWave(world, pos, wave + 1);
		world.setBlockState(pos, state.with(WAVE, wave + 1), Block.NOTIFY_ALL);
		world.scheduleBlockTick(pos, this, CHECK_TICKS);
		world.playSound(null, pos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 0.35F, 1.8F);
	}

	private void finish(ServerWorld world, BlockPos pos, BlockState state) {
		openGates(world, pos);
		world.setBlockState(pos, state.with(ACTIVE, false).with(WAVE, TOTAL_WAVES), Block.NOTIFY_ALL);

		for (int dy = 0; dy <= 1; dy++) {
			clearGate(world, pos.add(REWARD_DX + 1, dy, REWARD_DZ));
			clearGate(world, pos.add(REWARD_DX, dy, REWARD_DZ + 1));
		}

		world.playSound(null, pos, SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1.0F, 1.2F);
		world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
				60, 0.6, 0.6, 0.6, 0.3);

		PlayerEntity player = world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				ABANDON_DISTANCE, false);

		if (player != null) {
			player.sendMessage(Text.translatable("message.forged_depths.trial.cleared")
					.formatted(Formatting.GREEN), false);
		}
	}

	private int guardiansAlive(ServerWorld world, BlockPos pos) {
		Box arena = new Box(pos).expand(ROOM_RADIUS + 1, 6.0, ROOM_RADIUS + 1);
		return world.getEntitiesByClass(MobEntity.class, arena,
				mob -> mob.isAlive() && FDEntities.isForgeGuardian(mob)).size();
	}

	private void spawnWave(ServerWorld world, BlockPos pos, int wave) {
		int[][] spots = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}, {0, -5}, {0, 5}};

		switch (wave) {
			case 1 -> {
				summon(world, FDEntities.EMBER_WISP, pos, spots[0], 2);
				summon(world, FDEntities.EMBER_WISP, pos, spots[1], 2);
				summon(world, FDEntities.EMBER_WISP, pos, spots[2], 2);
			}
			case 2 -> {
				summon(world, FDEntities.ASHEN_SMITH, pos, spots[0], 0);
				summon(world, FDEntities.ASHEN_SMITH, pos, spots[3], 0);
				summon(world, FDEntities.EMBER_WISP, pos, spots[4], 2);
				summon(world, FDEntities.EMBER_WISP, pos, spots[5], 2);
			}
			default -> {
				summon(world, FDEntities.ASHEN_SMITH, pos, spots[1], 0);
				summon(world, FDEntities.ASHEN_SMITH, pos, spots[2], 0);
				summon(world, FDEntities.ASHEN_SMITH, pos, spots[4], 0);
				summon(world, FDEntities.EMBER_WISP, pos, spots[0], 3);
				summon(world, FDEntities.EMBER_WISP, pos, spots[3], 3);
			}
		}
	}

	private void summon(ServerWorld world, EntityType<? extends MobEntity> type, BlockPos altar, int[] offset,
			int height) {
		BlockPos spawn = altar.add(offset[0], height, offset[1]);
		MobEntity mob = type.spawn(world, spawn, SpawnReason.TRIGGERED);

		if (mob != null) {
			mob.setPersistent();
			world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, spawn.getX() + 0.5, spawn.getY() + 0.5,
					spawn.getZ() + 0.5, 20, 0.3, 0.5, 0.3, 0.05);
		}
	}

	private void closeGates(ServerWorld world, BlockPos altar) {
		forEachDoorway(altar, pos -> {
			if (world.getBlockState(pos).isAir()) {
				world.setBlockState(pos, FDBlocks.SEALED_GATE.getDefaultState(), Block.NOTIFY_ALL);
			}
		});

		world.playSound(null, altar, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.4F, 0.5F);
	}

	private void openGates(ServerWorld world, BlockPos altar) {
		forEachDoorway(altar, pos -> clearGate(world, pos));
		world.playSound(null, altar, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.4F, 0.7F);
	}

	private static void clearGate(ServerWorld world, BlockPos pos) {
		if (world.getBlockState(pos).isOf(FDBlocks.SEALED_GATE)) {
			world.setBlockState(pos, Blocks.CAVE_AIR.getDefaultState(), Block.NOTIFY_ALL);
		}
	}

	private static void forEachDoorway(BlockPos altar, java.util.function.Consumer<BlockPos> action) {
		for (int side = 0; side < 4; side++) {
			Direction facing = Direction.fromHorizontalQuarterTurns(side);
			Direction lateral = facing.rotateYClockwise();

			for (int offset = -1; offset <= 1; offset++) {
				for (int dy = 0; dy < DOORWAY_HEIGHT; dy++) {
					action.accept(altar.offset(facing, ROOM_RADIUS).offset(lateral, offset).up(dy));
				}
			}
		}
	}
}
