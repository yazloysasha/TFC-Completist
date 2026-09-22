package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class BiomeCollectionAdvancementPatch {

  private BiomeCollectionAdvancementPatch() {}

  public static void patch(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<BiomeCollectionSource> sources
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

    for (BiomeCollectionSource source : sources) {
      if (!AddonNamespaces.isPresent(registries, source.namespace())) {
        continue;
      }

      List<ResourceLocation> biomeIds = BiomeTagResolver.resolve(
        resourceManager,
        registries,
        source.tag()
      );

      int added = 0;
      for (ResourceLocation biomeId : biomeIds) {
        if (!source.biomeNamespace().equals(biomeId.getNamespace())) {
          continue;
        }

        String criterionName = source.criterionPrefix() + biomeId.getPath();
        if (criteria.has(criterionName)) {
          continue;
        }

        AdvancementCriterionBuilder.addAndRequire(
          criteria,
          requirements,
          criterionName,
          AdvancementCriterionBuilder.biomeLocation(biomeId)
        );
        added++;
      }

      if (added > 0) {
        TFCCollectionAdvancements.LOGGER.info(
          "Extended {} with {} {} biome criteria",
          advancementId,
          added,
          source.namespace()
        );
      }
    }
  }
}
