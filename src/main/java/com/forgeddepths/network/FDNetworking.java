package com.forgeddepths.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FDNetworking {
	private FDNetworking() {
	}

	public static void registerCommon() {
		PayloadTypeRegistry.playS2C().register(HeatStatusPayload.ID, HeatStatusPayload.CODEC);
	}

	public static void sendHeatStatus(ServerPlayerEntity player, boolean inside, float protection, int secondsLeft) {
		if (ServerPlayNetworking.canSend(player, HeatStatusPayload.ID)) {
			ServerPlayNetworking.send(player, new HeatStatusPayload(inside, protection, secondsLeft));
		}
	}
}
