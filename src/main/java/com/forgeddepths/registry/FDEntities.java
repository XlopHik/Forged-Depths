package com.forgeddepths.registry;

import com.forgeddepths.ForgedDepths;
import com.forgeddepths.entity.AshenSmithEntity;
import com.forgeddepths.entity.EmberWispEntity;
import com.forgeddepths.entity.SlagGolemEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.Heightmap;

public final class FDEntities {
	private FDEntities() {
	}

	public static final EntityType<AshenSmithEntity> ASHEN_SMITH = register("ashen_smith",
			EntityType.Builder.create(AshenSmithEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.7F, 2.1F)
					.makeFireImmune()
					.maxTrackingRange(10));

	public static final EntityType<EmberWispEntity> EMBER_WISP = register("ember_wisp",
			EntityType.Builder.create(EmberWispEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.6F, 1.8F)
					.makeFireImmune()
					.maxTrackingRange(8));

	public static final EntityType<SlagGolemEntity> SLAG_GOLEM = register("slag_golem",
			EntityType.Builder.create(SlagGolemEntity::new, SpawnGroup.MONSTER)
					.dimensions(1.4F, 2.7F)
					.makeFireImmune()
					.maxTrackingRange(10));

	public static boolean isForgeGuardian(Entity entity) {
		return entity instanceof AshenSmithEntity
				|| entity instanceof EmberWispEntity
				|| entity instanceof SlagGolemEntity;
	}

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, ForgedDepths.id(name));
		return Registry.register(Registries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(ASHEN_SMITH, AshenSmithEntity.createAshenSmithAttributes());
		FabricDefaultAttributeRegistry.register(EMBER_WISP, EmberWispEntity.createEmberWispAttributes());
		FabricDefaultAttributeRegistry.register(SLAG_GOLEM, SlagGolemEntity.createSlagGolemAttributes());

		registerSpawning(ASHEN_SMITH);
		registerSpawning(EMBER_WISP);
		registerSpawning(SLAG_GOLEM);
	}

	private static <T extends MobEntity> void registerSpawning(EntityType<T> type) {
		SpawnRestriction.register(type, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
				(entityType, world, reason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL);
	}
}
