package net.yazloysasha.tfccompletist.collection;

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
import net.yazloysasha.tfccompletist.discover.Namespaces;
import net.yazloysasha.tfccompletist.discover.RegistryTags;

public final class EntityRebuild {

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

  private EntityRebuild() {}

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
      CriterionJson::familiarizedAnimal,
      "familiarized-animal"
    );
  }

  /**
   * Rebuilds {@code minecraft:player_killed_entity} criteria for every
   * discoverable member of {@code tags}.
   */
  public static void patchKilledEntityFromTags(
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
      Set.of(),
      CriterionJson::playerKilledEntity,
      "killed-entity"
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
      CriterionJson::bredAnimal,
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
    Set<ResourceLocation> entities = new LinkedHashSet<>();
    for (TagKey<EntityType<?>> tag : tags) {
      for (ResourceLocation entityId : RegistryTags.resolveList(
        resourceManager,
        registries,
        tag
      )) {
        if (
          Namespaces.isDiscoverable(entityId.getNamespace()) &&
          !excluded.contains(entityId)
        ) {
          entities.add(entityId);
        }
      }
    }

    RebuildJson.rebuildIdCriteria(
      advancementId,
      advancements,
      entities,
      "entity",
      kind,
      true,
      RebuildJson::lastSegmentCriterion,
      criterionFactory
    );
  }
}
