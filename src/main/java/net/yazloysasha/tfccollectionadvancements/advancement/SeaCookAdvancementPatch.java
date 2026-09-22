package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.common.Tags;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Rebuilds Sea Cook ({@code tfc_collection_advancements:world/sea_cook}).
 * <p>
 * Cooked fish that pair with {@code c:foods/raw_fish} (same count as Gone
 * Fishing; excludes e.g. {@code cooked_turtle}). Parent is
 * {@code tfc:world/all_fish}.
 */
public final class SeaCookAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/sea_cook"
    );

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.discoveredCookedWithRawCounterpart(
      Tags.Items.FOODS_COOKED_FISH,
      Tags.Items.FOODS_RAW_FISH
    )
  );

  private SeaCookAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancementPatch.patchConsume(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      SOURCES
    );
  }
}
