package com.mascotas.client;

import com.mascotas.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class MascotasClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModEntities.TYPES.values().forEach(type -> EntityRendererRegistry.register(type, PetRenderer::new));
    }
}
