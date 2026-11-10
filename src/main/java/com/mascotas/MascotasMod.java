package com.mascotas;

import com.mascotas.pet.PetEvents;
import com.mascotas.registry.ModEntities;
import com.mascotas.registry.ModItems;
import com.mascotas.registry.ModSounds;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MascotasMod implements ModInitializer {
    public static final String MOD_ID = "mascotas";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModSounds.register();
        ModEntities.register();
        ModItems.register();
        PetEvents.register();
        LOGGER.info("Mascotas cargado: {} mascotas disponibles", com.mascotas.pet.PetType.values().length);
    }
}
