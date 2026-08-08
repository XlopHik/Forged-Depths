package com.forgeddepths.item;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class ForgeSealItem extends Item {
	public ForgeSealItem(Settings settings) {
		super(settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent,
			Consumer<Text> textConsumer, TooltipType type) {
		textConsumer.accept(Text.translatable("item.forged_depths.forge_seal.tooltip").formatted(Formatting.GOLD));
		textConsumer.accept(Text.translatable("item.forged_depths.forge_seal.tooltip2").formatted(Formatting.GRAY));
	}
}
