package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.yazloysasha.tfccollectionadvancements.util.BiomeCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.BiomeCollectionSource;

/**
 * Patches TFC Adventuring Time ({@code tfc:world/adventuring_time}).
 * <p>
 * Vanilla rows use {@code minecraft:location} with a biome id in the predicate — visit every
 * listed TFC overworld biome once. Addon patches extend that checklist with extra biomes the
 * player should discover in the same playthrough, using the same trigger shape via {@link
 * BiomeCollectionAdvancementPatch}.
 * <p>
 * {@link BiomeCollectionSource} selects biomes by mod namespace, optional biome id namespace
 * filter, and a biome tag (for example nether-tagged ids when a dimension reuses vanilla biome
 * names). Sources are skipped when the addon is not loaded. Do not add item criteria here;
 * biome ids must resolve from datapacks at reload time.
 */
public final class AdventuringTimeAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/adventuring_time");

  private static final TagKey<Biome> IS_NETHER = TagKey.create(
    Registries.BIOME,
    ResourceLocation.withDefaultNamespace("is_nether")
  );

  private static final List<BiomeCollectionSource> SOURCES = List.of(
    new BiomeCollectionSource("beneath", "minecraft", IS_NETHER)
  );

  private AdventuringTimeAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    BiomeCollectionAdvancementPatch.patch(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      SOURCES
    );
  }
}
