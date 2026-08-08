package com.forgeddepths.world;

import com.forgeddepths.block.ForgeAnvilBlock;
import com.forgeddepths.registry.FDBlocks;
import com.forgeddepths.registry.FDEntities;
import com.forgeddepths.registry.FDLootTables;
import com.forgeddepths.registry.FDStructures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChainBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class ForgedDepthsPiece extends StructurePiece {
	private static final String ROOM_TYPE_KEY = "RoomType";

	private static final int FLOOR = ForgedDepthsGenerator.SUBFLOOR;
	private static final int CEILING = FLOOR + ForgedDepthsGenerator.ROOM_TOP;
	private static final int CORRIDOR_CEILING = FLOOR + 5;

	private static final int MAX = ForgedDepthsGenerator.ROOM_SIZE - 1;

	private static final BlockState AIR = Blocks.CAVE_AIR.getDefaultState();
	private static final BlockState LAVA = Blocks.LAVA.getDefaultState();

	private static final String GUIDE_KEY = "Guide";

	private final RoomType roomType;
	private final Direction guide;

	public ForgedDepthsPiece(RoomType roomType, BlockBox box, Direction guide) {
		super(FDStructures.FORGE_ROOM, 0, box);
		this.roomType = roomType;
		this.guide = guide;

		setOrientation(Direction.SOUTH);
	}

	public ForgedDepthsPiece(NbtCompound nbt) {
		super(FDStructures.FORGE_ROOM, nbt);
		this.roomType = RoomType.byName(nbt.getString(ROOM_TYPE_KEY, ""));
		this.guide = Direction.byId(nbt.getString(GUIDE_KEY, ""));
	}

	@Override
	protected void writeNbt(StructureContext context, NbtCompound nbt) {
		nbt.putString(ROOM_TYPE_KEY, roomType.asString());
		nbt.putString(GUIDE_KEY, guide == null ? "" : guide.asString());
	}

	private BlockPos worldPos(int x, int y, int z) {
		return new BlockPos(boundingBox.getMinX() + x, boundingBox.getMinY() + y, boundingBox.getMinZ() + z);
	}

	@Override
	protected void addBlock(StructureWorldAccess world, BlockState state, int x, int y, int z, BlockBox chunkBox) {
		BlockPos pos = worldPos(x, y, z);

		if (chunkBox.contains(pos)) {
			world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
		}
	}

	@Override
	public void generate(StructureWorldAccess world, StructureAccessor accessor, ChunkGenerator chunkGenerator,
			Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
		if (roomType.isCorridor()) {
			buildCorridor(world, chunkBox, random);
			placeCorridorGuides(world, chunkBox);
			return;
		}

		buildShell(world, chunkBox, random);

		switch (roomType) {
			case TRAP_CORRIDOR -> decorateTrapCorridor(world, chunkBox, random);
			case FORGE_HALL -> decorateForgeHall(world, chunkBox, random);
			case TREASURY -> decorateTreasury(world, chunkBox, random);
			case BARRACKS -> decorateBarracks(world, chunkBox, random);
			case CRUCIBLE -> decorateCrucible(world, chunkBox, random);
			case ARCHIVE -> decorateArchive(world, chunkBox, random);
			case SMELTERY -> decorateSmeltery(world, chunkBox, random);
			case COLLAPSED_HALL -> decorateCollapsedHall(world, chunkBox, random);
			case CELLS -> decorateCells(world, chunkBox, random);
			case TRIAL_HALL -> decorateTrialHall(world, chunkBox, random);
			case FLOODED_FORGE -> decorateFloodedForge(world, chunkBox, random);
			case ANCESTOR_GALLERY -> decorateAncestorGallery(world, chunkBox, random);
			default -> decorateEntrance(world, chunkBox, random);
		}

		placeRoomGuides(world, chunkBox);
	}

	private void placeRoomGuides(StructureWorldAccess world, BlockBox box) {
		if (guide == null) {
			return;
		}

		int mid = MAX / 2;
		int[][] spots = {{mid, 2}, {mid, MAX - 2}, {2, mid}, {MAX - 2, mid}};

		for (int[] spot : spots) {
			addBlock(world, guideState(), spot[0], FLOOR + 1, spot[1], box);
		}
	}

	private void placeCorridorGuides(StructureWorldAccess world, BlockBox box) {
		if (guide == null) {
			return;
		}

		int lengthX = boundingBox.getBlockCountX();
		int lengthZ = boundingBox.getBlockCountZ();

		if (roomType == RoomType.CORRIDOR_X) {
			addBlock(world, guideState(), 1, FLOOR + 1, lengthZ / 2, box);
			addBlock(world, guideState(), lengthX - 2, FLOOR + 1, lengthZ / 2, box);
		} else {
			addBlock(world, guideState(), lengthX / 2, FLOOR + 1, 1, box);
			addBlock(world, guideState(), lengthX / 2, FLOOR + 1, lengthZ - 2, box);
		}
	}

	private BlockState guideState() {
		return FDBlocks.GUIDE_RUNE.getDefaultState().with(Properties.HORIZONTAL_FACING, guide);
	}

	private void buildShell(StructureWorldAccess world, BlockBox box, Random random) {
		for (int x = 0; x <= MAX; x++) {
			for (int z = 0; z <= MAX; z++) {
				for (int y = 0; y < FLOOR; y++) {
					addBlock(world, FDBlocks.SCORCHED_DEEPSLATE.getDefaultState(), x, y, z, box);
				}

				addBlock(world, floorState(random), x, FLOOR, z, box);
				addBlock(world, wallState(random), x, CEILING, z, box);
			}
		}

		for (int y = FLOOR + 1; y < CEILING; y++) {
			for (int x = 0; x <= MAX; x++) {
				for (int z = 0; z <= MAX; z++) {
					boolean edge = x == 0 || x == MAX || z == 0 || z == MAX;
					addBlock(world, edge ? wallState(random) : AIR, x, y, z, box);
				}
			}
		}

		for (int[] corner : new int[][]{{1, 1}, {1, MAX - 1}, {MAX - 1, 1}, {MAX - 1, MAX - 1}}) {
			for (int y = FLOOR + 1; y < CEILING; y++) {
				addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), corner[0], y, corner[1], box);
			}

			addBlock(world, FDBlocks.EMBER_LANTERN.getDefaultState(), corner[0], CEILING - 1, corner[1], box);
		}
	}

	private void buildCorridor(StructureWorldAccess world, BlockBox box, Random random) {
		int lengthX = boundingBox.getBlockCountX();
		int lengthZ = boundingBox.getBlockCountZ();

		for (int x = 0; x < lengthX; x++) {
			for (int z = 0; z < lengthZ; z++) {
				for (int y = 0; y < FLOOR; y++) {
					addBlock(world, FDBlocks.SCORCHED_DEEPSLATE.getDefaultState(), x, y, z, box);
				}

				addBlock(world, floorState(random), x, FLOOR, z, box);
				addBlock(world, wallState(random), x, CORRIDOR_CEILING, z, box);

				for (int y = FLOOR + 1; y < CORRIDOR_CEILING; y++) {
					boolean wall = roomType == RoomType.CORRIDOR_X
							? (z == 0 || z == lengthZ - 1)
							: (x == 0 || x == lengthX - 1);
					addBlock(world, wall ? wallState(random) : AIR, x, y, z, box);
				}
			}
		}

		int midX = lengthX / 2;
		int midZ = lengthZ / 2;
		addBlock(world, FDBlocks.EMBER_LANTERN.getDefaultState(), midX, CORRIDOR_CEILING - 1, midZ, box);

		if (random.nextInt(3) == 0) {
			addBlock(world, Blocks.COBWEB.getDefaultState(), midX, FLOOR + 1, midZ, box);
		}

		if (random.nextInt(4) == 0) {
			addBlock(world, FDBlocks.CRUMBLING_STONE.getDefaultState(), midX, FLOOR, midZ, box);
			addBlock(world, LAVA, midX, FLOOR - 1, midZ, box);
		}
	}

	private void decorateEntrance(StructureWorldAccess world, BlockBox box, Random random) {
		int top = boundingBox.getBlockCountY() - 1;

		for (int y = CEILING; y <= top; y++) {
			for (int x = 5; x <= 9; x++) {
				for (int z = 5; z <= 9; z++) {
					boolean edge = x == 5 || x == 9 || z == 5 || z == 9;
					addBlock(world, edge ? wallState(random) : AIR, x, y, z, box);
				}
			}

			addBlock(world, Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH),
					7, y, 6, box);
		}

		for (int x = 6; x <= 8; x++) {
			for (int z = 6; z <= 8; z++) {
				addBlock(world, FDBlocks.SCORCHED_DEEPSLATE_BRICKS.getDefaultState(), x, top, z, box);
			}
		}

		addChest(world, box, random, 8, top - 1, 8, FDLootTables.ENTRANCE_STASH);

		for (int i = 0; i < 12; i++) {
			int x = 2 + random.nextInt(MAX - 3);
			int z = 2 + random.nextInt(MAX - 3);
			addBlock(world, FDBlocks.SLAG_BLOCK.getDefaultState(), x, FLOOR + 1, z, box);
		}

		addBlock(world, Blocks.CHISELED_DEEPSLATE.getDefaultState(), 7, FLOOR + 1, 2, box);
		addBlock(world, FDBlocks.EMBER_LANTERN.getDefaultState(), 7, FLOOR + 2, 2, box);
	}

	private void decorateTrapCorridor(StructureWorldAccess world, BlockBox box, Random random) {
		for (int x = 2; x <= MAX - 2; x++) {
			for (int z = 2; z <= MAX - 2; z++) {
				addBlock(world, LAVA, x, FLOOR - 1, z, box);
			}
		}

		for (int x = 2; x <= MAX - 2; x++) {
			for (int z = 2; z <= MAX - 2; z++) {
				if (z == 2) {
					continue;
				}

				addBlock(world, FDBlocks.CRUMBLING_STONE.getDefaultState(), x, FLOOR, z, box);
			}
		}

		for (int x = 3; x <= MAX - 3; x += 2) {
			addBlock(world, FDBlocks.SOUL_SPIKES.getDefaultState(), x, FLOOR + 1, 2, box);
		}

		for (int i = 0; i < 5; i++) {
			int x = 2 + random.nextInt(MAX - 3);
			int z = 3 + random.nextInt(MAX - 5);
			addBlock(world, FDBlocks.MAGMA_VENT.getDefaultState(), x, FLOOR, z, box);
		}

		for (int x = 4; x <= MAX - 4; x += 4) {
			addBlock(world, FDBlocks.RUNE_TRAP.getDefaultState(), x, FLOOR + 1, 2, box);
			addBlock(world, FDBlocks.RUNE_TRAP.getDefaultState(), 1, FLOOR + 1, x, box);
			addBlock(world, FDBlocks.RUNE_TRAP.getDefaultState(), MAX - 1, FLOOR + 1, x, box);
		}

		for (int i = 0; i < 6; i++) {
			addBlock(world, Blocks.COBWEB.getDefaultState(),
					2 + random.nextInt(MAX - 3), FLOOR + 1 + random.nextInt(3), 2 + random.nextInt(MAX - 3), box);
		}

		hangChains(world, box, random, 4);
		addBlock(world, FDBlocks.RUNE_TRAP.getDefaultState(), 7, FLOOR + 1, MAX - 2, box);
	}

	private void decorateForgeHall(StructureWorldAccess world, BlockBox box, Random random) {
		buildVault(world, box, random);
		layRadialFloor(world, box);
		pourLavaMoat(world, box);
		raisePedestal(world, box);
		raisePillars(world, box);
		buildFurnaceWall(world, box);

		addChest(world, box, random, 2, FLOOR + 1, MAX - 2, FDLootTables.FORGE_HALL);
		addChest(world, box, random, MAX - 2, FLOOR + 1, MAX - 2, FDLootTables.FORGE_HALL);

		hangChains(world, box, random, 4);

		spawnMob(world, box, FDEntities.SLAG_GOLEM, 7, FLOOR + 1, 12);
		spawnMob(world, box, FDEntities.ASHEN_SMITH, 7, FLOOR + 1, 11);
		spawnMob(world, box, FDEntities.EMBER_WISP, 4, FLOOR + 3, 7);
		spawnMob(world, box, FDEntities.EMBER_WISP, 10, FLOOR + 3, 7);
	}

	private void buildVault(StructureWorldAccess world, BlockBox box, Random random) {
		int top = boundingBox.getBlockCountY() - 1;

		for (int y = CEILING; y <= top; y++) {
			int inset = y - CEILING;
			int low = 2 + inset;
			int high = MAX - 2 - inset;

			for (int x = 0; x <= MAX; x++) {
				for (int z = 0; z <= MAX; z++) {
					boolean open = low <= high && x >= low && x <= high && z >= low && z <= high;
					addBlock(world, open ? AIR : wallState(random), x, y, z, box);
				}
			}

			if (low < high) {
				addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), low, y, low, box);
				addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), low, y, high, box);
				addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), high, y, low, box);
				addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), high, y, high, box);
			}
		}
	}

	private void layRadialFloor(StructureWorldAccess world, BlockBox box) {
		for (int x = 1; x < MAX; x++) {
			for (int z = 1; z < MAX; z++) {
				int ring = Math.max(Math.abs(x - 7), Math.abs(z - 7));

				if (ring == 3 || ring == 6) {
					addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), x, FLOOR, z, box);
				} else if (ring == 5) {
					addBlock(world, FDBlocks.SCORCHED_DEEPSLATE_BRICKS.getDefaultState(), x, FLOOR, z, box);
				}
			}
		}
	}

	private void pourLavaMoat(StructureWorldAccess world, BlockBox box) {
		for (int x = 1; x < MAX; x++) {
			for (int z = 1; z < MAX; z++) {
				if (Math.max(Math.abs(x - 7), Math.abs(z - 7)) != 4) {
					continue;
				}

				if (x == 7 || z == 7) {
					addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), x, FLOOR, z, box);
				} else {
					addBlock(world, LAVA, x, FLOOR, z, box);
				}
			}
		}
	}

	private void raisePedestal(StructureWorldAccess world, BlockBox box) {
		for (int x = 5; x <= 9; x++) {
			for (int z = 5; z <= 9; z++) {
				addBlock(world, FDBlocks.SCORCHED_DEEPSLATE_BRICKS.getDefaultState(), x, FLOOR + 1, z, box);
			}
		}

		for (int x = 6; x <= 8; x++) {
			for (int z = 6; z <= 8; z++) {
				addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), x, FLOOR + 2, z, box);
			}
		}

		addBlock(world, FDBlocks.FORGE_ANVIL.getDefaultState()
				.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
				.with(ForgeAnvilBlock.CHARGES, ForgeAnvilBlock.MAX_CHARGES), 7, FLOOR + 3, 7, box);

		for (int[] corner : new int[][]{{5, 5}, {5, 9}, {9, 5}, {9, 9}}) {
			addBlock(world, FDBlocks.EMBER_LANTERN.getDefaultState(), corner[0], FLOOR + 2, corner[1], box);
		}
	}

	private void raisePillars(StructureWorldAccess world, BlockBox box) {
		for (int[] corner : new int[][]{{2, 2}, {2, MAX - 2}, {MAX - 2, 2}, {MAX - 2, MAX - 2}}) {
			for (int y = FLOOR + 1; y <= CEILING + 1; y++) {
				addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), corner[0], y, corner[1], box);
			}

			addBlock(world, FDBlocks.EMBER_LANTERN.getDefaultState(), corner[0], CEILING + 2, corner[1], box);
		}
	}

	private void buildFurnaceWall(StructureWorldAccess world, BlockBox box) {
		for (int x : new int[]{3, 4, MAX - 4, MAX - 3}) {
			addBlock(world, Blocks.BLAST_FURNACE.getDefaultState()
					.with(Properties.HORIZONTAL_FACING, Direction.SOUTH), x, FLOOR + 1, 1, box);
			addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), x, FLOOR + 2, 1, box);
		}

		addBlock(world, Blocks.SMITHING_TABLE.getDefaultState(), 1, FLOOR + 1, 5, box);
		addBlock(world, Blocks.GRINDSTONE.getDefaultState(), 1, FLOOR + 1, 9, box);
	}

	private void decorateTreasury(StructureWorldAccess world, BlockBox box, Random random) {
		for (int x = 4; x <= 10; x++) {
			for (int z = 4; z <= 10; z++) {
				boolean edge = x == 4 || x == 10 || z == 4 || z == 10;

				if (!edge) {
					continue;
				}

				for (int y = FLOOR + 1; y <= FLOOR + 4; y++) {
					boolean bars = y == FLOOR + 2 || y == FLOOR + 3;
					addBlock(world, bars ? Blocks.IRON_BARS.getDefaultState()
							: FDBlocks.CINDERFORGED_COPPER.getDefaultState(), x, y, z, box);
				}
			}
		}

		for (int y = FLOOR + 1; y <= FLOOR + 3; y++) {
			addBlock(world, FDBlocks.SEALED_GATE.getDefaultState(), 7, y, 10, box);
		}

		addChest(world, box, random, 5, FLOOR + 1, 5, FDLootTables.TREASURY);
		addChest(world, box, random, 9, FLOOR + 1, 5, FDLootTables.TREASURY);
		addChest(world, box, random, 7, FLOOR + 1, 5, FDLootTables.TREASURY);

		addBlock(world, FDBlocks.RUNE_TRAP.getDefaultState(), 7, FLOOR + 1, 11, box);
		addBlock(world, FDBlocks.RUNE_TRAP.getDefaultState(), 7, FLOOR + 1, 9, box);
		addBlock(world, FDBlocks.SOUL_SPIKES.getDefaultState(), 6, FLOOR + 1, 9, box);
		addBlock(world, FDBlocks.SOUL_SPIKES.getDefaultState(), 8, FLOOR + 1, 9, box);

		addBlock(world, FDBlocks.EMBER_LANTERN.getDefaultState(), 7, FLOOR + 4, 7, box);

		spawnMob(world, box, FDEntities.EMBER_WISP, 2, FLOOR + 2, 2);
		spawnMob(world, box, FDEntities.EMBER_WISP, MAX - 2, FLOOR + 2, MAX - 2);
	}

	private void decorateBarracks(StructureWorldAccess world, BlockBox box, Random random) {
		spawnMob(world, box, FDEntities.ASHEN_SMITH, 3, FLOOR + 1, 3);
		spawnMob(world, box, FDEntities.EMBER_WISP, MAX - 3, FLOOR + 2, MAX - 3);
		spawnMob(world, box, FDEntities.EMBER_WISP, 3, FLOOR + 2, MAX - 3);

		for (int z = 2; z <= MAX - 2; z += 4) {
			for (int y = FLOOR + 1; y <= FLOOR + 3; y++) {
				addBlock(world, Blocks.IRON_BARS.getDefaultState(), 7, y, z, box);
			}
		}

		addBlock(world, Blocks.SMITHING_TABLE.getDefaultState(), 2, FLOOR + 1, 7, box);
		addBlock(world, Blocks.GRINDSTONE.getDefaultState(), MAX - 2, FLOOR + 1, 7, box);
		addChest(world, box, random, 2, FLOOR + 1, MAX - 2, FDLootTables.BARRACKS);
		addChest(world, box, random, MAX - 2, FLOOR + 1, 2, FDLootTables.BARRACKS);

		for (int i = 0; i < 4; i++) {
			addBlock(world, FDBlocks.SOUL_SPIKES.getDefaultState(),
					2 + random.nextInt(MAX - 3), FLOOR + 1, 2 + random.nextInt(MAX - 3), box);
		}

		for (int i = 0; i < 3; i++) {
			addBlock(world, FDBlocks.RUNE_TRAP.getDefaultState(),
					2 + random.nextInt(MAX - 3), FLOOR + 1, 2 + random.nextInt(MAX - 3), box);
		}

		hangChains(world, box, random, 3);
	}

	private void decorateCrucible(StructureWorldAccess world, BlockBox box, Random random) {
		for (int x = 2; x <= MAX - 2; x++) {
			for (int z = 2; z <= MAX - 2; z++) {
				for (int y = FLOOR - 2; y <= FLOOR; y++) {
					addBlock(world, LAVA, x, y, z, box);
				}
			}
		}

		for (int i = 1; i <= MAX - 1; i++) {
			for (int offset = 6; offset <= 8; offset++) {
				addBlock(world, bridgeState(random), i, FLOOR, offset, box);
				addBlock(world, bridgeState(random), offset, FLOOR, i, box);
			}
		}

		for (int i = 4; i <= MAX - 4; i += 3) {
			addBlock(world, FDBlocks.MAGMA_VENT.getDefaultState(), i, FLOOR, 6, box);
			addBlock(world, FDBlocks.MAGMA_VENT.getDefaultState(), 8, FLOOR, i, box);
		}

		for (int x = 10; x <= 12; x++) {
			for (int z = 10; z <= 12; z++) {
				addBlock(world, FDBlocks.SCORCHED_DEEPSLATE_BRICKS.getDefaultState(), x, FLOOR, z, box);
			}
		}

		addChest(world, box, random, 11, FLOOR + 1, 11, FDLootTables.CRUCIBLE);
		addBlock(world, FDBlocks.EMBER_LANTERN.getDefaultState(), 11, FLOOR + 1, 12, box);

		hangChains(world, box, random, 5);
		spawnMob(world, box, FDEntities.EMBER_WISP, 7, FLOOR + 3, 4);
	}

	private void decorateArchive(StructureWorldAccess world, BlockBox box, Random random) {
		for (int z = 2; z <= MAX - 2; z++) {
			boolean gap = z % 4 == 0;

			for (int y = FLOOR + 1; y <= FLOOR + 3; y++) {
				addBlock(world, gap ? AIR : Blocks.BOOKSHELF.getDefaultState(), 2, y, z, box);
				addBlock(world, gap ? AIR : Blocks.BOOKSHELF.getDefaultState(), MAX - 2, y, z, box);
			}
		}

		for (int x = 6; x <= 8; x++) {
			for (int z = 6; z <= 8; z++) {
				addBlock(world, Blocks.POLISHED_DEEPSLATE.getDefaultState(), x, FLOOR + 1, z, box);
			}
		}

		addBlock(world, Blocks.ENCHANTING_TABLE.getDefaultState(), 7, FLOOR + 2, 7, box);
		addBlock(world, Blocks.LECTERN.getDefaultState(), 5, FLOOR + 1, 7, box);
		addBlock(world, Blocks.LECTERN.getDefaultState(), 9, FLOOR + 1, 7, box);
		addBlock(world, Blocks.CHISELED_BOOKSHELF.getDefaultState(), 7, FLOOR + 1, 2, box);

		addChest(world, box, random, 4, FLOOR + 1, 12, FDLootTables.ARCHIVE);
		addChest(world, box, random, MAX - 4, FLOOR + 1, 12, FDLootTables.ARCHIVE);

		for (int i = 0; i < 8; i++) {
			addBlock(world, Blocks.COBWEB.getDefaultState(),
					2 + random.nextInt(MAX - 3), FLOOR + 1 + random.nextInt(4), 2 + random.nextInt(MAX - 3), box);
		}

		addBlock(world, FDBlocks.RUNE_TRAP.getDefaultState(), 7, FLOOR + 1, 10, box);
		hangChains(world, box, random, 4);
		spawnMob(world, box, FDEntities.EMBER_WISP, 4, FLOOR + 3, 4);
	}

	private void decorateSmeltery(StructureWorldAccess world, BlockBox box, Random random) {
		BlockState[] ores = {
				Blocks.DEEPSLATE_IRON_ORE.getDefaultState(),
				Blocks.DEEPSLATE_GOLD_ORE.getDefaultState(),
				Blocks.DEEPSLATE_COPPER_ORE.getDefaultState(),
				Blocks.DEEPSLATE_DIAMOND_ORE.getDefaultState(),
				Blocks.DEEPSLATE_REDSTONE_ORE.getDefaultState(),
		};

		for (int i = 0; i < 18; i++) {
			int y = FLOOR + 1 + random.nextInt(6);
			BlockState ore = ores[random.nextInt(ores.length)];

			if (random.nextBoolean()) {
				addBlock(world, ore, random.nextBoolean() ? 0 : MAX, y, 2 + random.nextInt(MAX - 3), box);
			} else {
				addBlock(world, ore, 2 + random.nextInt(MAX - 3), y, random.nextBoolean() ? 0 : MAX, box);
			}
		}

		for (int x = 2; x <= MAX - 2; x++) {
			addBlock(world, LAVA, x, FLOOR, 7, box);
			addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), x, FLOOR - 1, 7, box);
		}

		for (int x = 3; x <= MAX - 3; x += 3) {
			addBlock(world, Blocks.BLAST_FURNACE.getDefaultState()
					.with(Properties.HORIZONTAL_FACING, Direction.SOUTH), x, FLOOR + 1, 2, box);
			addBlock(world, Blocks.FURNACE.getDefaultState()
					.with(Properties.HORIZONTAL_FACING, Direction.NORTH), x, FLOOR + 1, MAX - 2, box);
		}

		addBlock(world, Blocks.CAULDRON.getDefaultState(), 2, FLOOR + 1, 4, box);
		addBlock(world, Blocks.CAULDRON.getDefaultState(), MAX - 2, FLOOR + 1, 10, box);

		addChest(world, box, random, 2, FLOOR + 1, 11, FDLootTables.SMELTERY);
		addChest(world, box, random, MAX - 2, FLOOR + 1, 3, FDLootTables.SMELTERY);

		for (int x = 4; x <= MAX - 4; x += 4) {
			addBlock(world, FDBlocks.MAGMA_VENT.getDefaultState(), x, FLOOR, 4, box);
			addBlock(world, FDBlocks.MAGMA_VENT.getDefaultState(), x, FLOOR, 10, box);
		}

		hangChains(world, box, random, 5);
		spawnMob(world, box, FDEntities.ASHEN_SMITH, 7, FLOOR + 1, 11);
	}

	private void decorateCollapsedHall(StructureWorldAccess world, BlockBox box, Random random) {
		BlockState[] rubble = {
				Blocks.GRAVEL.getDefaultState(),
				Blocks.COBBLED_DEEPSLATE.getDefaultState(),
				FDBlocks.SLAG_BLOCK.getDefaultState(),
				FDBlocks.CRACKED_SCORCHED_DEEPSLATE_BRICKS.getDefaultState(),
		};

		for (int x = 1; x <= MAX - 1; x++) {
			for (int z = 1; z <= MAX - 1; z++) {
				int height = Math.max(0, 7 - (x + z) / 2 + random.nextInt(2));

				for (int y = 0; y < height; y++) {
					addBlock(world, rubble[random.nextInt(rubble.length)], x, FLOOR + 1 + y, z, box);
				}
			}
		}

		for (int x = 2; x <= 6; x++) {
			for (int z = 2; z <= 6; z++) {
				addBlock(world, AIR, x, CEILING, z, box);
			}
		}

		addChest(world, box, random, 2, FLOOR + 1, 2, FDLootTables.RUBBLE);
		addChest(world, box, random, MAX - 2, FLOOR + 1, MAX - 2, FDLootTables.RUBBLE);

		for (int i = 0; i < 10; i++) {
			addBlock(world, Blocks.COBWEB.getDefaultState(),
					2 + random.nextInt(MAX - 3), FLOOR + 1 + random.nextInt(5), 2 + random.nextInt(MAX - 3), box);
		}

		for (int i = 0; i < 5; i++) {
			addBlock(world, FDBlocks.CRUMBLING_STONE.getDefaultState(),
					8 + random.nextInt(5), FLOOR, 8 + random.nextInt(5), box);
		}

		spawnMob(world, box, FDEntities.EMBER_WISP, 11, FLOOR + 2, 11);
	}

	private void decorateCells(StructureWorldAccess world, BlockBox box, Random random) {
		int[][] corners = {{1, 1}, {1, 9}, {9, 1}, {9, 9}};

		for (int index = 0; index < corners.length; index++) {
			int ox = corners[index][0];
			int oz = corners[index][1];

			for (int x = ox; x <= ox + 4; x++) {
				for (int z = oz; z <= oz + 4; z++) {
					if (x != ox && x != ox + 4 && z != oz && z != oz + 4) {
						continue;
					}

					for (int y = FLOOR + 1; y <= FLOOR + 3; y++) {
						addBlock(world, Blocks.IRON_BARS.getDefaultState(), x, y, z, box);
					}
				}
			}

			int doorX = ox < 7 ? ox + 4 : ox;

			for (int y = FLOOR + 1; y <= FLOOR + 2; y++) {
				addBlock(world, AIR, doorX, y, oz + 2, box);
			}

			if (index % 2 == 0) {
				addChest(world, box, random, ox + 2, FLOOR + 1, oz + 2, FDLootTables.BARRACKS);
			} else {
				addBlock(world, FDBlocks.SOUL_SPIKES.getDefaultState(), ox + 2, FLOOR + 1, oz + 2, box);
				addBlock(world, Blocks.BONE_BLOCK.getDefaultState(), ox + 1, FLOOR + 1, oz + 3, box);
			}
		}

		addBlock(world, Blocks.GRINDSTONE.getDefaultState(), 7, FLOOR + 1, 7, box);
		addBlock(world, FDBlocks.RUNE_TRAP.getDefaultState(), 7, FLOOR + 1, 5, box);
		addBlock(world, FDBlocks.RUNE_TRAP.getDefaultState(), 7, FLOOR + 1, 9, box);

		hangChains(world, box, random, 4);
		spawnMob(world, box, FDEntities.ASHEN_SMITH, 7, FLOOR + 1, 3);
		spawnMob(world, box, FDEntities.EMBER_WISP, 7, FLOOR + 3, 11);
	}

	private void decorateTrialHall(StructureWorldAccess world, BlockBox box, Random random) {
		for (int x = 6; x <= 8; x++) {
			for (int z = 6; z <= 8; z++) {
				addBlock(world, Blocks.POLISHED_DEEPSLATE.getDefaultState(), x, FLOOR, z, box);
			}
		}

		addBlock(world, FDBlocks.TRIAL_ALTAR.getDefaultState(), 7, FLOOR + 1, 7, box);

		for (int x = 1; x < MAX; x++) {
			for (int z = 1; z < MAX; z++) {
				if (Math.max(Math.abs(x - 7), Math.abs(z - 7)) == 4) {
					addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), x, FLOOR, z, box);
				}
			}
		}

		addChest(world, box, random, 2, FLOOR + 1, 2, FDLootTables.TRIAL);

		for (int y = FLOOR + 1; y <= FLOOR + 2; y++) {
			addBlock(world, FDBlocks.SEALED_GATE.getDefaultState(), 3, y, 2, box);
			addBlock(world, FDBlocks.SEALED_GATE.getDefaultState(), 2, y, 3, box);
		}

		for (int[] corner : new int[][]{{3, 3}, {3, MAX - 3}, {MAX - 3, 3}, {MAX - 3, MAX - 3}}) {
			addBlock(world, FDBlocks.EMBER_LANTERN.getDefaultState(), corner[0], CEILING - 1, corner[1], box);
		}

		hangChains(world, box, random, 4);
	}

	private void decorateFloodedForge(StructureWorldAccess world, BlockBox box, Random random) {
		for (int x = 2; x <= MAX - 2; x++) {
			for (int z = 2; z <= MAX - 2; z++) {
				addBlock(world, FDBlocks.SCORCHED_DEEPSLATE.getDefaultState(), x, FLOOR - 1, z, box);
				addBlock(world, Blocks.WATER.getDefaultState(), x, FLOOR, z, box);
			}
		}

		for (int i = 0; i < 6; i++) {
			addBlock(world, Blocks.MAGMA_BLOCK.getDefaultState(),
					3 + random.nextInt(MAX - 5), FLOOR - 1, 3 + random.nextInt(MAX - 5), box);
		}

		for (int i = 0; i < 12; i++) {
			BlockState crust = random.nextBoolean()
					? Blocks.OBSIDIAN.getDefaultState()
					: Blocks.BASALT.getDefaultState();
			addBlock(world, crust, 2 + random.nextInt(MAX - 3), FLOOR, 2 + random.nextInt(MAX - 3), box);
		}

		for (int x = 2; x <= 4; x++) {
			for (int z = 2; z <= 4; z++) {
				addBlock(world, FDBlocks.SCORCHED_DEEPSLATE_BRICKS.getDefaultState(), x, FLOOR, z, box);
			}
		}

		addChest(world, box, random, 3, FLOOR + 1, 3, FDLootTables.FLOODED);
		addBlock(world, FDBlocks.EMBER_LANTERN.getDefaultState(), 2, FLOOR + 1, 2, box);

		for (int x = 6; x <= 8; x += 2) {
			addBlock(world, FDBlocks.MAGMA_VENT.getDefaultState(), x, FLOOR, 1, box);
		}

		hangChains(world, box, random, 5);
		spawnMob(world, box, FDEntities.EMBER_WISP, 11, FLOOR + 3, 11);
	}

	private void decorateAncestorGallery(StructureWorldAccess world, BlockBox box, Random random) {
		int[] columns = {3, 5, 9, 11};
		int mimic = columns[random.nextInt(columns.length)];

		for (int z : new int[]{2, MAX - 2}) {
			for (int x : columns) {
				if (x == mimic && z == MAX - 2) {
					continue;
				}

				addBlock(world, FDBlocks.SCORCHED_DEEPSLATE_BRICKS.getDefaultState(), x, FLOOR + 1, z, box);
				addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), x, FLOOR + 2, z, box);
				addBlock(world, Blocks.CHISELED_DEEPSLATE.getDefaultState(), x, FLOOR + 3, z, box);
			}
		}

		for (int z = 1; z < MAX; z++) {
			addBlock(world, FDBlocks.CINDERFORGED_COPPER.getDefaultState(), 7, FLOOR, z, box);
		}

		addBlock(world, Blocks.CHISELED_BOOKSHELF.getDefaultState(), 7, FLOOR + 1, 1, box);
		addChest(world, box, random, 7, FLOOR + 1, MAX - 1, FDLootTables.ARCHIVE);

		for (int i = 0; i < 6; i++) {
			addBlock(world, Blocks.COBWEB.getDefaultState(),
					2 + random.nextInt(MAX - 3), FLOOR + 1 + random.nextInt(4), 2 + random.nextInt(MAX - 3), box);
		}

		addBlock(world, FDBlocks.RUNE_TRAP.getDefaultState(), 7, FLOOR + 1, 5, box);
		hangChains(world, box, random, 5);

		spawnMob(world, box, FDEntities.ASHEN_SMITH, mimic, FLOOR + 1, MAX - 2);
	}

	private void hangChains(StructureWorldAccess world, BlockBox box, Random random, int count) {
		for (int i = 0; i < count; i++) {
			int x = 2 + random.nextInt(MAX - 3);
			int z = 2 + random.nextInt(MAX - 3);
			int length = 2 + random.nextInt(4);

			for (int y = 0; y < length; y++) {
				addBlock(world, Blocks.IRON_CHAIN.getDefaultState().with(ChainBlock.AXIS, Direction.Axis.Y),
						x, CEILING - 1 - y, z, box);
			}

			if (random.nextBoolean()) {
				addBlock(world, Blocks.LANTERN.getDefaultState().with(Properties.HANGING, true),
						x, CEILING - 1 - length, z, box);
			}
		}
	}

	private void spawnMob(StructureWorldAccess world, BlockBox box, EntityType<? extends MobEntity> type,
			int x, int y, int z) {
		BlockPos pos = worldPos(x, y, z);

		if (!box.contains(pos)) {
			return;
		}

		MobEntity mob = type.create(world.toServerWorld(), SpawnReason.STRUCTURE);

		if (mob == null) {
			return;
		}

		mob.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
		mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.STRUCTURE, null);
		mob.setPersistent();
		world.spawnEntity(mob);
	}

	private static BlockState bridgeState(Random random) {
		return random.nextInt(4) == 0
				? FDBlocks.CRUMBLING_STONE.getDefaultState()
				: Blocks.POLISHED_DEEPSLATE.getDefaultState();
	}

	private static BlockState wallState(Random random) {
		int roll = random.nextInt(10);

		if (roll < 6) {
			return FDBlocks.SCORCHED_DEEPSLATE_BRICKS.getDefaultState();
		}

		if (roll < 8) {
			return FDBlocks.CRACKED_SCORCHED_DEEPSLATE_BRICKS.getDefaultState();
		}

		return FDBlocks.SCORCHED_DEEPSLATE.getDefaultState();
	}

	private static BlockState floorState(Random random) {
		int roll = random.nextInt(10);

		if (roll < 5) {
			return FDBlocks.SCORCHED_DEEPSLATE.getDefaultState();
		}

		if (roll < 8) {
			return FDBlocks.SCORCHED_DEEPSLATE_BRICKS.getDefaultState();
		}

		return FDBlocks.SLAG_BLOCK.getDefaultState();
	}
}
