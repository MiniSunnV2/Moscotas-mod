# 🐾 Mascotas — mod de Minecraft (Fabric 1.20.4)

30 mascotas domesticables con modelos **GeckoLib** (formato Blockbench), sonidos propios por mascota,
aparición natural en el mundo como cualquier mob y un **beneficio único** al domesticarlas.

- Minecraft Java **1.20.4** · Fabric Loader ≥ 0.15 · Fabric API · **GeckoLib 4.4.x**
- Licencia MIT

## Cómo funciona

| Sistema | Detalle |
|---|---|
| **Aparición** | Cada mascota aparece sola en sus biomas (bosque, jungla, Nether, End, cuevas...) con rareza propia. |
| **Domesticación** | Cada una tiene su propia regla: item + condición (noche, lluvia, dimensión, bioma, altura, agachado, bajo el agua...) + cantidad. Click derecho con la mano vacía muestra una pista. |
| **Beneficio** | Pasivo mientras la mascota esté domesticada y a menos de 32 bloques (efectos, imán, cultivos, XP...) o reactivo (revivir, esquivar, inmunidades). |
| **Control** | Click derecho a tu mascota para sentarla / hacer que te siga. |
| **Sonidos** | 3 eventos por mascota (`ambient`, `hurt`, `death`). Por defecto usan sonidos de vainilla con tono distinto; puedes reemplazarlos por `.ogg` propios. |

Lista completa con cómo domesticar y beneficio de cada una: [docs/MASCOTAS.md](docs/MASCOTAS.md).

## Compilar

Requisitos: **JDK 17** y Gradle 8.5+.

```bash
gradle wrapper --gradle-version 8.5   # solo la primera vez (genera gradlew)
./gradlew build                        # el .jar queda en build/libs/
./gradlew runClient                    # abre Minecraft para probar
```

También hay un workflow de GitHub Actions (`.github/workflows/build.yml`) que compila en cada push y sube el `.jar` como artefacto.

Para instalar: copia `build/libs/mascotas-0.1.0.jar` **y GeckoLib 4.4.x para 1.20.4** a la carpeta `mods` junto con Fabric API.

Probar rápido: modo creativo → pestaña **Huevos de aparición** → cada mascota tiene el suyo.

## Estructura

```
src/main/java/com/mascotas/
  MascotasMod.java        punto de entrada
  entity/PetEntity.java   entidad única para las 30 (IA, domesticación, animaciones GeckoLib)
  pet/PetType.java        las 30 mascotas: tamaño, biomas, spawn, regla de domesticación
  pet/PetBenefits.java    beneficios pasivos
  pet/PetEvents.java      beneficios reactivos (revivir, esquivar, inmunidades, espinas)
  pet/Conds.java          condiciones reutilizables (noche, Nether, bioma, ...)
  registry/               entidades, sonidos y huevos
  client/                 renderer + modelo GeckoLib
src/main/resources/assets/mascotas/
  geo/*.geo.json          modelos
  animations/*.json       idle / walk / sit
  textures/entity/*.png   texturas
  sounds.json             eventos de sonido
  lang/                   es_*, en_us
tools/                    generador de assets base (Python + Pillow)
```

## Mejorar modelos, texturas y sonidos

Los modelos y texturas incluidos son una **base generada por código** para que todo funcione desde el primer arranque.
Para dejarlos aún más bonitos:

1. **Modelos:** abre `geo/<mascota>.geo.json` en [Blockbench](https://www.blockbench.net/) (File → Open Model), edita, pinta y exporta con el mismo nombre.
   Conserva los nombres de huesos `body`, `head` y `leg_fl/fr/bl/br` para que las animaciones sigan funcionando, o edita también `animations/<mascota>.animation.json` (animaciones `idle`, `walk`, `sit`).
2. **Texturas:** pinta sobre `textures/entity/<mascota>.png` (128×128).
3. **Sonidos:** coloca tus `.ogg` en `assets/mascotas/sounds/` y cambia la entrada en `sounds.json`, por ejemplo:
   ```json
   "zorro_fuego.ambient": { "sounds": ["mascotas:zorro_ambient"] }
   ```
   (el archivo sería `assets/mascotas/sounds/zorro_ambient.ogg`).
4. Si quieres regenerar los assets base: `pip install pillow && python3 tools/generate_assets.py` (**sobrescribe** lo que hayas editado).

## Agregar una mascota nueva

1. Añade una entrada en `PetType.java` (tamaño, colores del huevo, rareza, regla de domesticación, biomas).
2. Añade su beneficio en `PetBenefits.java` (o `PetEvents.java` si es reactivo).
3. Añade sus datos en `tools/pets_data.py` y ejecuta el generador (o crea a mano geo/animación/textura/lang/sonidos).

## Limitaciones conocidas (v0.1.0)

- Todas las mascotas caminan por tierra (las acuáticas y voladoras aparecen en playas/ríos y no nadan ni vuelan aún).
- Sonidos de vainilla reutilizados con distinto tono hasta que añadas los tuyos.
- Modelos y texturas base (procedurales); pensados para pulirse en Blockbench.
