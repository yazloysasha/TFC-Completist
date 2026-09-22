package net.yazloysasha.tfccollectionadvancements.advancement.patch;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.common.Tags;
import net.yazloysasha.tfccollectionadvancements.util.AdvancementCriterionBuilder;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancement;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Rebuilds TFC Gone Fishing ({@code tfc:world/all_fish}).
 * <p>
 * Raw fish tagged {@code c:foods/raw_fish}, including TFC.
 */
public final class GoneFishingAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/all_fish");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.discoveredTag(Tags.Items.FOODS_RAW_FISH)
  );

  private GoneFishingAdvancement() {}

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

    JsonElement advancement = advancements.get(ADVANCEMENT);
    if (advancement != null && advancement.isJsonObject()) {
      AdvancementCriterionBuilder.setFrame(
        advancement.getAsJsonObject(),
        "goal"
      );
    }
  }
}
