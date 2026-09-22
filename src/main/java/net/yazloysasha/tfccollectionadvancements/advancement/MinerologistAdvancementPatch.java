package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionSource;

/**
 * Patches TFC Minerologist ({@code tfc:world/minerologist}).
 * <p>
 * Collects addon-held mineral pieces ({@code ore/{name}} items, not rock-ore
 * block items or graded metal ores). Unique items such as Beneath cursecoal
 * that do not use the {@code ore/} prefix are still matched by id. Gems go to
 * {@link GemologistAdvancementPatch}.
 */
public final class MinerologistAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/minerologist");

  private static final List<InventoryCollectionSource> SOURCES = List.of(
    InventoryCollectionSource.addonOrePieces(false),
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
