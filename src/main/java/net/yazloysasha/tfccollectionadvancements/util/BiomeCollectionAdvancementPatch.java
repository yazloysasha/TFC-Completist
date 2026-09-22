package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.ModList;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class BiomeCollectionAdvancementPatch {

  private BiomeCollectionAdvancementPatch() {}

  public static void patch(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
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

    var biomeRegistry = registries.lookupOrThrow(Registries.BIOME);

    for (BiomeCollectionSource source : sources) {
      if (!ModList.get().isLoaded(source.modId())) {
        continue;
      }

      var taggedBiomes = biomeRegistry.get(source.tag());
      if (taggedBiomes.isEmpty()) {
        continue;
      }

      int added = 0;
      for (Holder<Biome> holder : taggedBiomes.get()) {
        ResourceLocation biomeId = holder.unwrapKey().orElseThrow().location();
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
          source.modId()
        );
      }
    }
  }
}
