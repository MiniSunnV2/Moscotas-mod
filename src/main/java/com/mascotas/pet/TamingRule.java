package com.mascotas.pet;

import java.util.function.Predicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Regla de domesticación: qué item hay que dar, bajo qué condición,
 * cuántas veces y si cada item debe ser distinto (p. ej. flores variadas).
 */
public record TamingRule(Predicate<ItemStack> item, PetCondition condition, int feeds, boolean distinct) {

    public static TamingRule of(Item item, PetCondition condition, int feeds) {
        return new TamingRule(stack -> stack.isOf(item), condition, feeds, false);
    }

    public static TamingRule of(Predicate<ItemStack> item, PetCondition condition, int feeds) {
        return new TamingRule(item, condition, feeds, false);
    }

    public static TamingRule distinct(Predicate<ItemStack> item, PetCondition condition, int feeds) {
        return new TamingRule(item, condition, feeds, true);
    }
}
