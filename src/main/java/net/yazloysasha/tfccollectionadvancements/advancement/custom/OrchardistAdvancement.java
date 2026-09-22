package net.yazloysasha.tfccollectionadvancements.advancement.custom;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancement;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Rebuilds Orchardist ({@code tfc_collection_advancements:world/orchardist}).
 * <p>
 * Fruit-tree saplings under {@code plant/..._sapling}, including TFC (banana,
 * cherry, apples, ...) and addons that follow the same path (Firmalife cocoa and
 * fig). Timber saplings stay on Arborist ({@code wood/sapling/...}). Potted
 * variants have no item.
 */
public final class OrchardistAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/orchardist"
    );

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.discovered("plant/", "_sapling")
  );

  private OrchardistAdvancement() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancement.patchPlacedBlock(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      SOURCES
    );
  }
}
