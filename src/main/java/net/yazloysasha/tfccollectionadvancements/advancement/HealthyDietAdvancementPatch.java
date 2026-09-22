package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

public final class HealthyDietAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/fruit");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    new InventoryCollectionSource("firmalife", "food/fig", null, true),
    new InventoryCollectionSource("firmalife", "food/pineapple", null, true),
    new InventoryCollectionSource("firmalife", "food/red_grapes", null, true),
    new InventoryCollectionSource("firmalife", "food/white_grapes", null, true),
    new InventoryCollectionSource(
      "firmalife",
      "food/nightshade_berry",
      null,
      true
    )
  );

  private HealthyDietAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancementPatch.patchConsume(
      ADVANCEMENT,
      advancements,
      registries,
      SOURCES
    );
  }
}
