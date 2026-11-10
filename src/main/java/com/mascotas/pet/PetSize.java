package com.mascotas.pet;

/** Tamaño de la caja de colisión de cada mascota. */
public enum PetSize {
    SMALL(0.5f, 0.6f),
    MEDIUM(0.7f, 0.85f),
    LARGE(0.95f, 1.1f);

    public final float width;
    public final float height;

    PetSize(float width, float height) {
        this.width = width;
        this.height = height;
    }
}
