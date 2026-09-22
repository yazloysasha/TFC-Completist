package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Patches TFC True Farmer ({@code tfc:world/all_crops}).
 * <p>
 * Every {@code seeds/...} item from TFC and addons (for example Firmalife, Beneath),
 * including crops that never made it into the base JSON. Vanilla {@code minecraft}
 * seeds are ignored. Items only tagged in {@code c:seeds} without a {@code seeds/}
 * path (for example Farmer's Delight) are excluded.
 */
public final class TrueFarmerAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/all_crops");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.discovered("seeds/")
  );

  private TrueFarmerAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancementPatch.patch(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      SOURCES
    );
  }
}
