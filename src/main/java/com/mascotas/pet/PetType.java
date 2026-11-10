package com.mascotas.pet;

import static com.mascotas.pet.PetSize.LARGE;
import static com.mascotas.pet.PetSize.MEDIUM;
import static com.mascotas.pet.PetSize.SMALL;

import java.util.Locale;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.world.biome.BiomeKeys;

/**
 * Las 30 mascotas. Para cada una: tamaño, colores del huevo, rareza de spawn,
 * regla de domesticación y biomas donde aparece. El beneficio vive en {@link PetBenefits}.
 */
public enum PetType {
    ZORRO_FUEGO(SMALL, 0xE8590C, 0xFFD43B, 5,
            TamingRule.of(Items.COAL, Conds.biomes(BiomeKeys.DESERT, BiomeKeys.BADLANDS, BiomeKeys.SAVANNA, BiomeKeys.ERODED_BADLANDS), 3),
            BiomeSelectors.includeByKey(BiomeKeys.DESERT, BiomeKeys.BADLANDS, BiomeKeys.SAVANNA, BiomeKeys.ERODED_BADLANDS)),
    BUHO_NOCTURNO(SMALL, 0x5C4033, 0xE0C9A6, 5,
            TamingRule.of(Items.RABBIT, Conds.NIGHT, 2),
            BiomeSelectors.tag(BiomeTags.IS_FOREST)),
    SLIME_GELATINA(SMALL, 0x6FD08C, 0xB8F2C8, 6,
            TamingRule.of(Items.SLIME_BALL, Conds.RAIN, 2),
            BiomeSelectors.includeByKey(BiomeKeys.SWAMP, BiomeKeys.MANGROVE_SWAMP)),
    CIERVO_BOSQUE(LARGE, 0xB5793D, 0xF2E3C6, 5,
            TamingRule.of(Items.GOLDEN_APPLE, Conds.tag(BiomeTags.IS_FOREST), 1),
            BiomeSelectors.tag(BiomeTags.IS_FOREST)),
    PANDA_BAMBU(LARGE, 0xF5F5F5, 0x2B2B2B, 4,
            TamingRule.of(Items.BAMBOO, Conds.ANY, 10),
            BiomeSelectors.tag(BiomeTags.IS_JUNGLE)),
    DRAGON_HIELO(MEDIUM, 0x8FD3F4, 0xE8F7FF, 2,
            TamingRule.of(Items.BLUE_ICE, Conds.biomes(BiomeKeys.SNOWY_PLAINS, BiomeKeys.SNOWY_TAIGA, BiomeKeys.ICE_SPIKES,
                    BiomeKeys.FROZEN_PEAKS, BiomeKeys.GROVE, BiomeKeys.SNOWY_SLOPES, BiomeKeys.JAGGED_PEAKS), 1),
            BiomeSelectors.includeByKey(BiomeKeys.SNOWY_PLAINS, BiomeKeys.SNOWY_TAIGA, BiomeKeys.ICE_SPIKES,
                    BiomeKeys.FROZEN_PEAKS, BiomeKeys.GROVE, BiomeKeys.SNOWY_SLOPES)),
    GATO_ESTELAR(SMALL, 0x2E2A5E, 0xFFE27A, 3,
            TamingRule.of(Items.COD, Conds.NIGHT_CLEAR, 2),
            BiomeSelectors.foundInOverworld()),
    LOBO_CENIZA(MEDIUM, 0x6B6B6B, 0xFF6B2B, 4,
            TamingRule.of(Items.BONE, Conds.NETHER, 3),
            BiomeSelectors.foundInTheNether()),
    PULPO_CORAL(SMALL, 0xFF7F9E, 0xFFC2D1, 4,
            TamingRule.of(Items.COD, Conds.UNDERWATER, 1),
            BiomeSelectors.tag(BiomeTags.IS_BEACH).or(BiomeSelectors.tag(BiomeTags.IS_RIVER))),
    MAPACHE_LADRON(SMALL, 0x8A8A8A, 0x2B2B2B, 5,
            TamingRule.of(Items.EMERALD, Conds.SNEAKING, 1),
            BiomeSelectors.tag(BiomeTags.IS_TAIGA).or(BiomeSelectors.tag(BiomeTags.IS_FOREST))),
    GOLEM_MUSGO(LARGE, 0x6E8B3D, 0xA9C46C, 3,
            TamingRule.of(Items.MOSS_BLOCK, Conds.ANY, 2),
            BiomeSelectors.includeByKey(BiomeKeys.DARK_FOREST, BiomeKeys.LUSH_CAVES)),
    TORTUGA_CRISTAL(MEDIUM, 0xB48CE8, 0xE9D6FF, 3,
            TamingRule.of(Items.AMETHYST_SHARD, Conds.UNDERGROUND, 3),
            BiomeSelectors.includeByKey(BiomeKeys.DRIPSTONE_CAVES, BiomeKeys.LUSH_CAVES)),
    MURCIELAGO_LUZ(SMALL, 0x3B2A5A, 0xFFE066, 3,
            TamingRule.of(Items.GLOW_BERRIES, Conds.UNDERGROUND, 2),
            BiomeSelectors.includeByKey(BiomeKeys.DRIPSTONE_CAVES, BiomeKeys.LUSH_CAVES, BiomeKeys.DARK_FOREST)),
    RANA_PANTANO(SMALL, 0x4F8F3A, 0xE3D26F, 5,
            TamingRule.of(Items.MAGMA_CREAM, Conds.biomes(BiomeKeys.SWAMP, BiomeKeys.MANGROVE_SWAMP), 1),
            BiomeSelectors.includeByKey(BiomeKeys.SWAMP, BiomeKeys.MANGROVE_SWAMP)),
    FENIX_CRIA(MEDIUM, 0xFF5A1F, 0xFFD23F, 1,
            TamingRule.of(Items.FEATHER, Conds.NETHER, 3),
            BiomeSelectors.foundInTheNether()),
    CAMALEON(SMALL, 0x58B368, 0xF2C94C, 4,
            TamingRule.of(Items.SPIDER_EYE, Conds.tag(BiomeTags.IS_JUNGLE), 3),
            BiomeSelectors.tag(BiomeTags.IS_JUNGLE)),
    ERIZO_CRISTAL(SMALL, 0x9A6B4B, 0x7FE0E0, 4,
            TamingRule.of(Items.BEETROOT, Conds.ANY, 4),
            BiomeSelectors.tag(BiomeTags.IS_FOREST).or(BiomeSelectors.tag(BiomeTags.IS_TAIGA))),
    CONEJO_LUNAR(SMALL, 0xD9DDF5, 0x9AA5E8, 3,
            TamingRule.of(Items.GOLDEN_CARROT, Conds.NIGHT, 1),
            BiomeSelectors.includeByKey(BiomeKeys.MEADOW, BiomeKeys.CHERRY_GROVE)),
    YAK_NIEVE(LARGE, 0xEDEDED, 0x8C7B6B, 4,
            TamingRule.of(Items.WHEAT, Conds.HIGH, 5),
            BiomeSelectors.tag(BiomeTags.IS_MOUNTAIN)),
    MEDUSA_FLOTANTE(SMALL, 0xC59BFF, 0xF2E5FF, 3,
            TamingRule.of(Items.DRIED_KELP, Conds.UNDERWATER, 3),
            BiomeSelectors.tag(BiomeTags.IS_RIVER).or(BiomeSelectors.tag(BiomeTags.IS_BEACH))),
    ARDILLA_RECOLECTORA(SMALL, 0xB5651D, 0xF4D8A8, 5,
            TamingRule.of(Items.PUMPKIN_SEEDS, Conds.tag(BiomeTags.IS_FOREST), 3),
            BiomeSelectors.tag(BiomeTags.IS_FOREST)),
    OSO_MIEL(LARGE, 0xC98A2B, 0xFFE08A, 4,
            TamingRule.of(Items.HONEYCOMB, Conds.ANY, 2),
            BiomeSelectors.includeByKey(BiomeKeys.FLOWER_FOREST, BiomeKeys.MEADOW, BiomeKeys.SUNFLOWER_PLAINS)),
    CANGREJO_ARENA(SMALL, 0xE86A4A, 0xFFB199, 5,
            TamingRule.of(Items.SEA_PICKLE, Conds.tag(BiomeTags.IS_BEACH), 2),
            BiomeSelectors.tag(BiomeTags.IS_BEACH)),
    LINCE_CUMBRES(MEDIUM, 0xB8A58A, 0x3A3329, 3,
            TamingRule.of(Items.CHICKEN, Conds.HIGH, 2),
            BiomeSelectors.tag(BiomeTags.IS_MOUNTAIN).or(BiomeSelectors.tag(BiomeTags.IS_TAIGA))),
    HADA_FLORES(SMALL, 0xFF9ECF, 0xFFF3A3, 2,
            TamingRule.distinct(stack -> stack.isIn(ItemTags.FLOWERS), Conds.ANY, 5),
            BiomeSelectors.includeByKey(BiomeKeys.FLOWER_FOREST, BiomeKeys.CHERRY_GROVE, BiomeKeys.MEADOW)),
    SERPIENTE_JADE(MEDIUM, 0x2FA36B, 0xCFF5DE, 3,
            TamingRule.of(Items.FERMENTED_SPIDER_EYE, Conds.ANY, 1),
            BiomeSelectors.tag(BiomeTags.IS_JUNGLE).or(BiomeSelectors.includeByKey(BiomeKeys.SWAMP))),
    ALPACA_ARCOIRIS(LARGE, 0xF2E6D8, 0xFF7AB6, 3,
            TamingRule.distinct(stack -> stack.getItem() instanceof DyeItem, Conds.ANY, 5),
            BiomeSelectors.includeByKey(BiomeKeys.MEADOW, BiomeKeys.SAVANNA, BiomeKeys.SAVANNA_PLATEAU)),
    SALAMANDRA_LAVA(SMALL, 0xD7351A, 0xFFB62B, 4,
            TamingRule.of(Items.BLAZE_POWDER, Conds.NETHER, 2),
            BiomeSelectors.foundInTheNether()),
    ENT_PEQUENO(MEDIUM, 0x6B4A2B, 0x58A33B, 3,
            TamingRule.of(stack -> stack.isIn(ItemTags.SAPLINGS), Conds.ANY, 1),
            BiomeSelectors.includeByKey(BiomeKeys.OLD_GROWTH_BIRCH_FOREST, BiomeKeys.DARK_FOREST, BiomeKeys.OLD_GROWTH_PINE_TAIGA)),
    ESPIRITU_END(MEDIUM, 0x1A1A2E, 0xC77DFF, 4,
            TamingRule.of(Items.ENDER_EYE, Conds.END, 1),
            BiomeSelectors.foundInTheEnd());

    private final PetSize size;
    private final int eggPrimary;
    private final int eggSecondary;
    private final int weight;
    private final TamingRule rule;
    private final Predicate<BiomeSelectionContext> biomes;

    PetType(PetSize size, int eggPrimary, int eggSecondary, int weight, TamingRule rule,
            Predicate<BiomeSelectionContext> biomes) {
        this.size = size;
        this.eggPrimary = eggPrimary;
        this.eggSecondary = eggSecondary;
        this.weight = weight;
        this.rule = rule;
        this.biomes = biomes;
    }

    /** Identificador en minúsculas, usado en assets y registros (p. ej. "zorro_fuego"). */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public PetSize size() { return size; }
    public int eggPrimary() { return eggPrimary; }
    public int eggSecondary() { return eggSecondary; }
    public int weight() { return weight; }
    public TamingRule rule() { return rule; }
    public Predicate<BiomeSelectionContext> biomes() { return biomes; }
}
