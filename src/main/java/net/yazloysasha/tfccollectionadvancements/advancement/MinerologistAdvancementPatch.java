package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Patches TFC Minerologist ({@code tfc:world/minerologist}).
 * <p>
 * TFC description is every non-metal mineral. That includes gems; the JSON list
 * {@code ALL_MINERALS} omits pyrite, ruby, sapphire, and topaz and duplicates
 * {@code sulfur} in {@code requirements}. Missing TFC pieces are filled from
 * held {@code ore/{name}} items that are not metal ores, not from a hardcoded
 * gem list; duplicates are removed when the advancement is patched. Addon
 * minerals and items such as Beneath cursecoal are included the same way.
 */
public final class MinerologistAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/minerologist");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.mineralPieces(),
    new InventoryCollectionSource("beneath", "cursecoal")
  );

  private MinerologistAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancementPatch.patch(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      SOURCES
    );
  }
}
