package com.forgeddepths.block;

import com.forgeddepths.registry.FDBlocks;
import com.forgeddepths.registry.FDItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SealedGateBlock extends Block {
	public static final MapCodec<SealedGateBlock> CODEC = createCodec(SealedGateBlock::new);

	private static final int MAX_GATE_BLOCKS = 128;

	public SealedGateBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
			PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!stack.isOf(FDItems.FORGE_SEAL)) {
			return locked(world, pos, player);
		}

		if (!(world instanceof ServerWorld serverWorld)) {
			return ActionResult.SUCCESS;
		}

		if (trialRunningNear(serverWorld, pos)) {
			if (!world.isClient()) {
				player.sendMessage(Text.translatable("message.forged_depths.gate.trial_running")
						.formatted(Formatting.RED), true);
			}

			return ActionResult.SUCCESS;
		}

		List<BlockPos> gate = collectGate(serverWorld, pos);

		for (BlockPos gatePos : gate) {
			serverWorld.setBlockState(gatePos, Blocks.CAVE_AIR.getDefaultState(), Block.NOTIFY_ALL);
			serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
					gatePos.getX() + 0.5, gatePos.getY() + 0.5, gatePos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.02);
		}

		serverWorld.playSound(null, pos, SoundEvents.BLOCK_COPPER_DOOR_OPEN, SoundCategory.BLOCKS, 1.2F, 0.7F);
		serverWorld.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.6F, 1.6F);
		player.sendMessage(Text.translatable("message.forged_depths.gate.opened").formatted(Formatting.GOLD), true);

		return ActionResult.SUCCESS;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
			BlockHitResult hit) {
		return locked(world, pos, player);
	}

	private static ActionResult locked(World world, BlockPos pos, PlayerEntity player) {
		if (!world.isClient()) {
			player.sendMessage(Text.translatable("message.forged_depths.gate.locked").formatted(Formatting.RED), true);
			world.playSound(null, pos, SoundEvents.BLOCK_CHAIN_HIT, SoundCategory.BLOCKS, 0.8F, 0.6F);
		}

		return ActionResult.SUCCESS;
	}

	private static boolean trialRunningNear(ServerWorld world, BlockPos gate) {
		for (BlockPos pos : BlockPos.iterate(gate.add(-9, -3, -9), gate.add(9, 3, 9))) {
			BlockState state = world.getBlockState(pos);

			if (state.isOf(FDBlocks.TRIAL_ALTAR) && state.get(TrialAltarBlock.ACTIVE)) {
				return true;
			}
		}

		return false;
	}

	private static List<BlockPos> collectGate(ServerWorld world, BlockPos start) {
		List<BlockPos> found = new ArrayList<>();
		Set<BlockPos> seen = new HashSet<>();
		Deque<BlockPos> queue = new ArrayDeque<>();

		queue.add(start);
		seen.add(start);

		while (!queue.isEmpty() && found.size() < MAX_GATE_BLOCKS) {
			BlockPos pos = queue.poll();
			found.add(pos);

			for (Direction direction : Direction.values()) {
				BlockPos next = pos.offset(direction);

				if (seen.add(next) && world.getBlockState(next).isOf(FDBlocks.SEALED_GATE)) {
					queue.add(next);
				}
			}
		}

		return found;
	}
}
