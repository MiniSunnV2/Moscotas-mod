package com.mascotas.registry;

import com.mascotas.MascotasMod;
import com.mascotas.entity.PetEntity;
import com.mascotas.pet.PetType;
import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

public final class ModEntities {
    public static final Map<PetType, EntityType<PetEntity>> TYPES = new EnumMap<>(PetType.class);

    private ModEntities() {}

    public static void register() {
        for (PetType pet : PetType.values()) {
            EntityType<PetEntity> type = Registry.register(
                    Registries.ENTITY_TYPE,
                    new Identifier(MascotasMod.MOD_ID, pet.id()),
                    FabricEntityTypeBuilder.createMob()
                            .<PetEntity>entityFactory((t, world) -> new PetEntity(t, world, pet))
                            .spawnGroup(SpawnGroup.CREATURE)
                            .dimensions(EntityDimensions.fixed(pet.size().width, pet.size().height))
                            .spawnRestriction(SpawnRestriction.Location.ON_GROUND,
                                    Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, PetEntity::canSpawnPet)
                            .trackRangeBlocks(64)
                            .build());

            FabricDefaultAttributeRegistry.register(type, PetEntity.createPetAttributes());
            BiomeModifications.addSpawn(pet.biomes(), SpawnGroup.CREATURE, type, pet.weight(), 1, 2);
            TYPES.put(pet, type);
        }
    }
}
