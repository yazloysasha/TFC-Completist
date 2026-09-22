package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;
import net.yazloysasha.tfccollectionadvancements.util.FluidCollectionAdvancementPatch;

/**
 * Rebuilds Sommelier ({@code tfc_collection_advancements:world/sommelier}).
 * <p>
 * Every discoverable still fluid in {@code #tfc:alcohols} and
 * {@code #c:alcohols}. Drinking is tracked through TFC's {@code Drinkable}
 * API, so any container or world sip that actually consumes the fluid counts
 * (jug, glass bottle, source block, addon vessels that call
 * {@code Drinkable.onDrink}). Farmer's Delight drinks stay out unless they
 * are registered as alcohol fluids. Parent is {@code tfc:world/root}.
 */
public final class SommelierAdvancementPatch {

  private static final TagKey<Fluid> COMMON_ALCOHOLS = TagKey.create(
    Registries.FLUID,
    ResourceLocation.fromNamespaceAndPath("c", "alcohols")
  );

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/sommelier"
    );

  private SommelierAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    FluidCollectionAdvancementPatch.patchDrinkFromTags(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      List.of(TFCTags.Fluids.ALCOHOLS, COMMON_ALCOHOLS)
    );
  }
}
