package com.forgeddepths.world;

import com.forgeddepths.registry.FDStructures;
import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

import java.util.Optional;

public final class DungeonLocator {
	private DungeonLocator() {
	}

	public static final int DEFAULT_SEARCH_RADIUS = 100;

	private static Optional<Structure> structure(ServerWorld world) {
		Registry<Structure> registry = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
		return registry.getOptionalValue(FDStructures.FORGED_DEPTHS);
	}

	public static Optional<BlockPos> findNearest(ServerWorld world, BlockPos origin, int radiusChunks) {
		Registry<Structure> registry = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
		Optional<RegistryEntry.Reference<Structure>> entry = registry.getEntry(FDStructures.FORGED_DEPTHS.getValue());

		if (entry.isEmpty()) {
			return Optional.empty();
		}

		RegistryEntryList<Structure> list = RegistryEntryList.of(entry.get());
		Pair<BlockPos, RegistryEntry<Structure>> result = world.getChunkManager().getChunkGenerator()
				.locateStructure(world, list, origin, radiusChunks, false);

		return result == null ? Optional.empty() : Optional.of(result.getFirst());
	}

	public static boolean isInsideDungeon(ServerWorld world, BlockPos pos) {
		Optional<Structure> structure = structure(world);

		if (structure.isEmpty()) {
			return false;
		}

		StructureStart start = world.getStructureAccessor().getStructureContaining(pos, structure.get());
		return start != null && start.hasChildren();
	}
}
