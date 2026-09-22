package net.yazloysasha.tfccollectionadvancements.advancement.patch;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancement;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Rebuilds TFC True Farmer ({@code tfc:world/all_crops}).
 * <p>
 * Every {@code seeds/...} item from TFC and addons, plus any seed of a TFC
 * {@code CropBlock}. {@code seeds/} stays because Firmalife grape seeds are not
 * crop blocks (they plant on grape strings). Beneath nether crops extend
 * {@code CropBlock}, so they are covered either way. Farmer's Delight crops
 * that only sit in {@code c:seeds} are excluded.
 */
public final class TrueFarmerAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/all_crops");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.discovered("seeds/"),
    InventoryCollectionSource.tfcFarmlandCrops()
  );

  private TrueFarmerAdvancement() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancement.patch(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      SOURCES
    );
  }
}
