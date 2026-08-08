package com.forgeddepths.world;

import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ForgedDepthsGenerator {
	private ForgedDepthsGenerator() {
	}

	public static final int ROOM_SIZE = 15;
	public static final int CELL_PITCH = 19;
	public static final int SUBFLOOR = 3;
	public static final int ROOM_TOP = 9;
	public static final int SHAFT_HEIGHT = 34;
	public static final int HALL_VAULT_HEIGHT = 6;

	private static final int MIN_OPTIONAL_ROOMS = 3;
	private static final int MAX_EXTRA_OPTIONAL_ROOMS = 4;

	private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

	public static void generate(StructurePiecesCollector collector, BlockPos origin, Random random) {
		Layout layout = layout(random);
		Map<Long, Direction> guides = buildGuides(layout);

		for (int i = 0; i < layout.cells.size(); i++) {
			int[] cell = layout.cells.get(i);
			RoomType type = layout.types.get(i);
			collector.addPiece(new ForgedDepthsPiece(type, roomBox(origin, cell, type),
					guides.get(key(cell[0], cell[1]))));
		}

		for (int[] link : layout.links) {
			collector.addPiece(corridor(origin, link, layout));
		}
	}

	private static final class Layout {
		final List<int[]> cells = new ArrayList<>();
		final List<RoomType> types = new ArrayList<>();
		final List<int[]> links = new ArrayList<>();
		Map<Long, Integer> distance = Map.of();

		void place(Set<Long> occupied, int cx, int cz, RoomType type) {
			occupied.add(key(cx, cz));
			cells.add(new int[]{cx, cz});
			types.add(type);
		}

		int[] cellOf(RoomType type) {
			int index = types.indexOf(type);
			return index < 0 ? null : cells.get(index);
		}
	}

	private static Layout layout(Random random) {
		Layout layout = new Layout();
		Set<Long> occupied = new HashSet<>();

		layout.place(occupied, 0, 0, RoomType.ENTRANCE);
		int[] current = {0, 0};

		for (RoomType type : pickRooms(random)) {
			int[] next = freeNeighbour(current, occupied, random);

			if (next == null) {
				for (int[] candidate : shuffledCopy(layout.cells, random)) {
					next = freeNeighbour(candidate, occupied, random);

					if (next != null) {
						current = candidate;
						break;
					}
				}
			}

			if (next == null) {
				break;
			}

			layout.place(occupied, next[0], next[1], type);
			layout.links.add(new int[]{current[0], current[1], next[0], next[1]});
			current = next;
		}

		addLoops(layout, random);
		return layout;
	}

	private static List<RoomType> pickRooms(Random random) {
		List<RoomType> optional = new ArrayList<>(RoomType.OPTIONAL);
		shuffle(optional, random);

		List<RoomType> plan = new ArrayList<>(RoomType.MANDATORY);
		plan.addAll(optional.subList(0, MIN_OPTIONAL_ROOMS + random.nextInt(MAX_EXTRA_OPTIONAL_ROOMS)));
		shuffle(plan, random);

		return plan;
	}

	private static void addLoops(Layout layout, Random random) {
		int wanted = 1 + random.nextInt(2);

		for (int[] a : shuffledCopy(layout.cells, random)) {
			if (wanted <= 0) {
				break;
			}

			for (int[] b : layout.cells) {
				if (Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]) != 1 || isLinked(layout.links, a, b)) {
					continue;
				}

				layout.links.add(new int[]{a[0], a[1], b[0], b[1]});
				wanted--;
				break;
			}
		}
	}

	private static Map<Long, Direction> buildGuides(Layout layout) {
		Map<Long, Direction> guides = new HashMap<>();
		int[] hall = layout.cellOf(RoomType.FORGE_HALL);

		if (hall == null) {
			return guides;
		}

		Map<Long, List<int[]>> adjacency = new HashMap<>();

		for (int[] link : layout.links) {
			adjacency.computeIfAbsent(key(link[0], link[1]), k -> new ArrayList<>())
					.add(new int[]{link[2], link[3]});
			adjacency.computeIfAbsent(key(link[2], link[3]), k -> new ArrayList<>())
					.add(new int[]{link[0], link[1]});
		}

		Map<Long, Integer> distance = new HashMap<>();
		distance.put(key(hall[0], hall[1]), 0);

		Deque<int[]> queue = new ArrayDeque<>();
		queue.add(hall);

		while (!queue.isEmpty()) {
			int[] cell = queue.poll();
			int step = distance.get(key(cell[0], cell[1]));

			for (int[] neighbour : adjacency.getOrDefault(key(cell[0], cell[1]), List.of())) {
				long id = key(neighbour[0], neighbour[1]);

				if (distance.containsKey(id)) {
					continue;
				}

				distance.put(id, step + 1);

				guides.put(id, towards(neighbour, cell));
				queue.add(neighbour);
			}
		}

		layout.distance = distance;
		return guides;
	}

	private static Direction towards(int[] from, int[] to) {
		if (to[0] > from[0]) {
			return Direction.EAST;
		}

		if (to[0] < from[0]) {
			return Direction.WEST;
		}

		return to[1] > from[1] ? Direction.SOUTH : Direction.NORTH;
	}

	private static BlockBox roomBox(BlockPos origin, int[] cell, RoomType type) {
		int x = origin.getX() + cell[0] * CELL_PITCH;
		int z = origin.getZ() + cell[1] * CELL_PITCH;
		int top = origin.getY() + ROOM_TOP + extraHeight(type);

		return new BlockBox(x, origin.getY() - SUBFLOOR, z, x + ROOM_SIZE - 1, top, z + ROOM_SIZE - 1);
	}

	private static int extraHeight(RoomType type) {
		return switch (type) {
			case ENTRANCE -> SHAFT_HEIGHT;
			case FORGE_HALL -> HALL_VAULT_HEIGHT;
			default -> 0;
		};
	}

	private static ForgedDepthsPiece corridor(BlockPos origin, int[] link, Layout layout) {
		int ax = link[0];
		int az = link[1];
		int bx = link[2];
		int bz = link[3];

		int ox = origin.getX() + Math.min(ax, bx) * CELL_PITCH;
		int oz = origin.getZ() + Math.min(az, bz) * CELL_PITCH;
		int minY = origin.getY() - SUBFLOOR;
		int maxY = origin.getY() + 5;

		int distA = layout.distance.getOrDefault(key(ax, az), Integer.MAX_VALUE);
		int distB = layout.distance.getOrDefault(key(bx, bz), Integer.MAX_VALUE);
		Direction guide = distA <= distB
				? towards(new int[]{bx, bz}, new int[]{ax, az})
				: towards(new int[]{ax, az}, new int[]{bx, bz});

		if (ax != bx) {
			return new ForgedDepthsPiece(RoomType.CORRIDOR_X,
					new BlockBox(ox + ROOM_SIZE - 1, minY, oz + 5, ox + CELL_PITCH, maxY, oz + 9), guide);
		}

		return new ForgedDepthsPiece(RoomType.CORRIDOR_Z,
				new BlockBox(ox + 5, minY, oz + ROOM_SIZE - 1, ox + 9, maxY, oz + CELL_PITCH), guide);
	}

	private static boolean isLinked(List<int[]> links, int[] a, int[] b) {
		for (int[] link : links) {
			boolean forward = link[0] == a[0] && link[1] == a[1] && link[2] == b[0] && link[3] == b[1];
			boolean backward = link[0] == b[0] && link[1] == b[1] && link[2] == a[0] && link[3] == a[1];

			if (forward || backward) {
				return true;
			}
		}

		return false;
	}

	private static int[] freeNeighbour(int[] cell, Set<Long> occupied, Random random) {
		int[] order = {0, 1, 2, 3};

		for (int i = order.length - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			int tmp = order[i];
			order[i] = order[j];
			order[j] = tmp;
		}

		for (int index : order) {
			int nx = cell[0] + DIRECTIONS[index][0];
			int nz = cell[1] + DIRECTIONS[index][1];

			if (!occupied.contains(key(nx, nz))) {
				return new int[]{nx, nz};
			}
		}

		return null;
	}

	private static long key(int x, int z) {
		return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
	}

	private static <T> void shuffle(List<T> list, Random random) {
		for (int i = list.size() - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			T tmp = list.get(i);
			list.set(i, list.get(j));
			list.set(j, tmp);
		}
	}

	private static List<int[]> shuffledCopy(List<int[]> list, Random random) {
		List<int[]> copy = new ArrayList<>(list);
		shuffle(copy, random);
		return copy;
	}
}
