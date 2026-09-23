package net.yazloysasha.tfccollectionadvancements.advancement.custom;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;
import net.yazloysasha.tfccollectionadvancements.util.CommonFoodTags;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancement;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Rebuilds Breakfast, Lunch, and Dinner ({@code
 * tfc_collection_advancements:world/breakfast_lunch_and_dinner}).
 * <p>
 * Every discoverable {@code food/*_sandwich} (plain and jam; 12 in base TFC).
 * {@code c:foods/sandwiches} is not used: it omits {@code wheat_bread_jam_sandwich}
 * and lists {@code wheat_bread} instead. Also {@code c:foods/soup} and
 * {@code c:foods/salad}.
 */
public final class BreakfastLunchAndDinnerAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/breakfast_lunch_and_dinner"
    );

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.discovered("food/", "_sandwich"),
    InventoryCollectionSource.discoveredTag(CommonFoodTags.SOUP),
    InventoryCollectionSource.discoveredTag(CommonFoodTags.SALAD)
  );

  private BreakfastLunchAndDinnerAdvancement() {}

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
