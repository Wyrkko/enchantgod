package com.godweapons.effect;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class Util {
	private Util() {}

	/** false = only hostile mobs are hit by the special abilities. true = every mob (except players and pets). */
	public static boolean HIT_ALL_MOBS = false;

	public static boolean isEnemy(Entity user, LivingEntity e) {
		if (e == user || !e.isAlive() || e.isSpectator()) return false;
		if (e instanceof PlayerEntity || e instanceof ArmorStandEntity) return false;
		if (e instanceof Monster) return true;
		if (!HIT_ALL_MOBS) return false;
		return !(e instanceof TameableEntity t && t.isTamed());
	}

	public static Vec3d pos(Entity e) {
		return new Vec3d(e.getX(), e.getY(), e.getZ());
	}

	/** The entity's world as a ServerWorld, or null on the client. */
	public static ServerWorld world(Entity e) {
		return e.getEntityWorld() instanceof ServerWorld sw ? sw : null;
	}

	public static List<LivingEntity> enemiesAround(ServerWorld world, Vec3d c, double r, Entity user) {
		Box box = new Box(c.x - r, c.y - r, c.z - r, c.x + r, c.y + r, c.z + r);
		return world.getEntitiesByClass(LivingEntity.class, box,
			e -> isEnemy(user, e) && e.squaredDistanceTo(c) <= r * r);
	}

	/** Magic damage ignores armor and still credits the player with the kill. */
	public static DamageSource magic(ServerWorld world, Entity user) {
		if (user == null || user.isRemoved()) return world.getDamageSources().generic();
		return world.getDamageSources().indirectMagic(user, user);
	}

	public static void cosmeticBolt(ServerWorld world, double x, double y, double z) {
		LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
		if (bolt == null) return;
		bolt.refreshPositionAfterTeleport(x, y, z);
		bolt.setCosmetic(true); // visual + sound only, we deal the damage ourselves
		world.spawnEntity(bolt);
	}

	public static void hurt(ServerWorld world, LivingEntity e, DamageSource src, float amount) {
		e.timeUntilRegen = 0; // bypass invulnerability frames
		e.damage(world, src, amount);
	}

	// Two overloads so this compiles whether a sound constant is a SoundEvent or a RegistryEntry<SoundEvent>.
	public static void sound(ServerWorld world, double x, double y, double z, SoundEvent s, float vol, float pitch) {
		world.playSound(null, x, y, z, s, SoundCategory.PLAYERS, vol, pitch);
	}

	public static void sound(ServerWorld world, double x, double y, double z, RegistryEntry<SoundEvent> s, float vol, float pitch) {
		world.playSound(null, x, y, z, s.value(), SoundCategory.PLAYERS, vol, pitch);
	}
}
