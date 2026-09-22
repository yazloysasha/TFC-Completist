package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class BiomeCollectionAdvancement {

  private BiomeCollectionAdvancement() {}

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

    int resolvedCriteria = countResolvedBiomeCriteria(
      resourceManager,
      registries,
      sources
    );
    if (
      CollectionAdvancementRebuildGuard.shouldSkipRebuild(
        advancementId,
        resolvedCriteria,
        "biome"
      )
    ) {
      return;
    }

    AdvancementCriterionBuilder.resetCollection(root);
    criteria = root.getAsJsonObject("criteria");
    requirements = root.getAsJsonArray("requirements");

    for (BiomeCollectionSource source : sources) {
      int added = 0;
      for (ResourceLocation biomeId : matchingBiomes(
        resourceManager,
        registries,
        source
      )) {
        String criterionName = criterionName(biomeId);
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
          "Rebuilt {} with {} {} biome criteria",
          advancementId,
          added,
          source.displayNamespace()
        );
      }
    }

    CollectionAdvancementDeduplicator.deduplicate(root);
  }

  private static int countResolvedBiomeCriteria(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<BiomeCollectionSource> sources
  ) {
    Set<String> criterionNames = new HashSet<>();
    for (BiomeCollectionSource source : sources) {
      for (ResourceLocation biomeId : matchingBiomes(
        resourceManager,
        registries,
        source
      )) {
        criterionNames.add(criterionName(biomeId));
      }
    }
    return criterionNames.size();
  }

  private static List<ResourceLocation> matchingBiomes(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    BiomeCollectionSource source
  ) {
    if (
      source.namespace() != null &&
      !AddonNamespaces.isPresent(registries, source.namespace())
    ) {
      return List.of();
    }
    List<ResourceLocation> biomeIds = source.tag() == null
      ? registries
        .lookupOrThrow(Registries.BIOME)
        .listElementIds()
        .map(ResourceKey::location)
        .toList()
      : RegistryTagResolver.resolveList(
        resourceManager,
        registries,
        source.tag()
      );
    return biomeIds.stream().filter(id -> matchesBiome(source, id)).toList();
  }

  private static boolean matchesBiome(
    BiomeCollectionSource source,
    ResourceLocation biomeId
  ) {
    if (source.biomeNamespace() != null) {
      return source.biomeNamespace().equals(biomeId.getNamespace());
    }
    return AddonNamespaces.isDiscoverable(biomeId.getNamespace());
  }

  private static String criterionName(ResourceLocation biomeId) {
    return AddonNamespaces.isTfc(biomeId.getNamespace())
      ? biomeId.getPath()
      : biomeId.getNamespace() + "_" + biomeId.getPath();
  }
}
