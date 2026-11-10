package com.mascotas.client;

import com.mascotas.MascotasMod;
import com.mascotas.entity.PetEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

/** Cada mascota carga su propio .geo.json, textura y .animation.json por id. */
public class PetModel extends GeoModel<PetEntity> {
    @Override
    public Identifier getModelResource(PetEntity pet) {
        return new Identifier(MascotasMod.MOD_ID, "geo/" + pet.getPetType().id() + ".geo.json");
    }

    @Override
    public Identifier getTextureResource(PetEntity pet) {
        return new Identifier(MascotasMod.MOD_ID, "textures/entity/" + pet.getPetType().id() + ".png");
    }

    @Override
    public Identifier getAnimationResource(PetEntity pet) {
        return new Identifier(MascotasMod.MOD_ID, "animations/" + pet.getPetType().id() + ".animation.json");
    }

    @Override
    public void setCustomAnimations(PetEntity pet, long instanceId, AnimationState<PetEntity> state) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head == null) return;
        EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);
        head.setRotX(data.headPitch() * MathHelper.RADIANS_PER_DEGREE);
        head.setRotY(data.netHeadYaw() * MathHelper.RADIANS_PER_DEGREE);
    }
}
