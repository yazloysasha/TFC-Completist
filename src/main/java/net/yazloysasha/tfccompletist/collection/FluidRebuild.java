package net.yazloysasha.tfccompletist.collection;

import com.google.gson.JsonElement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.yazloysasha.tfccompletist.discover.Namespaces;
import net.yazloysasha.tfccompletist.discover.RegistryTags;

public final class FluidRebuild {

  private FluidRebuild() {}

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
    Set<ResourceLocation> fluids = new LinkedHashSet<>();
    for (TagKey<Fluid> tag : tags) {
      for (ResourceLocation fluidId : RegistryTags.resolveList(
        resourceManager,
        registries,
        tag
      )) {
        if (
          Namespaces.isDiscoverable(fluidId.getNamespace()) &&
          !isFlowingFluidPath(fluidId.getPath())
        ) {
          fluids.add(fluidId);
        }
      }
    }

    RebuildJson.rebuildIdCriteria(
      advancementId,
      advancements,
      fluids,
      "fluid",
      "drink-fluid",
      true,
      RebuildJson::pathCriterion,
      CriterionJson::drinkFluid
    );
  }

  static boolean isFlowingFluidPath(String path) {
    return path.startsWith("flowing_") || path.endsWith("_flowing");
  }
}
