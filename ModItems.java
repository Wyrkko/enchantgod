package com.godweapons;

import com.godweapons.item.CelestialJudgementItem;
import com.godweapons.item.GodslayerItem;
import com.godweapons.item.VoidReaperItem;
import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Unit;

public class ModItems {

	// Damage numbers: final hit = 1 (base) + 4 (netherite) + the number below
	public static final Item VOID_REAPER = register("void_reaper",
		s -> new VoidReaperItem(s.sword(ToolMaterial.NETHERITE, 12.0f, -2.2f)));

	public static final Item GODSLAYER = register("godslayer",
		s -> new GodslayerItem(s)); // axe damage 12 / speed -2.8 is set inside GodslayerItem

	public static final Item CELESTIAL_JUDGEMENT = register("celestial_judgement",
		s -> new CelestialJudgementItem(s.sword(ToolMaterial.NETHERITE, 14.0f, -2.0f)));

	private static Item.Settings base(Item.Settings s) {
		return s.rarity(Rarity.EPIC)
			.fireproof()
			.component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
			.component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
	}

	private static <T extends Item> T register(String name, Function<Item.Settings, T> factory) {
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(GodWeaponsMod.MOD_ID, name));
		T item = factory.apply(base(new Item.Settings().registryKey(key)));
		return Registry.register(Registries.ITEM, key, item);
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
			entries.add(VOID_REAPER);
			entries.add(GODSLAYER);
			entries.add(CELESTIAL_JUDGEMENT);
		});
	}
}
