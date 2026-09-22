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
 * Patches TFC Healthy Diet ({@code tfc:world/fruit}).
 * <p>
 * Base TFC uses {@code minecraft:consume_item}. Its JSON list is hand-maintained
 * and misses entries (for example {@code melon_slice} is fruit in {@code
 * c:foods/fruit} but not in the advancement). We add every {@code tfc} and addon
 * item in {@code c:foods/fruit} that is not already a criterion — one tag, no
 * special cases.
 */
public final class HealthyDietAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/fruit");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.discoveredTag(Tags.Items.FOODS_FRUIT)
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
