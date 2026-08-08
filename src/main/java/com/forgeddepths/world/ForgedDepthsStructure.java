package com.forgeddepths.world;

import com.forgeddepths.registry.FDStructures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.Optional;

public class ForgedDepthsStructure extends Structure {
	public static final MapCodec<ForgedDepthsStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					configCodecBuilder(instance),
					Codec.intRange(-64, 320).optionalFieldOf("min_floor_y", -58)
							.forGetter(structure -> structure.minFloorY),
					Codec.intRange(-64, 320).optionalFieldOf("max_floor_y", -50)
							.forGetter(structure -> structure.maxFloorY)
			).apply(instance, ForgedDepthsStructure::new));

	private static final RegistryKey<Biome> DEEP_BIOME = BiomeKeys.DEEP_DARK;

	private final int minFloorY;
	private final int maxFloorY;

	public ForgedDepthsStructure(Config config, int minFloorY, int maxFloorY) {
		super(config);
		this.minFloorY = minFloorY;
		this.maxFloorY = maxFloorY;
	}

	@Override
	protected Optional<StructurePosition> getStructurePosition(Context context) {
		ChunkGenerator generator = context.chunkGenerator();
		int bottom = generator.getMinimumY();

		int lowest = Math.max(minFloorY, bottom + ForgedDepthsGenerator.SUBFLOOR + 1);
		int highest = Math.max(lowest, maxFloorY);

		if (highest + ForgedDepthsGenerator.ROOM_TOP >= bottom + generator.getWorldHeight()) {
			return Optional.empty();
		}

		ChunkRandom random = context.random();
		int floorY = lowest + random.nextInt(highest - lowest + 1);

		ChunkPos chunkPos = context.chunkPos();
		int x = chunkPos.getStartX();
		int z = chunkPos.getStartZ();
		BlockPos origin = new BlockPos(x, floorY, z);

		int probeY = isDeepBiome(context, x, floorY, z)
				? floorY
				: generator.getHeight(x, z, Heightmap.Type.WORLD_SURFACE_WG, context.world(), context.noiseConfig());

		return Optional.of(new StructurePosition(new BlockPos(x, probeY, z),
				collector -> ForgedDepthsGenerator.generate(collector, origin, random)));
	}

	private static boolean isDeepBiome(Context context, int x, int y, int z) {
		MultiNoiseUtil.MultiNoiseSampler sampler = context.noiseConfig().getMultiNoiseSampler();
		return context.biomeSource().getBiome(
				BiomeCoords.fromBlock(x), BiomeCoords.fromBlock(y), BiomeCoords.fromBlock(z), sampler)
				.matchesKey(DEEP_BIOME);
	}

	@Override
	public StructureType<?> getType() {
		return FDStructures.FORGED_DEPTHS_TYPE;
	}
}
