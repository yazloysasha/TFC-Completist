package net.yazloysasha.tfccollectionadvancements.collection;

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
import net.yazloysasha.tfccollectionadvancements.discover.Namespaces;
import net.yazloysasha.tfccollectionadvancements.discover.RegistryTags;

public final class BiomeRebuild {

  private BiomeRebuild() {}

  public static void patch(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<BiomeSource> sources
  ) {
    JsonObject root = RebuildJson.collectionRootOrNull(
      advancementId,
      advancements
    );
    if (root == null) {
      return;
    }

    int resolvedCriteria = countResolvedBiomeCriteria(
      resourceManager,
      registries,
      sources
    );
    if (
      RebuildJson.resetIfResolved(
        advancementId,
        root,
        resolvedCriteria,
        "biome"
      ) ==
      null
    ) {
      return;
    }

    JsonObject criteria = root.getAsJsonObject("criteria");
    JsonArray requirements = root.getAsJsonArray("requirements");

    for (BiomeSource source : sources) {
      int added = 0;
      for (ResourceLocation biomeId : matchingBiomes(
        resourceManager,
        registries,
        source
      )) {
        String criterionName = RebuildJson.pathCriterion(biomeId);
        if (criteria.has(criterionName)) {
          continue;
        }

        CriterionJson.addAndRequire(
          criteria,
          requirements,
          criterionName,
          CriterionJson.biomeLocation(biomeId)
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

    RebuildDeduplicator.deduplicate(root);
  }

  private static int countResolvedBiomeCriteria(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<BiomeSource> sources
  ) {
    Set<String> criterionNames = new HashSet<>();
    for (BiomeSource source : sources) {
      for (ResourceLocation biomeId : matchingBiomes(
        resourceManager,
        registries,
        source
      )) {
        criterionNames.add(RebuildJson.pathCriterion(biomeId));
      }
    }
    return criterionNames.size();
  }

  private static List<ResourceLocation> matchingBiomes(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    BiomeSource source
  ) {
    if (
      source.namespace() != null &&
      !Namespaces.isPresent(registries, source.namespace())
    ) {
      return List.of();
    }
    List<ResourceLocation> biomeIds = source.tag() == null
      ? registries
        .lookupOrThrow(Registries.BIOME)
        .listElementIds()
        .map(ResourceKey::location)
        .toList()
      : RegistryTags.resolveList(resourceManager, registries, source.tag());
    return biomeIds.stream().filter(id -> matchesBiome(source, id)).toList();
  }

  private static boolean matchesBiome(
    BiomeSource source,
    ResourceLocation biomeId
  ) {
    if (source.biomeNamespace() != null) {
      return source.biomeNamespace().equals(biomeId.getNamespace());
    }
    return Namespaces.isDiscoverable(biomeId.getNamespace());
  }
}
