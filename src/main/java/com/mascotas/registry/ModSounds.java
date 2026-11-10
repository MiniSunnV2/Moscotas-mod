package com.mascotas.registry;

import com.mascotas.MascotasMod;
import com.mascotas.pet.PetType;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/** Tres eventos de sonido por mascota: ambient, hurt y death (definidos en sounds.json). */
public final class ModSounds {
    private static final String[] KINDS = {"ambient", "hurt", "death"};
    private static final Map<String, SoundEvent> EVENTS = new HashMap<>();

    private ModSounds() {}

    public static void register() {
        for (PetType pet : PetType.values()) {
            for (String kind : KINDS) {
                String key = pet.id() + "." + kind;
                Identifier id = new Identifier(MascotasMod.MOD_ID, key);
                EVENTS.put(key, Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id)));
            }
        }
    }

    public static SoundEvent get(PetType pet, String kind) {
        return EVENTS.get(pet.id() + "." + kind);
    }
}
