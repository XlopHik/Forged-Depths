package com.forgeddepths.heat;

import com.forgeddepths.item.HeatproofCharmItem;
import com.forgeddepths.network.FDNetworking;
import com.forgeddepths.registry.FDDamageTypes;
import com.forgeddepths.registry.FDItems;
import com.forgeddepths.world.DungeonLocator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class HeatManager {
	private HeatManager() {
	}

	public static final int DAMAGE_INTERVAL = 200;
	public static final float BASE_DAMAGE = 4.0F;
	private static final int CHECK_INTERVAL = 10;
	private static final float MAX_REDUCTION = 0.70F;

	private static final float ARMOR_WEIGHT = 0.02F;
	private static final float TOUGHNESS_WEIGHT = 0.012F;
	private static final float FIRE_RESISTANCE_BONUS = 0.25F;

	private static final Map<UUID, Integer> TIMERS = new HashMap<>();
	private static final Map<UUID, Boolean> LAST_INSIDE = new HashMap<>();

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(HeatManager::tick);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			UUID uuid = handler.getPlayer().getUuid();
			TIMERS.remove(uuid);
			LAST_INSIDE.remove(uuid);
		});
	}

	private static void tick(MinecraftServer server) {
		if (server.getTicks() % CHECK_INTERVAL != 0) {
			return;
		}

		for (ServerPlayerEntity player : List.copyOf(server.getPlayerManager().getPlayerList())) {
			updatePlayer(player);
		}
	}

	private static void updatePlayer(ServerPlayerEntity player) {
		UUID uuid = player.getUuid();

		if (!(player.getEntityWorld() instanceof ServerWorld world)) {
			return;
		}

		boolean protectedByMode = player.isCreative() || player.isSpectator();
		boolean inside = !protectedByMode && DungeonLocator.isInsideDungeon(world, player.getBlockPos());
		boolean wasInside = Boolean.TRUE.equals(LAST_INSIDE.put(uuid, inside));

		if (!inside) {
			TIMERS.remove(uuid);

			if (wasInside) {
				FDNetworking.sendHeatStatus(player, false, 0.0F, 0);
				player.sendMessage(Text.translatable("message.forged_depths.heat.left")
						.formatted(Formatting.AQUA), true);
			}

			return;
		}

		if (!wasInside) {
			player.sendMessage(Text.translatable("message.forged_depths.heat.entered")
					.formatted(Formatting.GOLD), false);
			world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_FIRE_AMBIENT,
					SoundCategory.AMBIENT, 0.7F, 0.6F);
		}

		if (player.isTouchingWater()) {
			TIMERS.put(uuid, 0);
			FDNetworking.sendHeatStatus(player, true, 1.0F, DAMAGE_INTERVAL / 20);
			return;
		}

		float reduction = protection(player);
		int timer = TIMERS.getOrDefault(uuid, 0) + CHECK_INTERVAL;

		if (timer >= DAMAGE_INTERVAL) {
			timer = 0;
			burn(player, world, reduction);
		}

		TIMERS.put(uuid, timer);
		FDNetworking.sendHeatStatus(player, true, reduction, (DAMAGE_INTERVAL - timer) / 20);
	}

	private static void burn(ServerPlayerEntity player, ServerWorld world, float reduction) {
		float damage = BASE_DAMAGE * (1.0F - reduction);
		player.damage(world, heatSource(world), damage);

		player.sendMessage(Text.translatable("message.forged_depths.heat.burn",
				String.format("%.1f", damage / 2.0F)).formatted(Formatting.RED), true);

		world.spawnParticles(ParticleTypes.SMOKE, player.getX(), player.getBodyY(0.8), player.getZ(),
				10, 0.3, 0.4, 0.3, 0.01);
		world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_LAVA_EXTINGUISH,
				SoundCategory.PLAYERS, 0.5F, 1.4F);
	}

	private static DamageSource heatSource(ServerWorld world) {
		Optional<RegistryEntry.Reference<DamageType>> entry = world.getRegistryManager()
				.getOrThrow(RegistryKeys.DAMAGE_TYPE)
				.getEntry(FDDamageTypes.FORGE_HEAT.getValue());

		return entry.<DamageSource>map(DamageSource::new).orElseGet(() -> world.getDamageSources().hotFloor());
	}

	public static float protection(ServerPlayerEntity player) {
		float reduction = player.getArmor() * ARMOR_WEIGHT
				+ (float) player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS) * TOUGHNESS_WEIGHT;

		if (hasCharm(player)) {
			reduction += HeatproofCharmItem.PROTECTION;
		}

		if (player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
			reduction += FIRE_RESISTANCE_BONUS;
		}

		return MathHelper.clamp(reduction, 0.0F, MAX_REDUCTION);
	}

	private static boolean hasCharm(ServerPlayerEntity player) {
		PlayerInventory inventory = player.getInventory();

		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = inventory.getStack(slot);

			if (stack.isOf(FDItems.HEATPROOF_CHARM)) {
				return true;
			}
		}

		return false;
	}
}
