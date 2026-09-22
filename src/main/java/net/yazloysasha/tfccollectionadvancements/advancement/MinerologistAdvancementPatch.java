package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Patches TFC Minerologist ({@code tfc:world/minerologist}), child of {@link
 * GemologistAdvancementPatch} in the world tab. Base criteria are {@code
 * minecraft:inventory_changed} on non-metal mineral ores ({@code tfc:ore/...}). Smelted metals belong
 * under {@link MetallurgistAdvancementPatch}. Use {@code exactPath} when a prefix would also match
 * graded or block-item variants; see {@link InventoryCollectionSource#exactPath()}.
 */
public final class MinerologistAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/minerologist");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    new InventoryCollectionSource("beneath", "cursecoal"),
    new InventoryCollectionSource("tfc", "ore/pyrite", null, true),
    new InventoryCollectionSource("tfc", "ore/ruby", null, true),
    new InventoryCollectionSource("tfc", "ore/sapphire", null, true),
    new InventoryCollectionSource("tfc", "ore/topaz", null, true)
  );

  private MinerologistAdvancementPatch() {}

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
