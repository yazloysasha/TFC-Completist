package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Patches TFC Gemologist ({@code tfc:world/gemologist}).
 * <p>
 * Held gem ore pieces ({@code ore/{name}} with a matching {@code gem/{name}}).
 * Includes TFC so a gem missing from the JSON is still required.
 */
public final class GemologistAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/gemologist");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.gemPieces()
  );

  private GemologistAdvancementPatch() {}

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
