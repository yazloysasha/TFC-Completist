package net.yazloysasha.tfccollectionadvancements.advancement.custom;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.common.Tags;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancement;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;
import net.yazloysasha.tfccollectionadvancements.util.MeatCollectionItems;

/**
 * Rebuilds Sea Cook ({@code tfc_collection_advancements:world/sea_cook}).
 * <p>
 * Every {@code tfc} and addon item in {@code c:foods/cooked_fish}, except
 * {@code cooked_turtle} (counts toward Carnivore / Butcher).
 */
public final class SeaCookAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/sea_cook"
    );

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.discoveredTagExcludingItems(
      Tags.Items.FOODS_COOKED_FISH,
      MeatCollectionItems.SEAFOOD_TAG_EXCLUSIONS
    )
  );

  private SeaCookAdvancement() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancement.patchConsume(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      SOURCES
    );
  }
}
