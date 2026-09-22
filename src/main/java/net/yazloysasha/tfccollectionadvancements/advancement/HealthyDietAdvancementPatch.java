package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.Tags;
import net.yazloysasha.tfccollectionadvancements.util.AdvancementCriterionBuilder;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;
import net.yazloysasha.tfccollectionadvancements.util.SeasonalFruitItems;

/**
 * Patches TFC Healthy Diet ({@code tfc:world/fruit}).
 * <p>
 * Base TFC uses {@code minecraft:consume_item}. Addon rows stay on that trigger
 * via {@link InventoryCollectionAdvancementPatch#patchConsume}.
 * <p>
 * New fruits are discovered from {@code c:foods/fruit} plus TFC-style berry
 * bush / fruit tree products (so untagged berries such as nightshade still
 * count). TFC’s own items are left to the original advancement.
 */
public final class HealthyDietAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/fruit");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.addonTag(Tags.Items.FOODS_FRUIT)
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
    InventoryCollectionAdvancementPatch.addItems(
      ADVANCEMENT,
      advancements,
      SeasonalFruitItems.collectAddonProducts(registries),
      AdvancementCriterionBuilder::consumeItem
    );
  }
}
