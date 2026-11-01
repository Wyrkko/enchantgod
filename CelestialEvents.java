package com.godweapons.effect;

import com.godweapons.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.Heightmap;

public final class CelestialEvents {
	private CelestialEvents() {}

	public static final int SHOWER_EVERY = 10;
	public static final int SHOWER_METEORS = 30;
	public static final double SHOWER_RADIUS = 18.0;

	private static final Map<UUID, Integer> KILLS = new HashMap<>();

	public static void register() {
		ServerLivingEntityEvents.AFTER_DEATH.register((killed, damageSource) -> {
			if (Meteor.guard) return;
			if (!(damageSource.getAttacker() instanceof ServerPlayerEntity player)) return;
			if (killed instanceof PlayerEntity) return;
			if (!player.getMainHandStack().isOf(ModItems.CELESTIAL_JUDGEMENT)) return;

			ServerWorld world = Util.world(killed);
			if (world == null) return;

			int kills = KILLS.merge(player.getUuid(), 1, Integer::sum);

			// every kill = one meteor on the victim
			Effects.add(new Meteor(world, killed.getX(), killed.getY(), killed.getZ(), player));

			if (kills % SHOWER_EVERY == 0) {
				meteorShower(world, player);
			} else {
				player.sendMessage(Text.literal("Kills: " + (kills % SHOWER_EVERY) + "/" + SHOWER_EVERY)
					.formatted(Formatting.GOLD), true);
			}
		});
	}

	private static void meteorShower(ServerWorld world, ServerPlayerEntity player) {
		player.sendMessage(Text.literal("METEOR SHOWER!").formatted(Formatting.RED, Formatting.BOLD), true);
		for (int i = 0; i < SHOWER_METEORS; i++) {
			int delay = 5 + i * 4 + world.random.nextInt(4);
			Effects.later(delay, () -> {
				if (player.isRemoved() || Util.world(player) != world) return;
				double mx = player.getX() + (world.random.nextDouble() * 2 - 1) * SHOWER_RADIUS;
				double mz = player.getZ() + (world.random.nextDouble() * 2 - 1) * SHOWER_RADIUS;
				double gy = world.getTopY(Heightmap.Type.MOTION_BLOCKING, (int) Math.floor(mx), (int) Math.floor(mz));
				if (gy - player.getY() > 6) gy = player.getY(); // under a roof/cave: land at player height
				Effects.add(new Meteor(world, mx, gy, mz, player));
			});
		}
	}
}
