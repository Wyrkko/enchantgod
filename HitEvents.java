package com.godweapons.effect;

import com.godweapons.ModItems;
import com.godweapons.item.GodslayerItem;
import com.godweapons.item.VoidReaperItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

/**
 * Melee-hit hooks for Void Reaper and Godslayer.
 * (Since 1.21.2 the old Item#postHit override no longer fits, so we listen to the damage event instead.)
 */
public final class HitEvents {
	private HitEvents() {}

	/** Stops our own bonus damage from triggering the hook again. */
	private static boolean busy = false;

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((target, source, baseDamageTaken, damageTaken, blocked) -> {
			if (busy || blocked) return;
			if (!(source.isOf(DamageTypes.PLAYER_ATTACK) || source.isOf(DamageTypes.MOB_ATTACK))) return;
			if (!(source.getAttacker() instanceof LivingEntity attacker)) return;

			ServerWorld world = Util.world(attacker);
			if (world == null) return;

			ItemStack held = attacker.getMainHandStack();
			busy = true;
			try {
				if (held.isOf(ModItems.GODSLAYER)) {
					GodslayerItem.onHit(world, target, attacker);
				} else if (held.isOf(ModItems.VOID_REAPER)) {
					VoidReaperItem.onHit(world, held, target, attacker);
				}
			} finally {
				busy = false;
			}
		});
	}
}
