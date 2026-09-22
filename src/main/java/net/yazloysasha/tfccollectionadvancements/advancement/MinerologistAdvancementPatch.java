package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Patches TFC Minerologist ({@code tfc:world/minerologist}), chained after {@code
 * tfc:world/gemologist} in the world tab.
 * <p>
 * Each vanilla criterion is {@code minecraft:inventory_changed} for one <em>logical mineral
 * type</em>: a single item id such as {@code .../ore/{name}} with no rock suffix and no poor/normal/rich
 * grade split. That checklist covers non-smelted geological loot (salts, coals, sulfur, gems also
 * listed here, etc.), not metals you track as ingots — those belong under {@link
 * MetallurgistAdvancementPatch}.
 * <p>
 * Addon minerals fit when the player collects one representative item per type, using the same
 * id shape as TFC ({@code ore/...} or a lone item id if the mod does not use {@code ore/}). Do not
 * add graded ore drops ({@code ore/{grade}_{ore}/{rock}} block items), every rock variant, or
 * smelting products. Use {@code exactPath} when a path prefix would also match block-item
 * variants; see {@link InventoryCollectionSource#exactPath()}.
 */
public final class MinerologistAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/minerologist");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    new InventoryCollectionSource("beneath", "cursecoal")
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
