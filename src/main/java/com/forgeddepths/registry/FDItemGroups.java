package com.forgeddepths.registry;

import com.forgeddepths.ForgedDepths;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public final class FDItemGroups {
	private FDItemGroups() {
	}

	public static final RegistryKey<ItemGroup> MAIN =
			RegistryKey.of(RegistryKeys.ITEM_GROUP, ForgedDepths.id("main"));

	public static void register() {
		Registry.register(Registries.ITEM_GROUP, MAIN, FabricItemGroup.builder()
				.icon(() -> new ItemStack(FDBlocks.FORGE_ANVIL))
				.displayName(Text.translatable("itemGroup.forged_depths.main"))
				.entries((context, entries) -> {
					FDItems.ALL.forEach(entries::add);
					FDBlocks.ALL.forEach(entries::add);
				})
				.build());
	}
}
