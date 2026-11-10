package com.mascotas.pet;

import com.mascotas.entity.PetEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

/** Beneficios reactivos: se activan cuando el dueño recibe daño o está a punto de morir. */
public final class PetEvents {
    private PetEvents() {}

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(PetEvents::allowDamage);
        ServerLivingEntityEvents.ALLOW_DEATH.register(PetEvents::allowDeath);
    }

    private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayerEntity player) || entity.getWorld().isClient) return true;

        // Slime gelatina: sin daño de caída
        if (source.isOf(DamageTypes.FALL) && PetBenefits.findPet(player, PetType.SLIME_GELATINA) != null) return false;
        // Dragón de hielo: inmune a la congelación
        if (source.isOf(DamageTypes.FREEZE) && PetBenefits.findPet(player, PetType.DRAGON_HIELO) != null) return false;
        // Salamandra de lava: inmune al daño de lava
        if (source.isOf(DamageTypes.LAVA) && PetBenefits.findPet(player, PetType.SALAMANDRA_LAVA) != null) return false;

        // Erizo de cristal: refleja daño al atacante
        if (source.getAttacker() instanceof LivingEntity attacker && attacker != player
                && PetBenefits.findPet(player, PetType.ERIZO_CRISTAL) != null) {
            attacker.damage(player.getDamageSources().thorns(player), 2.0f);
        }

        // Espíritu del End: 20 % de probabilidad de esquivar teletransportándose
        if (amount >= 2.0f && player.getRandom().nextFloat() < 0.2f
                && PetBenefits.findPet(player, PetType.ESPIRITU_END) != null && dodge(player)) {
            return false;
        }
        return true;
    }

    private static boolean dodge(ServerPlayerEntity player) {
        for (int i = 0; i < 8; i++) {
            double dx = (player.getRandom().nextDouble() - 0.5) * 12.0;
            double dz = (player.getRandom().nextDouble() - 0.5) * 12.0;
            Box moved = player.getBoundingBox().offset(dx, 0, dz);
            if (player.getWorld().isSpaceEmpty(player, moved)) {
                player.requestTeleport(player.getX() + dx, player.getY(), player.getZ() + dz);
                return true;
            }
        }
        return false;
    }

    private static boolean allowDeath(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayerEntity player)) return true;
        PetEntity phoenix = PetBenefits.findPet(player, PetType.FENIX_CRIA);
        if (phoenix == null) return true;

        long now = player.getWorld().getTime();
        if (now - phoenix.getLastRevive() < 24000L) return true; // en enfriamiento (1 día de Minecraft)

        phoenix.setLastRevive(now);
        player.setHealth(10.0f);
        player.clearStatusEffects();
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
        player.getWorld().sendEntityStatus(player, (byte) 35); // animación del tótem
        return false;
    }
}
