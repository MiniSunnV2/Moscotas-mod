package com.mascotas.pet;

import com.mascotas.entity.PetEntity;
import net.minecraft.entity.player.PlayerEntity;

/** Condición extra (hora, dimensión, bioma, postura...) que debe cumplirse para domesticar. */
@FunctionalInterface
public interface PetCondition {
    boolean test(PetEntity pet, PlayerEntity player);
}
