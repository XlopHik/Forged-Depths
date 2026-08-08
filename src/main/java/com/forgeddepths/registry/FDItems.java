package com.forgeddepths.registry;

import com.forgeddepths.ForgedDepths;
import com.forgeddepths.item.AncientBrandItem;
import com.forgeddepths.item.AshenHammerItem;
import com.forgeddepths.item.ForgeSealItem;
import com.forgeddepths.item.ForgeCompassItem;
import com.forgeddepths.item.HeatproofCharmItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class FDItems {
	private FDItems() {
	}

	public static final List<Item> ALL = new ArrayList<>();

	public static final Item EMBERFORGED_INGOT = register("emberforged_ingot", Item::new,
			new Item.Settings().fireproof());

	public static final Item CINDER_HEART = register("cinder_heart", Item::new,
			new Item.Settings().rarity(Rarity.RARE).fireproof());

	public static final Item HEATPROOF_CHARM = register("heatproof_charm", HeatproofCharmItem::new,
			new Item.Settings().maxCount(1).rarity(Rarity.RARE).fireproof());

	public static final Item FORGE_COMPASS = register("forge_compass", ForgeCompassItem::new,
			new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON));

	public static final Item ANCIENT_BRAND = register("ancient_brand", AncientBrandItem::new,
			new Item.Settings()
					.maxCount(1)
					.maxDamage(AncientBrandItem.USES)
					.rarity(Rarity.EPIC)
					.fireproof());

	public static final Item FORGE_SEAL = register("forge_seal", ForgeSealItem::new,
			new Item.Settings().maxCount(1).rarity(Rarity.RARE).fireproof());

	public static final Item ASHEN_HAMMER = register("ashen_hammer", AshenHammerItem::new,
			new Item.Settings()
					.sword(ToolMaterial.NETHERITE, 6.0F, -3.1F)
					.rarity(Rarity.EPIC)
					.fireproof());

	private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, ForgedDepths.id(name));
		Item item = factory.apply(settings.registryKey(key));
		Registry.register(Registries.ITEM, key, item);
		ALL.add(item);
		return item;
	}

	public static void register() {
	}
}
