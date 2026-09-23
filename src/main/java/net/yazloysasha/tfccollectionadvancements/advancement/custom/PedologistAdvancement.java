package net.yazloysasha.tfccollectionadvancements.advancement.custom;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancement;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Rebuilds Pedologist ({@code tfc_collection_advancements:world/pedologist}).
 * <p>
 * One AND-group per soil order (entisol, aridisol, ...). Dirt, grass, duff, clay,
 * clay duff, mud, and coarse dirt of the same order are OR'd.
 */
public final class PedologistAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/pedologist"
    );

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.discovered("dirt/"),
    InventoryCollectionSource.discovered("grass/"),
    InventoryCollectionSource.discovered("duff/"),
    InventoryCollectionSource.discovered("clay/"),
    InventoryCollectionSource.discovered("clay_duff/"),
    InventoryCollectionSource.discovered("mud/"),
    InventoryCollectionSource.discovered("coarse_dirt/")
  );

  private PedologistAdvancement() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancement.patchAnyOfByLastPathSegment(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      SOURCES
    );
  }
}
