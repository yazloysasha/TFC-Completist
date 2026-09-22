package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Patches TFC True Farmer ({@code tfc:world/all_crops}), the full seed checklist after {@code
 * tfc:world/seeds}.
 * <p>
 * Base criteria are {@code minecraft:inventory_changed}, one distinct {@code .../seeds/{crop}} item
 * per row in the advancement — the same id pattern TFC uses for farm crops, not “anything
 * plantable”. Obtaining the item counts; how the crop is planted in the addon (farmland, planter,
 * trellis, etc.) is irrelevant to the trigger.
 * <p>
 * Include addon crops that ship a dedicated seed item under {@code seeds/...}. Do not fold in tree
 * saplings, forage bushes, ore, or processed food. A broad {@code seeds/} prefix is appropriate
 * when every matching item in that namespace is meant to be an extra row in this checklist; use
 * {@code exactPath} when only specific ids qualify.
 */
public final class TrueFarmerAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/all_crops");

  private static final String SEEDS_PATH_PREFIX = "seeds/";

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    new InventoryCollectionSource("beneath", SEEDS_PATH_PREFIX),
    new InventoryCollectionSource("firmalife", SEEDS_PATH_PREFIX)
  );

  private TrueFarmerAdvancementPatch() {}

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
