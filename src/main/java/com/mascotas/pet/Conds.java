package com.mascotas.pet;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/** Condiciones reutilizables para las reglas de domesticación. */
public final class Conds {
    private Conds() {}

    private static boolean isNight(long time) {
        long t = time % 24000L;
        return t >= 13000L && t < 23000L;
    }

    public static final PetCondition ANY = (pet, player) -> true;
    public static final PetCondition NIGHT = (pet, player) -> isNight(pet.getWorld().getTimeOfDay());
    public static final PetCondition NIGHT_CLEAR = (pet, player) ->
            isNight(pet.getWorld().getTimeOfDay()) && !pet.getWorld().isRaining();
    public static final PetCondition RAIN = (pet, player) -> pet.getWorld().isRaining();
    public static final PetCondition NETHER = (pet, player) -> pet.getWorld().getRegistryKey() == World.NETHER;
    public static final PetCondition END = (pet, player) -> pet.getWorld().getRegistryKey() == World.END;
    public static final PetCondition SNEAKING = (pet, player) -> player.isSneaking();
    public static final PetCondition UNDERWATER = (pet, player) -> player.isSubmergedInWater();
    public static final PetCondition HIGH = (pet, player) -> pet.getY() > 90;
    public static final PetCondition UNDERGROUND = (pet, player) ->
            pet.getY() < 50 && !pet.getWorld().isSkyVisible(pet.getBlockPos());

    @SafeVarargs
    public static PetCondition biomes(RegistryKey<Biome>... keys) {
        return (pet, player) -> {
            RegistryEntry<Biome> biome = pet.getWorld().getBiome(pet.getBlockPos());
            for (RegistryKey<Biome> key : keys) {
                if (biome.matchesKey(key)) return true;
            }
            return false;
        };
    }

    public static PetCondition tag(TagKey<Biome> tag) {
        return (pet, player) -> pet.getWorld().getBiome(pet.getBlockPos()).isIn(tag);
    }
}
