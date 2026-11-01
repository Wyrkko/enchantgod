package com.godweapons.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class Meteor implements Effects.Task {
	public static final float DAMAGE = 30.0f; // 15 hearts
	public static final double RADIUS = 5.5;
	public static final double SPEED = 2.2;

	/** Prevents meteor kills from spawning more meteors forever. */
	public static boolean guard = false;

	private final ServerWorld world;
	private final double x, z, groundY;
	private final PlayerEntity owner;
	private double y;

	public Meteor(ServerWorld world, double x, double groundY, double z, PlayerEntity owner) {
		this.world = world;
		this.x = x;
		this.z = z;
		this.groundY = groundY;
		this.owner = owner;
		double ceiling = world.getBottomY() + world.getHeight() - 2;
		double start = Math.min(groundY + 45, ceiling);
		this.y = Math.max(start, groundY + 6);
		Util.sound(world, x, y, z, SoundEvents.ITEM_FIRECHARGE_USE, 3.0f, 0.5f);
	}

	@Override
	public boolean tick(MinecraftServer server) {
		y -= SPEED;
		world.spawnParticles(ParticleTypes.FLAME, x, y, z, 20, 0.5, 0.5, 0.5, 0.05);
		world.spawnParticles(ParticleTypes.LAVA, x, y, z, 5, 0.5, 0.5, 0.5, 0.0);
		world.spawnParticles(ParticleTypes.LARGE_SMOKE, x, y + 1.5, z, 10, 0.6, 1.0, 0.6, 0.02);
		if (y <= groundY) {
			impact();
			return true;
		}
		return false;
	}

	private void impact() {
		Vec3d c = new Vec3d(x, groundY + 0.5, z);
		world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y, c.z, 2, 1.0, 0.2, 1.0, 0);
		world.spawnParticles(ParticleTypes.FLAME, c.x, c.y, c.z, 120, 3.0, 0.5, 3.0, 0.3);
		world.spawnParticles(ParticleTypes.LAVA, c.x, c.y, c.z, 40, 2.5, 0.5, 2.5, 0.0);
		Util.sound(world, c.x, c.y, c.z, SoundEvents.ENTITY_GENERIC_EXPLODE, 4.0f, 0.7f);
		Util.sound(world, c.x, c.y, c.z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 4.0f, 0.8f);

		guard = true;
		try {
			for (LivingEntity e : Util.enemiesAround(world, c, RADIUS, owner)) {
				Util.hurt(world, e, Util.magic(world, owner), DAMAGE);
				if (e.isAlive()) {
					e.setOnFireFor(5);
					Vec3d away = Util.pos(e).subtract(c);
					Vec3d flat = new Vec3d(away.x, 0, away.z);
					flat = flat.lengthSquared() < 1.0E-4 ? Vec3d.ZERO : flat.normalize();
					e.addVelocity(flat.x * 0.9, 1.5, flat.z * 0.9); // launched into the air
					e.velocityModified = true;
				}
			}
		} finally {
			guard = false;
		}
	}
}
