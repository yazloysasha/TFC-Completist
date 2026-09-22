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
 * Rebuilds Berry Gardener ({@code tfc_collection_advancements:world/berry_gardener}).
 * <p>
 * Berry bushes under {@code plant/..._bush}, including TFC (stationary,
 * spreading, and cranberry) and addons that follow the same path (Firmalife
 * nightshade and pineapple). Spreading canes and dead bushes have no item.
 * Firmalife grapes are seeds on trellises, already covered by True Farmer.
 */
public final class BerryGardenerAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/berry_gardener"
    );

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.discovered("plant/", "_bush")
  );

  private BerryGardenerAdvancement() {}

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
