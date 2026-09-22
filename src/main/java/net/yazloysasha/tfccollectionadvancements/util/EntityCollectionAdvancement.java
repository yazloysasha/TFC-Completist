package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class EntityCollectionAdvancement {

  private EntityCollectionAdvancement() {}

  /**
   * Rebuilds {@code tfc:fed_animal} criteria for every discoverable entity in
   * {@code tags}. TFC fires that trigger only when feeding actually raises
   * familiarity ({@code TFCAnimalProperties#eatFood}), including pets that
   * extend {@code TamableMammal}.
   */
  public static void patchFedAnimalFromTags(
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
      entityId -> true,
      AdvancementCriterionBuilder::fedAnimal,
      "fed-animal"
    );
  }

  /**
   * Rebuilds {@code bred_animal} criteria for tag members that pass TFC breed
   * simulation ({@link TfcBreedableLivestockCollector}), same idea as True
   * Farmer scanning {@code CropBlock} for seeds.
   */
  public static void patchBredAnimalFromTags(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<TagKey<EntityType<?>>> tags
  ) {
    Set<ResourceLocation> breedable =
      TfcBreedableLivestockCollector.collectFromTags(
        resourceManager,
        registries,
        tags
      );
    patchFromTags(
      advancementId,
      advancements,
      resourceManager,
      registries,
      tags,
      breedable::contains,
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
    Predicate<ResourceLocation> include,
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
          include.test(entityId)
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
