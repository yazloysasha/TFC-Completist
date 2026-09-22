package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.Tags;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Patches TFC True Farmer ({@code tfc:world/all_crops}).
 * <p>
 * Pulls every addon seed that follows TFC’s {@code seeds/...} id layout or is
 * tagged {@code c:seeds}. A newly added crop from any loaded addon is picked up
 * on advancement reload without a hardcoded item list.
 */
public final class TrueFarmerAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/all_crops");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.anyAddon("seeds/"),
    InventoryCollectionSource.addonTag(Tags.Items.SEEDS)
  );

  private TrueFarmerAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancementPatch.patch(
      ADVANCEMENT,
      advancements,
      registries,
      SOURCES
    );
  }
}
