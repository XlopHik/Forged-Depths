package com.forgeddepths.item;

import com.forgeddepths.world.DungeonLocator;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Consumer;

public class ForgeCompassItem extends Item {
	private static final int COOLDOWN_TICKS = 120;
	private static final int SEARCH_RADIUS_CHUNKS = 80;

	public ForgeCompassItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		if (!(world instanceof ServerWorld serverWorld)) {
			return ActionResult.SUCCESS;
		}

		user.getItemCooldownManager().set(stack, COOLDOWN_TICKS);
		BlockPos origin = user.getBlockPos();
		Optional<BlockPos> found = DungeonLocator.findNearest(serverWorld, origin, SEARCH_RADIUS_CHUNKS);

		if (found.isEmpty()) {
			user.sendMessage(Text.translatable("message.forged_depths.compass.nothing").formatted(Formatting.GRAY),
					true);
			world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS,
					0.6F, 1.6F);
			return ActionResult.SUCCESS;
		}

		BlockPos target = found.get();
		int distance = MathHelper.floor(Math.sqrt(origin.getSquaredDistance(target.getX(), origin.getY(), target.getZ())));

		user.sendMessage(Text.translatable("message.forged_depths.compass.found",
				target.getX(), target.getZ(), distance, cardinal(origin, target)).formatted(Formatting.GOLD), true);
		world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.5F, 1.8F);

		return ActionResult.SUCCESS;
	}

	private static String cardinal(BlockPos from, BlockPos to) {
		int dx = to.getX() - from.getX();
		int dz = to.getZ() - from.getZ();

		if (Math.abs(dx) > Math.abs(dz) * 2) {
			return dx > 0 ? "E" : "W";
		}

		if (Math.abs(dz) > Math.abs(dx) * 2) {
			return dz > 0 ? "S" : "N";
		}

		return (dz > 0 ? "S" : "N") + (dx > 0 ? "E" : "W");
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent,
			Consumer<Text> textConsumer, TooltipType type) {
		textConsumer.accept(Text.translatable("item.forged_depths.forge_compass.tooltip").formatted(Formatting.GRAY));
	}
}
