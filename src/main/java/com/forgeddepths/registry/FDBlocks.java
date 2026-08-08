package com.forgeddepths.registry;

import com.forgeddepths.ForgedDepths;
import com.forgeddepths.block.CrumblingStoneBlock;
import com.forgeddepths.block.ForgeAnvilBlock;
import com.forgeddepths.block.GuideRuneBlock;
import com.forgeddepths.block.MagmaVentBlock;
import com.forgeddepths.block.RuneTrapBlock;
import com.forgeddepths.block.SealedGateBlock;
import com.forgeddepths.block.SoulSpikeBlock;
import com.forgeddepths.block.TrialAltarBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class FDBlocks {
	private FDBlocks() {
	}

	public static final List<Block> ALL = new ArrayList<>();

	public static final Block SCORCHED_DEEPSLATE = register("scorched_deepslate", Block::new,
			AbstractBlock.Settings.copy(Blocks.DEEPSLATE).strength(3.5F, 8.0F));

	public static final Block SCORCHED_DEEPSLATE_BRICKS = register("scorched_deepslate_bricks", Block::new,
			AbstractBlock.Settings.copy(Blocks.DEEPSLATE_BRICKS).strength(3.5F, 8.0F));

	public static final Block CRACKED_SCORCHED_DEEPSLATE_BRICKS = register("cracked_scorched_deepslate_bricks",
			Block::new, AbstractBlock.Settings.copy(Blocks.CRACKED_DEEPSLATE_BRICKS).strength(3.0F, 8.0F));

	public static final Block CINDERFORGED_COPPER = register("cinderforged_copper", Block::new,
			AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK).strength(4.0F, 8.0F)
					.sounds(BlockSoundGroup.COPPER));

	public static final Block SLAG_BLOCK = register("slag_block", Block::new,
			AbstractBlock.Settings.copy(Blocks.BASALT).strength(2.5F, 6.0F));

	public static final Block EMBER_LANTERN = register("ember_lantern", Block::new,
			AbstractBlock.Settings.copy(Blocks.SHROOMLIGHT).luminance(state -> 15));

	public static final Block FORGE_ANVIL = register("forge_anvil", ForgeAnvilBlock::new,
			AbstractBlock.Settings.copy(Blocks.ANVIL)
					.strength(6.0F, 1200.0F)
					.sounds(BlockSoundGroup.ANVIL)
					.pistonBehavior(PistonBehavior.BLOCK)
					.nonOpaque());

	public static final Block CRUMBLING_STONE = register("crumbling_stone", CrumblingStoneBlock::new,
			AbstractBlock.Settings.copy(Blocks.DEEPSLATE_TILES).strength(1.0F, 2.0F));

	public static final Block SOUL_SPIKES = register("soul_spikes", SoulSpikeBlock::new,
			AbstractBlock.Settings.copy(Blocks.IRON_BARS)
					.strength(3.0F, 6.0F)
					.nonOpaque()
					.noCollision());

	public static final Block MAGMA_VENT = register("magma_vent", MagmaVentBlock::new,
			AbstractBlock.Settings.copy(Blocks.MAGMA_BLOCK)
					.strength(2.0F, 6.0F)
					.luminance(state -> state.get(MagmaVentBlock.ACTIVE) ? 13 : 5));

	public static final Block TRIAL_ALTAR = register("trial_altar", TrialAltarBlock::new,
			AbstractBlock.Settings.copy(Blocks.CHISELED_DEEPSLATE)
					.strength(8.0F, 1200.0F)
					.luminance(state -> state.get(TrialAltarBlock.ACTIVE) ? 12 : 6));

	public static final Block SEALED_GATE = register("sealed_gate", SealedGateBlock::new,
			AbstractBlock.Settings.copy(Blocks.COPPER_BLOCK)
					.strength(25.0F, 1200.0F)
					.sounds(BlockSoundGroup.COPPER)
					.nonOpaque()
					.luminance(state -> 4));

	public static final Block GUIDE_RUNE = register("guide_rune", GuideRuneBlock::new,
			AbstractBlock.Settings.copy(Blocks.DEEPSLATE_TILES)
					.strength(2.0F, 6.0F)
					.nonOpaque()
					.noCollision()
					.luminance(state -> 9));

	public static final Block RUNE_TRAP = register("rune_trap", RuneTrapBlock::new,
			AbstractBlock.Settings.copy(Blocks.DEEPSLATE_TILES)
					.strength(2.0F, 6.0F)
					.nonOpaque()
					.noCollision()
					.luminance(state -> state.get(RuneTrapBlock.TRIGGERED) ? 2 : 7));

	private static Block register(String name, Function<AbstractBlock.Settings, Block> factory,
			AbstractBlock.Settings settings) {
		RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, ForgedDepths.id(name));
		Block block = factory.apply(settings.registryKey(blockKey));
		Registry.register(Registries.BLOCK, blockKey, block);

		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, ForgedDepths.id(name));
		Registry.register(Registries.ITEM, itemKey,
				new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey()));

		ALL.add(block);
		return block;
	}

	public static void register() {
	}

	public static boolean isForgeMasonry(Block block) {
		return block == SCORCHED_DEEPSLATE || block == SCORCHED_DEEPSLATE_BRICKS
				|| block == CRACKED_SCORCHED_DEEPSLATE_BRICKS || block == CINDERFORGED_COPPER;
	}
}
