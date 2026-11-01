package com.godweapons.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class BlackHole implements Effects.Task {
	public static final double PULL_RADIUS = 9.0;
	public static final double KILL_RADIUS = 4.0;
	public static final int DURATION_TICKS = 50;
	public static final float DAMAGE = 16.0f; // 8 hearts

	private final ServerWorld world;
	private final Vec3d center;
	private final PlayerEntity owner;
	private int age = 0;

	public BlackHole(ServerWorld world, Vec3d center, PlayerEntity owner) {
		this.world = world;
		this.center = center;
		this.owner = owner;
		Util.sound(world, center.x, center.y, center.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.8f);
	}

	@Override
	public boolean tick(MinecraftServer server) {
		age++;

		// visuals: dark core and swirling portal particles
		world.spawnParticles(ParticleTypes.SQUID_INK, center.x, center.y, center.z, 8, 0.25, 0.25, 0.25, 0.02);
		world.spawnParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 50, 2.5, 2.5, 2.5, 0.3);
		world.spawnParticles(ParticleTypes.PORTAL, center.x, center.y, center.z, 40, 4.0, 4.0, 4.0, 0.8);

		// pull
		for (LivingEntity e : Util.enemiesAround(world, center, PULL_RADIUS, owner)) {
			Vec3d mid = Util.pos(e).add(0, e.getHeight() * 0.5, 0);
			Vec3d to = center.subtract(mid);
			double d = to.length();
			if (d < 0.6) {
				e.setVelocity(Vec3d.ZERO);
			} else {
				e.setVelocity(to.normalize().multiply(Math.min(0.9, 0.2 + d * 0.08)));
			}
			e.velocityModified = true;
			e.fallDistance = 0;
		}

		if (age >= DURATION_TICKS) {
			collapse();
			return true;
		}
		return false;
	}

	private void collapse() {
		for (LivingEntity e : Util.enemiesAround(world, center, KILL_RADIUS, owner)) {
			Util.hurt(world, e, Util.magic(world, owner), DAMAGE);
		}
		world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 1, 0, 0, 0, 0);
		world.spawnParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 200, 0.5, 0.5, 0.5, 1.2);
		Util.sound(world, center.x, center.y, center.z, SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.6f);
	}
}
