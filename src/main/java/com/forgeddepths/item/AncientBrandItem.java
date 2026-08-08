package com.forgeddepths.item;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class AncientBrandItem extends Item {
	public static final int USES = 3;

	public AncientBrandItem(Settings settings) {
		super(settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent,
			Consumer<Text> textConsumer, TooltipType type) {
		textConsumer.accept(Text.translatable("item.forged_depths.ancient_brand.tooltip")
				.formatted(Formatting.LIGHT_PURPLE));
		textConsumer.accept(Text.translatable("item.forged_depths.ancient_brand.tooltip2")
				.formatted(Formatting.GRAY));
		textConsumer.accept(Text.translatable("item.forged_depths.ancient_brand.tooltip3")
				.formatted(Formatting.DARK_GRAY));
	}
}
