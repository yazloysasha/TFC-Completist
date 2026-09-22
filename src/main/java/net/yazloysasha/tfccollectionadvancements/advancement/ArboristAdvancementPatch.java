package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Patches TFC Arborist ({@code tfc:world/saplings}).
 * <p>
 * In base TFC this is a <em>timber</em> collection: obtain every lumber-tree sapling. The
 * in-game text refers to non-fruit trees; fruit-tree content belongs under {@link
 * HealthyDietAdvancementPatch} (food), not here. Criteria use {@code minecraft:inventory_changed}
 * with one item id per wood species ({@code .../wood/sapling/{species}}).
 * <p>
 * An addon entry belongs here only if its sapling item matches that role <em>and</em> the same
 * id layout ({@code wood/sapling/...}). Sapling-like items under {@code plant/...}, crop seeds, or
 * other paths are different systems even when the display name says “sapling”.
 * <p>
 * Use {@link InventoryCollectionSource} path prefixes that mirror TFC; avoid prefix rules that
 * also match leaves, fruit, or bush blocks. Criteria are created only for items present in the
 * loaded registries when advancements reload.
 */
public final class ArboristAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/saplings");

  private static final String SAPLING_PATH_PREFIX = "wood/sapling/";

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    new InventoryCollectionSource("afc", SAPLING_PATH_PREFIX),
    new InventoryCollectionSource("beneath", SAPLING_PATH_PREFIX)
  );

  private ArboristAdvancementPatch() {}

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
