package com.godweapons.item;

import com.godweapons.effect.BlackHole;
import com.godweapons.effect.Effects;
import com.godweapons.effect.Util;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class VoidReaperItem extends Item {
	public static final float CHANCE = 0.25f;
	public static final int COOLDOWN_TICKS = 100; // 5 seconds

	public VoidReaperItem(Item.Settings settings) {
		super(settings);
	}

	/** Called from HitEvents after a melee hit made while holding this item. */
	public static void onHit(ServerWorld world, ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!(attacker instanceof PlayerEntity player)) return;
		var cd = player.getItemCooldownManager();
		if (!cd.isCoolingDown(stack) && world.random.nextFloat() < CHANCE) {
			cd.set(stack, COOLDOWN_TICKS);
			Vec3d center = Util.pos(target).add(0, target.getHeight() * 0.5, 0);
			Effects.add(new BlackHole(world, center, player));
		}
	}
}
