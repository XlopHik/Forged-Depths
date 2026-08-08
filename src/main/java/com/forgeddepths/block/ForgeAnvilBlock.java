package com.forgeddepths.block;

import com.forgeddepths.registry.FDItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Optional;

public class ForgeAnvilBlock extends HorizontalFacingBlock {
	public static final MapCodec<ForgeAnvilBlock> CODEC = createCodec(ForgeAnvilBlock::new);

	public static final IntProperty CHARGES = IntProperty.of("charges", 0, 5);
	public static final int MAX_CHARGES = 5;

	public static final float BREAK_CHANCE = 0.15F;

	private static final VoxelShape SHAPE_Z = VoxelShapes.union(
			Block.createCuboidShape(2, 0, 2, 14, 4, 14),
			Block.createCuboidShape(4, 4, 5, 12, 10, 11),
			Block.createCuboidShape(0, 10, 2, 16, 16, 14));

	private static final VoxelShape SHAPE_X = VoxelShapes.union(
			Block.createCuboidShape(2, 0, 2, 14, 4, 14),
			Block.createCuboidShape(5, 4, 4, 11, 10, 12),
			Block.createCuboidShape(2, 10, 0, 14, 16, 16));

	public ForgeAnvilBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState()
				.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
				.with(CHARGES, MAX_CHARGES));
	}

	@Override
	protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(Properties.HORIZONTAL_FACING, CHARGES);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().rotateYClockwise());
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos,
			net.minecraft.block.ShapeContext context) {
		Direction facing = state.get(Properties.HORIZONTAL_FACING);
		return facing.getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
			BlockHitResult hit) {
		if (!world.isClient()) {
			int charges = state.get(CHARGES);
			player.sendMessage(charges > 0
					? Text.translatable("message.forged_depths.anvil.charges", charges).formatted(Formatting.GOLD)
					: Text.translatable("message.forged_depths.anvil.spent").formatted(Formatting.DARK_GRAY), true);
		}

		return ActionResult.SUCCESS;
	}

	@Override
	protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
			PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (stack.isEmpty()) {
			return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
		}

		int charges = state.get(CHARGES);

		if (charges <= 0) {
			if (!world.isClient()) {
				player.sendMessage(Text.translatable("message.forged_depths.anvil.spent")
						.formatted(Formatting.DARK_GRAY), true);
				world.playSound(null, pos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.4F, 0.6F);
			}

			return ActionResult.SUCCESS;
		}

		if (stack.isOf(FDItems.ANCIENT_BRAND)) {
			return enchantWithBrand(stack, state, world, pos, player);
		}

		if (!stack.isDamageable()) {
			if (!world.isClient()) {
				player.sendMessage(Text.translatable("message.forged_depths.anvil.not_repairable")
						.formatted(Formatting.RED), true);
			}

			return ActionResult.SUCCESS;
		}

		if (!stack.isDamaged()) {
			if (!world.isClient()) {
				player.sendMessage(Text.translatable("message.forged_depths.anvil.undamaged")
						.formatted(Formatting.GRAY), true);
			}

			return ActionResult.SUCCESS;
		}

		if (!(world instanceof ServerWorld serverWorld)) {
			return ActionResult.SUCCESS;
		}

		boolean destroyed = serverWorld.getRandom().nextFloat() < BREAK_CHANCE;

		if (destroyed) {
			Text name = stack.getName();
			stack.decrement(1);
			player.sendMessage(Text.translatable("message.forged_depths.anvil.destroyed", name)
					.formatted(Formatting.DARK_RED), false);
			serverWorld.playSound(null, pos, SoundEvents.ENTITY_ITEM_BREAK.value(), SoundCategory.BLOCKS, 1.0F, 0.7F);
			serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
					18, 0.3, 0.2, 0.3, 0.02);
		} else {
			stack.setDamage(0);
			player.sendMessage(Text.translatable("message.forged_depths.anvil.repaired", stack.getName())
					.formatted(Formatting.GREEN), true);
			serverWorld.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.9F, 1.0F);
			serverWorld.spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
					14, 0.3, 0.15, 0.3, 0.03);
		}

		wearDown(serverWorld, state, pos, player, charges);
		return ActionResult.SUCCESS;
	}

	private ActionResult enchantWithBrand(ItemStack brand, BlockState state, World world, BlockPos pos,
			PlayerEntity player) {
		ItemStack target = player.getOffHandStack();

		if (target.isEmpty() || target.get(DataComponentTypes.ENCHANTABLE) == null) {
			if (!world.isClient()) {
				player.sendMessage(Text.translatable("message.forged_depths.anvil.brand_needs_target")
						.formatted(Formatting.RED), true);
			}

			return ActionResult.SUCCESS;
		}

		if (!(world instanceof ServerWorld serverWorld)) {
			return ActionResult.SUCCESS;
		}

		Random random = serverWorld.getRandom();

		if (random.nextFloat() < BREAK_CHANCE) {
			Text name = target.getName();
			target.decrement(1);
			player.sendMessage(Text.translatable("message.forged_depths.anvil.destroyed", name)
					.formatted(Formatting.DARK_RED), false);
			serverWorld.playSound(null, pos, SoundEvents.ENTITY_ITEM_BREAK.value(), SoundCategory.BLOCKS, 1.0F, 0.7F);
		} else {
			ItemStack enchanted = EnchantmentHelper.enchant(random, target, rollPower(random),
					serverWorld.getRegistryManager(), Optional.empty());

			player.setStackInHand(Hand.OFF_HAND, enchanted);

			player.sendMessage(Text.translatable("message.forged_depths.anvil.enchanted", enchanted.getName())
					.formatted(Formatting.LIGHT_PURPLE), false);
			serverWorld.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 0.8F);
			serverWorld.spawnParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.4, pos.getZ() + 0.5,
					40, 0.5, 0.4, 0.5, 0.4);
		}

		brand.damage(1, player, EquipmentSlot.MAINHAND);
		wearDown(serverWorld, state, pos, player, state.get(CHARGES));

		return ActionResult.SUCCESS;
	}

	private static int rollPower(Random random) {
		int power = 4 + random.nextInt(9);

		if (random.nextFloat() < 0.30F) {
			power += random.nextInt(9);
		}

		if (random.nextFloat() < 0.08F) {
			power += random.nextInt(12);
		}

		return MathHelper.clamp(power, 1, 30);
	}

	private void wearDown(ServerWorld world, BlockState state, BlockPos pos, PlayerEntity player, int charges) {
		int left = charges - 1;
		world.setBlockState(pos, state.with(CHARGES, left), Block.NOTIFY_ALL);
		world.playSound(null, pos, SoundEvents.BLOCK_DEEPSLATE_BRICKS_BREAK, SoundCategory.BLOCKS, 0.5F, 0.8F);
		world.spawnParticles(ParticleTypes.ASH, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
				20, 0.4, 0.3, 0.4, 0.01);

		player.sendMessage(left > 0
				? Text.translatable("message.forged_depths.anvil.charges", left).formatted(Formatting.YELLOW)
				: Text.translatable("message.forged_depths.anvil.crumbled").formatted(Formatting.DARK_RED), false);
	}
}
