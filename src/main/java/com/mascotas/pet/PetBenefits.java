package com.mascotas.pet;

import com.mascotas.entity.PetEntity;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Beneficios pasivos. {@link #tick} se llama cada tick mientras la mascota está
 * domesticada y su dueño está a menos de 32 bloques. Los beneficios reactivos
 * (caída, congelación, lava, espinas, revivir, esquivar) están en {@link PetEvents}.
 */
public final class PetBenefits {
    private PetBenefits() {}

    public static void tick(PetEntity pet, ServerPlayerEntity owner) {
        switch (pet.getPetType()) {
            case ZORRO_FUEGO -> pulse(pet, owner, StatusEffects.FIRE_RESISTANCE, 0);
            case BUHO_NOCTURNO -> pulse(pet, owner, StatusEffects.NIGHT_VISION, 0);
            case CIERVO_BOSQUE -> pulse(pet, owner, StatusEffects.SPEED, 0);
            case PANDA_BAMBU -> pulse(pet, owner, StatusEffects.HEALTH_BOOST, 1);
            case GATO_ESTELAR -> pulse(pet, owner, StatusEffects.LUCK, 0);
            case LOBO_CENIZA -> pulse(pet, owner, StatusEffects.STRENGTH, 0);
            case PULPO_CORAL -> pulse(pet, owner, StatusEffects.WATER_BREATHING, 0);
            case MAPACHE_LADRON -> pulse(pet, owner, StatusEffects.HERO_OF_THE_VILLAGE, 0);
            case GOLEM_MUSGO -> pulse(pet, owner, StatusEffects.RESISTANCE, 0);
            case TORTUGA_CRISTAL -> pulse(pet, owner, StatusEffects.ABSORPTION, 1);
            case MURCIELAGO_LUZ -> {
                if (owner.getY() < 50 && !owner.getWorld().isSkyVisible(owner.getBlockPos())) {
                    pulse(pet, owner, StatusEffects.NIGHT_VISION, 0);
                }
            }
            case RANA_PANTANO -> pulse(pet, owner, StatusEffects.JUMP_BOOST, 1);
            case CAMALEON -> {
                if (owner.isSneaking()) pulse(pet, owner, StatusEffects.INVISIBILITY, 0);
            }
            case CONEJO_LUNAR -> pulse(pet, owner, StatusEffects.SLOW_FALLING, 0);
            case YAK_NIEVE -> {
                if (pet.age % 600 == 0) owner.getHungerManager().add(2, 0.5f);
            }
            case MEDUSA_FLOTANTE -> pulse(pet, owner, StatusEffects.DOLPHINS_GRACE, 0);
            case ARDILLA_RECOLECTORA -> magnet(owner);
            case OSO_MIEL -> pulse(pet, owner, StatusEffects.HASTE, 0);
            case CANGREJO_ARENA -> pulse(pet, owner, StatusEffects.CONDUIT_POWER, 0);
            case LINCE_CUMBRES -> {
                if (pet.age % 20 == 0) {
                    List<HostileEntity> mobs = owner.getWorld().getEntitiesByClass(
                            HostileEntity.class, owner.getBoundingBox().expand(16), e -> e.isAlive());
                    for (HostileEntity mob : mobs) {
                        mob.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60, 0, true, false, false));
                    }
                }
            }
            case HADA_FLORES -> pulse(pet, owner, StatusEffects.REGENERATION, 0);
            case SERPIENTE_JADE -> {
                if (owner.hasStatusEffect(StatusEffects.POISON)) owner.removeStatusEffect(StatusEffects.POISON);
            }
            case ALPACA_ARCOIRIS -> {
                if (pet.age % 600 == 0) owner.addExperience(1);
            }
            case ENT_PEQUENO -> growCrops(pet);
            default -> { /* beneficios reactivos: ver PetEvents */ }
        }
    }

    private static void pulse(PetEntity pet, ServerPlayerEntity owner, StatusEffect effect, int amplifier) {
        if (pet.age % 40 != 0) return;
        owner.addStatusEffect(new StatusEffectInstance(effect, 300, amplifier, true, false, true));
    }

    private static void magnet(ServerPlayerEntity owner) {
        List<ItemEntity> items = owner.getWorld().getEntitiesByClass(
                ItemEntity.class, owner.getBoundingBox().expand(6), e -> e.isAlive());
        Vec3d target = owner.getPos().add(0, 0.6, 0);
        for (ItemEntity item : items) {
            Vec3d pull = target.subtract(item.getPos());
            if (pull.lengthSquared() > 0.5) {
                item.setVelocity(item.getVelocity().add(pull.normalize().multiply(0.06)));
                item.velocityModified = true;
            }
        }
    }

    private static void growCrops(PetEntity pet) {
        if (pet.age % 60 != 0 || !(pet.getWorld() instanceof ServerWorld world)) return;
        for (int i = 0; i < 4; i++) {
            BlockPos pos = pet.getBlockPos().add(
                    pet.getRandom().nextInt(9) - 4, pet.getRandom().nextInt(3) - 1, pet.getRandom().nextInt(9) - 4);
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof CropBlock || state.getBlock() instanceof SaplingBlock
                    || state.getBlock() instanceof StemBlock) {
                state.randomTick(world, pos, world.random);
            }
        }
    }

    /** Busca una mascota domesticada del tipo dado, perteneciente a {@code owner}, a menos de 32 bloques. */
    public static PetEntity findPet(ServerPlayerEntity owner, PetType type) {
        List<PetEntity> pets = owner.getWorld().getEntitiesByClass(
                PetEntity.class, owner.getBoundingBox().expand(32),
                p -> p.getPetType() == type && p.isTamed() && p.isOwner(owner));
        return pets.isEmpty() ? null : pets.get(0);
    }
}
