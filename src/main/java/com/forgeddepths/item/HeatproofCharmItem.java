package com.forgeddepths.item;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class HeatproofCharmItem extends Item {
	public static final float PROTECTION = 0.35F;

	public HeatproofCharmItem(Settings settings) {
		super(settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent,
			Consumer<Text> textConsumer, TooltipType type) {
		textConsumer.accept(Text.translatable("item.forged_depths.heatproof_charm.tooltip")
				.formatted(Formatting.GOLD));
		textConsumer.accept(Text.translatable("item.forged_depths.heatproof_charm.tooltip2")
				.formatted(Formatting.DARK_GRAY));
	}
}
