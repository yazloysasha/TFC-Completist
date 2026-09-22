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
 * Rebuilds Tasty Breads ({@code tfc_collection_advancements:world/tasty_breads}).
 * <p>
 * Every plain bread sandwich ({@code food/*_bread_sandwich}), not jam sandwiches
 * or whole loaves.
 */
public final class TastyBreadsAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/tasty_breads"
    );

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    new InventoryCollectionSource(
      null,
      "food/",
      "_bread_sandwich",
      false,
      null,
      false,
      null,
      true,
      InventoryCollectionSource.CollectedItems.NONE,
      false
    )
  );

  private TastyBreadsAdvancement() {}

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
