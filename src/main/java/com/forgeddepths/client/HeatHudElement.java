package com.forgeddepths.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public final class HeatHudElement {
	private HeatHudElement() {
	}

	private static final float CYCLE_SECONDS = 10.0F;
	private static final long STALE_MS = 3000L;

	private static final int GAUGE_X = 14;
	private static final int GAUGE_WIDTH = 9;
	private static final int GAUGE_HEIGHT = 74;
	private static final int BULB_RADIUS = 7;

	private static final int COLOR_FRAME = 0xFF1B1310;
	private static final int COLOR_TRACK = 0xFF2E1C13;
	private static final int COLOR_EDGE = 0xFF7A4A22;

	private static boolean inside;
	private static float protection;
	private static int secondsLeft;
	private static long lastUpdate;

	private static float displayedUrgency;
	private static float flash;
	private static int previousSeconds;

	public static void update(boolean nowInside, float nowProtection, int nowSecondsLeft) {
		if (inside && nowInside && nowSecondsLeft > previousSeconds) {
			flash = 1.0F;
		}

		inside = nowInside;
		protection = nowProtection;
		secondsLeft = nowSecondsLeft;
		previousSeconds = nowSecondsLeft;
		lastUpdate = System.currentTimeMillis();
	}

	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();

		if (!inside || client.player == null || client.options.hudHidden) {
			return;
		}

		if (System.currentTimeMillis() - lastUpdate > STALE_MS) {
			inside = false;
			return;
		}

		float delta = tickCounter.getDynamicDeltaTicks();
		float target = 1.0F - MathHelper.clamp(secondsLeft / CYCLE_SECONDS, 0.0F, 1.0F);
		displayedUrgency = MathHelper.lerp(Math.min(1.0F, delta * 0.25F), displayedUrgency, target);
		flash = Math.max(0.0F, flash - delta * 0.06F);

		int width = context.getScaledWindowWidth();
		int height = context.getScaledWindowHeight();

		drawVignette(context, width, height);
		drawGauge(context, client, height);
	}

	private static void drawVignette(DrawContext context, int width, int height) {
		int depth = 6 + Math.round(10 * displayedUrgency) + Math.round(6 * flash);
		int peak = 70 + Math.round(80 * displayedUrgency) + Math.round(90 * flash);

		for (int i = 0; i < depth; i++) {
			float falloff = 1.0F - (float) i / depth;
			int alpha = Math.round(peak * falloff * falloff);

			if (alpha <= 0) {
				continue;
			}

			int color = (alpha << 24) | tint(displayedUrgency);

			context.fill(i, i, width - i, i + 1, color);
			context.fill(i, height - i - 1, width - i, height - i, color);
			context.fill(i, i + 1, i + 1, height - i - 1, color);
			context.fill(width - i - 1, i + 1, width - i, height - i - 1, color);
		}
	}

	private static int tint(float urgency) {
		int red = 0xFF;
		int green = Math.round(MathHelper.lerp(urgency, 0x86, 0x22));
		int blue = Math.round(MathHelper.lerp(urgency, 0x30, 0x10));
		return (red << 16) | (green << 8) | blue;
	}

	private static void drawGauge(DrawContext context, MinecraftClient client, int screenHeight) {
		int bottom = screenHeight / 2 + GAUGE_HEIGHT / 2;
		int top = bottom - GAUGE_HEIGHT;
		int left = GAUGE_X;
		int right = left + GAUGE_WIDTH;

		context.fill(left - 2, top - 1, right + 2, bottom + 2, COLOR_FRAME);
		context.fill(left - 3, top + 1, right + 3, bottom, COLOR_FRAME);
		context.fill(left - 1, top, right + 1, bottom + 1, COLOR_EDGE);
		context.fill(left, top + 1, right, bottom, COLOR_TRACK);

		int fillHeight = Math.round((bottom - top - 2) * displayedUrgency);

		for (int i = 0; i < fillHeight; i++) {
			int y = bottom - 1 - i;
			float position = (float) i / Math.max(1, bottom - top - 2);
			context.fill(left, y, right, y + 1, 0xFF000000 | gradient(position));
		}

		if (fillHeight > 2) {
			context.fill(left, bottom - fillHeight, left + 1, bottom - 1, 0x66FFFFFF);
		}

		drawBulb(context, (left + right) / 2, bottom + 6);
		drawTicks(context, left, right, top, bottom);

		Text seconds = Text.literal(secondsLeft + "s");
		Text shield = Text.literal(Math.round(protection * 100.0F) + "%");

		context.drawCenteredTextWithShadow(client.textRenderer,
				Text.translatable("hud.forged_depths.heat_short"), (left + right) / 2, top - 14, 0xFFFFC46B);
		context.drawCenteredTextWithShadow(client.textRenderer, seconds, (left + right) / 2, top - 24,
				0xFFFF7A3C);
		context.drawCenteredTextWithShadow(client.textRenderer, shield, (left + right) / 2, bottom + 16,
				0xFF9FD8FF);
	}

	private static void drawBulb(DrawContext context, int cx, int cy) {
		for (int dy = -BULB_RADIUS; dy <= BULB_RADIUS; dy++) {
			int half = (int) Math.sqrt(Math.max(0, BULB_RADIUS * BULB_RADIUS - dy * dy));

			if (half == 0) {
				continue;
			}

			int inner = Math.max(0, half - 2);
			context.fill(cx - half, cy + dy, cx + half, cy + dy + 1, COLOR_EDGE);

			if (inner > 0) {
				float heat = 0.45F + 0.55F * displayedUrgency;
				context.fill(cx - inner, cy + dy, cx + inner, cy + dy + 1,
						0xFF000000 | gradient(heat));
			}
		}
	}

	private static void drawTicks(DrawContext context, int left, int right, int top, int bottom) {
		for (int i = 1; i <= 3; i++) {
			int y = bottom - (bottom - top) * i / 4;
			context.fill(right + 1, y, right + 4, y + 1, 0xAA9A6A3A);
		}
	}

	private static int gradient(float position) {
		float clamped = MathHelper.clamp(position, 0.0F, 1.0F);
		int red = Math.round(MathHelper.lerp(clamped, 0xE0, 0xFF));
		int green = Math.round(MathHelper.lerp(clamped, 0x52, 0xE8));
		int blue = Math.round(MathHelper.lerp(clamped, 0x14, 0xA0));
		return (red << 16) | (green << 8) | blue;
	}
}
