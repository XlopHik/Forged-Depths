package com.forgeddepths.world;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum RoomType {
	ENTRANCE("entrance"),
	FORGE_HALL("forge_hall"),
	TREASURY("treasury"),

	TRAP_CORRIDOR("trap_corridor"),
	BARRACKS("barracks"),
	CRUCIBLE("crucible"),
	ARCHIVE("archive"),
	SMELTERY("smeltery"),
	COLLAPSED_HALL("collapsed_hall"),
	CELLS("cells"),
	TRIAL_HALL("trial_hall"),
	FLOODED_FORGE("flooded_forge"),
	ANCESTOR_GALLERY("ancestor_gallery"),

	CORRIDOR_X("corridor_x"),
	CORRIDOR_Z("corridor_z");

	public static final List<RoomType> MANDATORY = List.of(FORGE_HALL, TREASURY);

	public static final List<RoomType> OPTIONAL = List.of(
			TRAP_CORRIDOR, BARRACKS, CRUCIBLE, ARCHIVE, SMELTERY, COLLAPSED_HALL, CELLS,
			TRIAL_HALL, FLOODED_FORGE, ANCESTOR_GALLERY);

	private static final Map<String, RoomType> BY_NAME = new HashMap<>();

	static {
		for (RoomType type : values()) {
			BY_NAME.put(type.name, type);
		}
	}

	private final String name;

	RoomType(String name) {
		this.name = name;
	}

	public String asString() {
		return name;
	}

	public static RoomType byName(String name) {
		return BY_NAME.getOrDefault(name, ENTRANCE);
	}

	public boolean isCorridor() {
		return this == CORRIDOR_X || this == CORRIDOR_Z;
	}
}
