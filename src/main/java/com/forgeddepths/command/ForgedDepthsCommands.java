package com.forgeddepths.command;

import com.forgeddepths.heat.HeatManager;
import com.forgeddepths.registry.FDBlocks;
import com.forgeddepths.world.DungeonLocator;
import com.forgeddepths.world.ForgedDepthsGenerator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.block.BlockState;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;
import java.util.Set;

public final class ForgedDepthsCommands {
	private ForgedDepthsCommands() {
	}

	private static final int SCAN_TOP = -40;
	private static final int SCAN_BOTTOM = -63;

	private static final Permission GAMEMASTER = new Permission.Level(PermissionLevel.GAMEMASTERS);

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("forgeddepths")
				.then(CommandManager.literal("locate").executes(ForgedDepthsCommands::locate))
				.then(CommandManager.literal("status").executes(ForgedDepthsCommands::status))
				.then(CommandManager.literal("info").executes(ForgedDepthsCommands::info))
				.then(CommandManager.literal("tp")
						.requires(source -> source.getPermissions().hasPermission(GAMEMASTER))
						.then(CommandManager.argument("x", IntegerArgumentType.integer())
								.then(CommandManager.argument("z", IntegerArgumentType.integer())
										.executes(ForgedDepthsCommands::teleport))))
				.executes(ForgedDepthsCommands::info);

		LiteralCommandNode<ServerCommandSource> node = dispatcher.register(root);
		dispatcher.register(CommandManager.literal("fd").redirect(node).executes(ForgedDepthsCommands::info));
	}

	private static int locate(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		ServerWorld world = source.getWorld();
		BlockPos origin = BlockPos.ofFloored(source.getPosition());

		source.sendFeedback(() -> Text.translatable("command.forged_depths.locate.searching")
				.formatted(Formatting.DARK_GRAY), false);

		Optional<BlockPos> found = DungeonLocator.findNearest(world, origin, DungeonLocator.DEFAULT_SEARCH_RADIUS);

		if (found.isEmpty()) {
			source.sendError(Text.translatable("command.forged_depths.locate.not_found"));
			return 0;
		}

		BlockPos target = found.get();
		int x = target.getX();
		int z = target.getZ();
		int distance = MathHelper.floor(Math.sqrt(
				Math.pow(x - origin.getX(), 2) + Math.pow(z - origin.getZ(), 2)));

		MutableText coords = Text.literal("[" + x + ", ~, " + z + "]")
				.styled(style -> style
						.withColor(Formatting.GOLD)
						.withUnderline(true)
						.withClickEvent(new ClickEvent.RunCommand("/forgeddepths tp " + x + " " + z))
						.withHoverEvent(new HoverEvent.ShowText(
								Text.translatable("command.forged_depths.locate.hover"))));

		source.sendFeedback(() -> Text.translatable("command.forged_depths.locate.found", coords, distance)
				.formatted(Formatting.YELLOW), false);

		return 1;
	}

	private static int teleport(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player;

		try {
			player = source.getPlayerOrThrow();
		} catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
			source.sendError(Text.translatable("command.forged_depths.tp.player_only"));
			return 0;
		}

		int x = IntegerArgumentType.getInteger(context, "x") + ForgedDepthsGenerator.ROOM_SIZE / 2;
		int z = IntegerArgumentType.getInteger(context, "z") + ForgedDepthsGenerator.ROOM_SIZE / 2;
		ServerWorld world = source.getWorld();

		source.sendFeedback(() -> Text.translatable("command.forged_depths.tp.loading")
				.formatted(Formatting.DARK_GRAY), false);

		world.getChunk(x >> 4, z >> 4);

		int y = findFloor(world, x, z);

		if (y == Integer.MIN_VALUE) {
			source.sendError(Text.translatable("command.forged_depths.tp.no_room"));
			return 0;
		}

		player.teleport(world, x + 0.5, y, z + 0.5, Set.of(), player.getYaw(), player.getPitch(), true);
		source.sendFeedback(() -> Text.translatable("command.forged_depths.tp.done", x, y, z)
				.formatted(Formatting.GREEN), false);

		return 1;
	}

	private static int findFloor(ServerWorld world, int x, int z) {
		BlockPos.Mutable pos = new BlockPos.Mutable();

		for (int y = SCAN_TOP; y >= SCAN_BOTTOM; y--) {
			pos.set(x, y, z);
			BlockState ground = world.getBlockState(pos);

			if (ground.isAir() || !isForgeFloor(ground)) {
				continue;
			}

			if (world.getBlockState(pos.set(x, y + 1, z)).isAir()
					&& world.getBlockState(pos.set(x, y + 2, z)).isAir()) {
				return y + 1;
			}
		}

		return Integer.MIN_VALUE;
	}

	private static boolean isForgeFloor(BlockState state) {
		return FDBlocks.isForgeMasonry(state.getBlock()) || state.isOf(FDBlocks.SLAG_BLOCK);
	}

	private static int status(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player;

		try {
			player = source.getPlayerOrThrow();
		} catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
			source.sendError(Text.translatable("command.forged_depths.tp.player_only"));
			return 0;
		}

		boolean inside = player.getEntityWorld() instanceof ServerWorld world
				&& DungeonLocator.isInsideDungeon(world, player.getBlockPos());
		int percent = Math.round(HeatManager.protection(player) * 100.0F);
		float damage = HeatManager.BASE_DAMAGE * (1.0F - HeatManager.protection(player)) / 2.0F;

		source.sendFeedback(() -> Text.translatable("command.forged_depths.status.header")
				.formatted(Formatting.GOLD), false);
		source.sendFeedback(() -> Text.translatable("command.forged_depths.status.inside",
				Text.translatable(inside ? "command.forged_depths.yes" : "command.forged_depths.no"))
				.formatted(inside ? Formatting.RED : Formatting.GREEN), false);
		source.sendFeedback(() -> Text.translatable("command.forged_depths.status.protection", percent)
				.formatted(Formatting.AQUA), false);
		source.sendFeedback(() -> Text.translatable("command.forged_depths.status.damage",
				String.format("%.2f", damage)).formatted(Formatting.YELLOW), false);

		return 1;
	}

	private static int info(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();

		source.sendFeedback(() -> Text.translatable("command.forged_depths.info.title")
				.formatted(Formatting.GOLD, Formatting.BOLD), false);

		for (int i = 1; i <= 5; i++) {
			int line = i;
			source.sendFeedback(() -> Text.translatable("command.forged_depths.info.line" + line)
					.formatted(Formatting.GRAY), false);
		}

		return 1;
	}
}
