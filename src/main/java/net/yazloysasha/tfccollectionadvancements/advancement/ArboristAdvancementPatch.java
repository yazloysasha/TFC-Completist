package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Patches TFC Arborist ({@code tfc:world/saplings}).
 * <p>
 * Any addon timber sapling under {@code wood/sapling/...} is added. Fruit-tree
 * saplings live under other paths and belong with {@link
 * HealthyDietAdvancementPatch}.
 */
public final class ArboristAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/saplings");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.anyAddon("wood/sapling/")
  );

  private ArboristAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancementPatch.patch(
      ADVANCEMENT,
      advancements,
      registries,
      SOURCES
    );
  }
}
