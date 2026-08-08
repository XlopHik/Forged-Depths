package com.forgeddepths.item;

import com.forgeddepths.registry.FDEntities;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;

import java.util.function.Consumer;

public class AshenHammerItem extends Item {
	private static final float SHOCKWAVE_RADIUS = 2.5F;
	private static final float SHOCKWAVE_DAMAGE = 4.0F;
	private static final int IGNITE_SECONDS = 5;

	public AshenHammerItem(Settings settings) {
		super(settings);
	}

	@Override
	public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.postHit(stack, target, attacker);
		target.setOnFireFor(IGNITE_SECONDS);

		if (!(attacker.getEntityWorld() instanceof ServerWorld world)) {
			return;
		}

		boolean wieldedByGuardian = FDEntities.isForgeGuardian(attacker);
		Box area = target.getBoundingBox().expand(SHOCKWAVE_RADIUS);

		for (Entity nearby : world.getOtherEntities(attacker, area,
				e -> e instanceof LivingEntity && e != target
						&& !(wieldedByGuardian && FDEntities.isForgeGuardian(e)))) {
			((LivingEntity) nearby).damage(world, attacker.getDamageSources().mobAttack(attacker), SHOCKWAVE_DAMAGE);
			nearby.setOnFireFor(IGNITE_SECONDS / 2);
		}

		world.spawnParticles(ParticleTypes.LAVA, target.getX(), target.getBodyY(0.5), target.getZ(),
				12, 0.4, 0.4, 0.4, 0.02);
		world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.6F, 1.4F);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent,
			Consumer<Text> textConsumer, TooltipType type) {
		textConsumer.accept(Text.translatable("item.forged_depths.ashen_hammer.tooltip").formatted(Formatting.GOLD));
		textConsumer.accept(Text.translatable("item.forged_depths.ashen_hammer.tooltip2").formatted(Formatting.GRAY));
	}
}
