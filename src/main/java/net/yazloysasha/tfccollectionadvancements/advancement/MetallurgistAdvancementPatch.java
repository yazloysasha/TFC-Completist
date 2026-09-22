package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancementPatch;

/**
 * Patches TFC Metallurgist ({@code tfc:world/metallurgist}).
 * <p>
 * A TFC metal is anything in {@code tfc:molten_metals}. The criterion is the
 * matching ingot from
 * {@code metal/ingot/{metal}} or {@code c:ingots/{metal}}.
 */
public final class MetallurgistAdvancementPatch {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/metallurgist");

  private MetallurgistAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancementPatch.patchIngotsFromMoltenMetals(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries
    );
  }
}
