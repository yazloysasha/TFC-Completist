package net.yazloysasha.tfccollectionadvancements.advancement.patch;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.yazloysasha.tfccollectionadvancements.util.BiomeCollectionAdvancement;
import net.yazloysasha.tfccollectionadvancements.util.BiomeCollectionSource;

/**
 * Rebuilds TFC Adventuring Time ({@code tfc:world/adventuring_time}).
 * <p>
 * Every non-Minecraft biome, plus Minecraft nether biomes when Beneath is
 * loaded (Beneath reuses vanilla nether biome ids). Vanilla rows use
 * {@code minecraft:location} with a biome id in the predicate.
 */
public final class AdventuringTimeAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/adventuring_time");

  private static final TagKey<Biome> IS_NETHER = TagKey.create(
    Registries.BIOME,
    ResourceLocation.withDefaultNamespace("is_nether")
  );

  private static final List<BiomeCollectionSource> SOURCES = List.of(
    BiomeCollectionSource.discovered(),
    new BiomeCollectionSource("beneath", "minecraft", IS_NETHER)
  );

  private AdventuringTimeAdvancement() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    BiomeCollectionAdvancement.patch(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      SOURCES
    );
  }
}
