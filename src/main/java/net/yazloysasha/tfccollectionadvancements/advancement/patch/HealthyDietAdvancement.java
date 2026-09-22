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
 * Rebuilds TFC Healthy Diet ({@code tfc:world/fruit}).
 * <p>
 * Every {@code tfc} and addon item in {@code c:foods/fruit}. Base TFC uses
 * {@code minecraft:consume_item}. Item tags are read from datapacks at
 * advancement reload (holders are not tagged yet).
 */
public final class HealthyDietAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/fruit");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.discoveredTag(Tags.Items.FOODS_FRUIT)
  );

  private HealthyDietAdvancement() {}

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

    JsonElement advancement = advancements.get(ADVANCEMENT);
    if (advancement != null && advancement.isJsonObject()) {
      AdvancementCriterionBuilder.setFrame(
        advancement.getAsJsonObject(),
        "goal"
      );
    }
  }
}
