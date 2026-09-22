package net.yazloysasha.tfccollectionadvancements.advancement.patch;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.util.AdvancementCriterionBuilder;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancement;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Rebuilds TFC Minerologist ({@code tfc:world/minerologist}).
 * <p>
 * Criteria are unique item drops from {@code tfc:prospectable} blocks that are
 * not metal ores. That covers TFC mineral and gem pieces, Beneath cursecoal,
 * and any addon mineral whose ore block is prospectable — without hardcoding
 * item ids. TFC halite contributes salt, which is what the block actually
 * drops. Gems still belong here: TFC describes Minerologist as every
 * non-metal mineral.
 */
public final class MinerologistAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/minerologist");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.mineralOreDrops()
  );

  private MinerologistAdvancement() {}

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
        "challenge"
      );
    }
  }
}
