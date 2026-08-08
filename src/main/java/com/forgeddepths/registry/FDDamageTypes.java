package com.forgeddepths.registry;

import com.forgeddepths.ForgedDepths;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class FDDamageTypes {
	private FDDamageTypes() {
	}

	public static final RegistryKey<DamageType> FORGE_HEAT =
			RegistryKey.of(RegistryKeys.DAMAGE_TYPE, ForgedDepths.id("forge_heat"));
}
