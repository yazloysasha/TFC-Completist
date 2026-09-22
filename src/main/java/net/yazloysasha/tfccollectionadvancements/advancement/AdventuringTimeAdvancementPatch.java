package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.yazloysasha.tfccollectionadvancements.util.BiomeCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.BiomeCollectionSource;

public final class AdventuringTimeAdvancementPatch {

  public static final ResourceLocation TFC_ADVENTURING_TIME_ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/adventuring_time");

  private static final TagKey<Biome> IS_NETHER = TagKey.create(
    Registries.BIOME,
    ResourceLocation.withDefaultNamespace("is_nether")
  );

  private static final List<BiomeCollectionSource> BIOME_SOURCES = List.of(
    new BiomeCollectionSource("beneath", "minecraft", IS_NETHER)
  );

  private AdventuringTimeAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    HolderLookup.Provider registries
  ) {
    BiomeCollectionAdvancementPatch.patch(
      TFC_ADVENTURING_TIME_ADVANCEMENT,
      advancements,
      registries,
      BIOME_SOURCES
    );
  }
}
