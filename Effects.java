package com.godweapons.effect;

import com.godweapons.GodWeaponsMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

/** Tiny tick scheduler used by black holes, meteors and delayed tasks. */
public final class Effects {
	private Effects() {}

	public interface Task {
		/** @return true when finished and should be removed */
		boolean tick(MinecraftServer server);
	}

	private static final List<Task> ACTIVE = new ArrayList<>();
	private static final List<Task> PENDING = new ArrayList<>();

	public static void add(Task t) {
		PENDING.add(t);
	}

	public static void later(int ticks, Runnable r) {
		add(new Task() {
			int left = ticks;
			@Override
			public boolean tick(MinecraftServer server) {
				if (--left > 0) return false;
				r.run();
				return true;
			}
		});
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(Effects::tickAll);
		ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
			ACTIVE.clear();
			PENDING.clear();
		});
	}

	private static void tickAll(MinecraftServer server) {
		ACTIVE.addAll(PENDING);
		PENDING.clear();
		ACTIVE.removeIf(t -> {
			try {
				return t.tick(server);
			} catch (Exception ex) {
				GodWeaponsMod.LOGGER.error("Effect task crashed, removing it", ex);
				return true;
			}
		});
	}
}
