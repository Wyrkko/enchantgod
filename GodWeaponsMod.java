package com.godweapons;

import com.godweapons.effect.CelestialEvents;
import com.godweapons.effect.Effects;
import com.godweapons.effect.HitEvents;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GodWeaponsMod implements ModInitializer {
	public static final String MOD_ID = "godweapons";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.register();
		Effects.register();
		HitEvents.register();
		CelestialEvents.register();
		LOGGER.info("God Weapons loaded. Good luck, everything else.");
	}
}
