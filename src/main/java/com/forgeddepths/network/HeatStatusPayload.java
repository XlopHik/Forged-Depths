package com.forgeddepths.network;

import com.forgeddepths.ForgedDepths;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record HeatStatusPayload(boolean inside, float protection, int secondsLeft) implements CustomPayload {
	public static final CustomPayload.Id<HeatStatusPayload> ID =
			new CustomPayload.Id<>(ForgedDepths.id("heat_status"));

	public static final PacketCodec<RegistryByteBuf, HeatStatusPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.BOOLEAN, HeatStatusPayload::inside,
			PacketCodecs.FLOAT, HeatStatusPayload::protection,
			PacketCodecs.VAR_INT, HeatStatusPayload::secondsLeft,
			HeatStatusPayload::new);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
