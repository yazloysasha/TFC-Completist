package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class EntityCollectionAdvancement {

  private static final ResourceLocation TFC_FROG =
    ResourceLocation.fromNamespaceAndPath("tfc", "frog");

  private static final ResourceLocation TFC_MULE =
    ResourceLocation.fromNamespaceAndPath("tfc", "mule");

  /**
   * No familiarity cap in TFC; omitted from collection advancements.
   */
  private static final Set<ResourceLocation> FAMILIARIZED_EXCLUDED = Set.of(
    TFC_FROG
  );

  /**
   * Sterile or non-standard breeding.
   */
  private static final Set<ResourceLocation> BRED_ANIMAL_EXCLUDED = Set.of(
    TFC_MULE,
    TFC_FROG
  );

  public static boolean isExcludedFromFamiliarization(
    ResourceLocation entityId
  ) {
    return FAMILIARIZED_EXCLUDED.contains(entityId);
  }

  public static boolean isExcludedFromBreeding(ResourceLocation entityId) {
    return BRED_ANIMAL_EXCLUDED.contains(entityId);
  }

  private EntityCollectionAdvancement() {}

  /**
   * Rebuilds {@code familiarized_animal} criteria for tag members that reach
   * adult familiarity cap (or 100% as a child).
   */
  public static void patchFamiliarizedAnimalFromTags(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<TagKey<EntityType<?>>> tags
  ) {
    patchFromTags(
      advancementId,
      advancements,
      resourceManager,
      registries,
      tags,
      FAMILIARIZED_EXCLUDED,
      AdvancementCriterionBuilder::familiarizedAnimal,
      "familiarized-animal"
    );
  }

  /**
   * Rebuilds {@code bred_animal} criteria for every discoverable entity in
   * {@code tags}. Progress is this mod's trigger from {@code onFertilized}.
   */
  public static void patchBredAnimalFromTags(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<TagKey<EntityType<?>>> tags
  ) {
    patchFromTags(
      advancementId,
      advancements,
      resourceManager,
      registries,
      tags,
      BRED_ANIMAL_EXCLUDED,
      AdvancementCriterionBuilder::bredAnimal,
      "bred-animal"
    );
  }

  private static void patchFromTags(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<TagKey<EntityType<?>>> tags,
    Set<ResourceLocation> excluded,
    Function<ResourceLocation, JsonObject> criterionFactory,
    String kind
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

    Set<ResourceLocation> entities = new LinkedHashSet<>();
    for (TagKey<EntityType<?>> tag : tags) {
      for (ResourceLocation entityId : RegistryTagResolver.resolveList(
        resourceManager,
        registries,
        tag
      )) {
        if (
          AddonNamespaces.isDiscoverable(entityId.getNamespace()) &&
          !excluded.contains(entityId)
        ) {
          entities.add(entityId);
        }
      }
    }

    if (
      CollectionAdvancementRebuildGuard.shouldSkipRebuild(
        advancementId,
        entities.size(),
        "entity"
      )
    ) {
      return;
    }

    AdvancementCriterionBuilder.resetCollection(root);
    criteria = root.getAsJsonObject("criteria");
    requirements = root.getAsJsonArray("requirements");

    int added = 0;
    for (ResourceLocation entityId : entities) {
      String criterionName = entityCriterionName(entityId);
      if (criteria.has(criterionName)) {
        continue;
      }
      AdvancementCriterionBuilder.addAndRequire(
        criteria,
        requirements,
        criterionName,
        criterionFactory.apply(entityId)
      );
      added++;
    }

    CollectionAdvancementDeduplicator.deduplicate(root);
    if (added > 0) {
      TFCCollectionAdvancements.LOGGER.info(
        "Rebuilt {} with {} {} criteria",
        advancementId,
        added,
        kind
      );
    }
  }

  static String entityCriterionName(ResourceLocation entityId) {
    String path = entityId.getPath();
    int slash = path.lastIndexOf('/');
    String suffix = slash >= 0 ? path.substring(slash + 1) : path;
    return AddonNamespaces.isTfc(entityId.getNamespace())
      ? suffix
      : entityId.getNamespace() + "_" + suffix;
  }
}
