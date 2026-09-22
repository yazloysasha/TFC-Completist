package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Patches TFC Healthy Diet ({@code tfc:world/fruit}).
 * <p>
 * This advancement is <em>not</em> inventory-based: TFC uses {@code minecraft:consume_item} with
 * a single {@code item} predicate pointing at {@code .../food/{name}}. Addon patches must use
 * {@link InventoryCollectionAdvancementPatch#patchConsume} so triggers stay compatible; inventory
 * criteria would never complete here.
 * <p>
 * Qualifying content is raw (or normally eaten) tree fruit and bush berries — the same category
 * as TFC’s {@code tfc:food/*} fruit entries. Exclude vegetables, mushrooms, meat, drinks,
 * inedible recipe intermediates ({@code raw_*}, dough, jam, preserves, jarred goods), and
 * anything where eating is not the intended player action. Prefer {@code exactPath} on {@code
 * food/...} ids: a wide {@code food/} prefix almost always pulls in non-fruit items from the same
 * namespace.
 */
public final class HealthyDietAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/fruit");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    new InventoryCollectionSource("firmalife", "food/fig", null, true),
    new InventoryCollectionSource(
      "firmalife",
      "food/nightshade_berry",
      null,
      true
    ),
    new InventoryCollectionSource("firmalife", "food/pineapple", null, true),
    new InventoryCollectionSource("firmalife", "food/red_grapes", null, true),
    new InventoryCollectionSource("firmalife", "food/white_grapes", null, true)
  );

  private HealthyDietAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancementPatch.patchConsume(
      ADVANCEMENT,
      advancements,
      registries,
      SOURCES
    );
  }
}
