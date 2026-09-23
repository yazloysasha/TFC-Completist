package net.yazloysasha.tfccollectionadvancements.advancement.patch;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.util.AdvancementCriterionBuilder;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancement;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Rebuilds TFC Arborist ({@code tfc:world/saplings}).
 * <p>
 * Timber saplings under {@code wood/sapling/...}, including TFC. Fruit-tree
 * saplings use {@code plant/..._sapling} and belong with
 * {@link net.yazloysasha.tfccollectionadvancements.advancement.custom.OrchardistAdvancement}.
 */
public final class ArboristAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/saplings");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.discovered("wood/sapling/")
  );

  private ArboristAdvancement() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancement.patch(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      SOURCES
    );

    JsonElement advancement = advancements.get(ADVANCEMENT);
    if (advancement != null && advancement.isJsonObject()) {
      AdvancementCriterionBuilder.setFrame(
        advancement.getAsJsonObject(),
        "goal"
      );
    }
  }
}
