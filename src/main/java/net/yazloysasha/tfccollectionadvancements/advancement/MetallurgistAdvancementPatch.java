package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Patches TFC Metallurgist ({@code tfc:world/metallurgist}).
 * <p>
 * TFC tracks <em>finished metal identity</em>, not ore in the ground: {@code
 * minecraft:inventory_changed} on {@code .../metal/ingot/{metal}} for each smeltable metal the mod
 * adds to the progression. Ore pieces, nuggets, anvil parts, sheets, and fluid buckets are
 * out of scope even if they belong to the same material.
 * <p>
 * For addons that introduce new metals in the TFC metal system, add criteria for ingot items
 * only ({@code metal/ingot/...}). A namespace-wide ingot prefix is safe when every ingot under it
 * is a distinct metal row in this advancement. Smeltable ore belongs under {@link
 * MinerologistAdvancementPatch} only when it is collected as a non-metallic mineral in TFC’s
 * sense (usually it is not).
 */
public final class MetallurgistAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/metallurgist");

  private static final String INGOT_PATH_PREFIX = "metal/ingot/";

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    new InventoryCollectionSource("firmalife", INGOT_PATH_PREFIX)
  );

  private MetallurgistAdvancementPatch() {}

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
