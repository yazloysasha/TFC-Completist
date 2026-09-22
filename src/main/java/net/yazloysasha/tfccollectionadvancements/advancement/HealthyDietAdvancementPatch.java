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
 * TFC text is “eat every berry and tree fruit”. The JSON is generated from
 * {@code BERRIES + FRUITS} — bush and fruit-tree products — with {@code
 * minecraft:consume_item}. {@code c:foods/fruit} is wider (TFC puts {@code
 * melon_slice} there) and is not the advancement’s source of truth.
 * <p>
 * Rows are filled from {@link SeasonalFruitItems} (including TFC, so a new bush
 * that never made it into the JSON still counts). Addon items tagged {@code
 * c:foods/fruit} cover fruits that are not {@code SeasonalPlantBlock} products,
 * such as FirmaLife grapes.
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
    InventoryCollectionAdvancementPatch.addItems(
      ADVANCEMENT,
      advancements,
      SeasonalFruitItems.collect(registries),
      AdvancementCriterionBuilder::consumeItem
    );
    InventoryCollectionAdvancementPatch.patchConsume(
      ADVANCEMENT,
      advancements,
      registries,
      SOURCES
    );
  }
}
