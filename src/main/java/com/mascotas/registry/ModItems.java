package com.mascotas.registry;

import com.mascotas.MascotasMod;
import com.mascotas.pet.PetType;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/** Huevos de aparición (modo creativo) para probar cada mascota. */
public final class ModItems {
    private static final List<Item> EGGS = new ArrayList<>();

    private ModItems() {}

    public static void register() {
        for (PetType pet : PetType.values()) {
            Item egg = new SpawnEggItem(ModEntities.TYPES.get(pet), pet.eggPrimary(), pet.eggSecondary(),
                    new FabricItemSettings());
            Registry.register(Registries.ITEM, new Identifier(MascotasMod.MOD_ID, pet.id() + "_spawn_egg"), egg);
            EGGS.add(egg);
        }
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> EGGS.forEach(entries::add));
    }
}
