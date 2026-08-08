package com.forgeddepths.registry;

import com.forgeddepths.ForgedDepths;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class FDLootTables {
	private FDLootTables() {
	}

	public static final RegistryKey<LootTable> ENTRANCE_STASH = of("chests/entrance_stash");
	public static final RegistryKey<LootTable> FORGE_HALL = of("chests/forge_hall");
	public static final RegistryKey<LootTable> TREASURY = of("chests/treasury");
	public static final RegistryKey<LootTable> BARRACKS = of("chests/barracks");
	public static final RegistryKey<LootTable> CRUCIBLE = of("chests/crucible");
	public static final RegistryKey<LootTable> ARCHIVE = of("chests/archive");
	public static final RegistryKey<LootTable> SMELTERY = of("chests/smeltery");
	public static final RegistryKey<LootTable> RUBBLE = of("chests/rubble");
	public static final RegistryKey<LootTable> TRIAL = of("chests/trial");
	public static final RegistryKey<LootTable> FLOODED = of("chests/flooded");

	private static RegistryKey<LootTable> of(String path) {
		return RegistryKey.of(RegistryKeys.LOOT_TABLE, ForgedDepths.id(path));
	}
}
