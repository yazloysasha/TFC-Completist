package net.yazloysasha.tfccollectionadvancements.advancement.patch;

import com.google.gson.JsonElement;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.util.AdvancementCriterionBuilder;
import net.yazloysasha.tfccollectionadvancements.util.InventoryCollectionAdvancement;

/**
 * Rebuilds TFC Metallurgist ({@code tfc:world/metallurgist}).
 * <p>
 * A TFC metal is anything in {@code tfc:molten_metals}. The criterion is the
 * matching ingot from
 * {@code metal/ingot/{metal}} or {@code c:ingots/{metal}}.
 */
public final class MetallurgistAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/metallurgist");

  private MetallurgistAdvancement() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    InventoryCollectionAdvancement.patchIngotsFromMoltenMetals(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries
    );

    JsonElement advancement = advancements.get(ADVANCEMENT);
    if (advancement != null && advancement.isJsonObject()) {
      AdvancementCriterionBuilder.setFrame(
        advancement.getAsJsonObject(),
        "challenge"
      );
    }
  }
}
