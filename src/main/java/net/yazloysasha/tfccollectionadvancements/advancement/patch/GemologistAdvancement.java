package net.yazloysasha.tfccollectionadvancements.advancement.patch;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancement;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Rebuilds TFC Gemologist ({@code tfc:world/gemologist}).
 * <p>
 * Held gem ore pieces ({@code ore/{name}} with a matching {@code gem/{name}}).
 */
public final class GemologistAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/gemologist");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.gemPieces()
  );

  private GemologistAdvancement() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancement.patch(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      SOURCES
    );
  }
}
