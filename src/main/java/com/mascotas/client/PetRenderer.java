package com.mascotas.client;

import com.mascotas.entity.PetEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PetRenderer extends GeoEntityRenderer<PetEntity> {
    public PetRenderer(EntityRendererFactory.Context context) {
        super(context, new PetModel());
        this.shadowRadius = 0.35f;
    }
}
