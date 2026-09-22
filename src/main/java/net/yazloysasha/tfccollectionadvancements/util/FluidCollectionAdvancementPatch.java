package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class FluidCollectionAdvancementPatch {

  private FluidCollectionAdvancementPatch() {}

  /**
   * Rebuilds drink-fluid criteria for every fluid in {@code tag}, including addon
   * fluids registered in datapacks (for example Firmalife wines in
   * {@code #tfc:alcohols}). Non-drinkable addon fluids are ignored unless they
   * are tagged.
   */
  public static void patchDrinkFromTags(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<TagKey<Fluid>> tags
  ) {
    JsonElement advancementElement = advancements.get(advancementId);
    if (advancementElement == null || !advancementElement.isJsonObject()) {
      return;
    }

    JsonObject root = advancementElement.getAsJsonObject();
    JsonObject criteria = root.getAsJsonObject("criteria");
    JsonArray requirements = root.getAsJsonArray("requirements");
    if (criteria == null || requirements == null) {
      return;
    }

    Set<ResourceLocation> fluids = new LinkedHashSet<>();
    for (TagKey<Fluid> tag : tags) {
      for (ResourceLocation fluidId : RegistryTagResolver.resolveList(
        resourceManager,
        registries,
        tag
      )) {
        if (
          AddonNamespaces.isDiscoverable(fluidId.getNamespace()) &&
          !isFlowingFluidPath(fluidId.getPath())
        ) {
          fluids.add(fluidId);
        }
      }
    }

    if (
      CollectionAdvancementRebuildGuard.shouldSkipRebuild(
        advancementId,
        fluids.size(),
        "fluid"
      )
    ) {
      return;
    }

    AdvancementCriterionBuilder.resetCollection(root);
    criteria = root.getAsJsonObject("criteria");
    requirements = root.getAsJsonArray("requirements");

    int added = 0;
    for (ResourceLocation fluidId : fluids) {
      String criterionName = fluidCriterionName(fluidId);
      if (criteria.has(criterionName)) {
        continue;
      }
      AdvancementCriterionBuilder.addAndRequire(
        criteria,
        requirements,
        criterionName,
        AdvancementCriterionBuilder.drinkFluid(fluidId)
      );
      added++;
    }

    CollectionAdvancementDeduplicator.deduplicate(root);
    if (added > 0) {
      TFCCollectionAdvancements.LOGGER.info(
        "Rebuilt {} with {} drink-fluid criteria",
        advancementId,
        added
      );
    }
  }

  public static void patchDrinkFromTag(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    TagKey<Fluid> tag
  ) {
    patchDrinkFromTags(
      advancementId,
      advancements,
      resourceManager,
      registries,
      List.of(tag)
    );
  }

  static String fluidCriterionName(ResourceLocation fluidId) {
    return AddonNamespaces.isTfc(fluidId.getNamespace())
      ? fluidId.getPath()
      : fluidId.getNamespace() + "_" + fluidId.getPath();
  }

  static boolean isFlowingFluidPath(String path) {
    return path.startsWith("flowing_") || path.endsWith("_flowing");
  }
}
