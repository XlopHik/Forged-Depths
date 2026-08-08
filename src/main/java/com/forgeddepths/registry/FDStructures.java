package com.forgeddepths.registry;

import com.forgeddepths.ForgedDepths;
import com.forgeddepths.world.ForgedDepthsPiece;
import com.forgeddepths.world.ForgedDepthsStructure;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public final class FDStructures {
	private FDStructures() {
	}

	public static final RegistryKey<Structure> FORGED_DEPTHS =
			RegistryKey.of(RegistryKeys.STRUCTURE, ForgedDepths.id("forged_depths"));

	public static StructureType<ForgedDepthsStructure> FORGED_DEPTHS_TYPE;
	public static StructurePieceType FORGE_ROOM;

	public static void register() {
		FORGED_DEPTHS_TYPE = Registry.register(Registries.STRUCTURE_TYPE, ForgedDepths.id("forged_depths"),
				(StructureType<ForgedDepthsStructure>) () -> ForgedDepthsStructure.CODEC);

		FORGE_ROOM = Registry.register(Registries.STRUCTURE_PIECE, ForgedDepths.id("forge_room"),
				(StructurePieceType.Simple) ForgedDepthsPiece::new);
	}
}
