package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

public final class OreCollectionAdvancementPatch {

  public static final ResourceLocation TFC_MINERALOGIST_ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/minerologist");

  private static final List<InventoryCollectionSource> MINERALOGIST_SOURCES =
    List.of(new InventoryCollectionSource("beneath", "cursecoal"));

  private OreCollectionAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancementPatch.patch(
      TFC_MINERALOGIST_ADVANCEMENT,
      advancements,
      registries,
      MINERALOGIST_SOURCES
    );
  }
}
