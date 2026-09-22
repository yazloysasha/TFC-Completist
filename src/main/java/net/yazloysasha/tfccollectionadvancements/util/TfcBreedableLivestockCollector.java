package net.yazloysasha.tfccollectionadvancements.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.dries007.tfc.common.entities.livestock.Age;
import net.dries007.tfc.common.entities.livestock.CommonAnimalBehavior;
import net.dries007.tfc.common.entities.livestock.Gender;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

/**
 * Like {@link InventoryCollectionAdvancement} scanning {@code CropBlock} for
 * plantable seeds, resolves Husbandman criteria by simulating TFC mating on a
 * scratch pair of entities ({@code canMate} / {@code checkExtraBreedConditions}).
 */
public final class TfcBreedableLivestockCollector {

  private TfcBreedableLivestockCollector() {}

  public static Set<ResourceLocation> collectFromTags(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<TagKey<EntityType<?>>> tags
  ) {
    Set<ResourceLocation> fromTags = new LinkedHashSet<>();
    for (TagKey<EntityType<?>> tag : tags) {
      for (ResourceLocation entityId : RegistryTagResolver.resolveList(
        resourceManager,
        registries,
        tag
      )) {
        if (AddonNamespaces.isDiscoverable(entityId.getNamespace())) {
          fromTags.add(entityId);
        }
      }
    }

    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    if (server == null) {
      TFCCollectionAdvancements.LOGGER.warn(
        "No server during advancement reload; skipping TFC breed simulation (Husbandman)"
      );
      return Set.of();
    }

    ServerLevel level = server.overworld();
    var entityRegistry = registries.lookupOrThrow(Registries.ENTITY_TYPE);
    Set<ResourceLocation> breedable = new LinkedHashSet<>();
    for (ResourceLocation entityId : fromTags) {
      ResourceKey<EntityType<?>> key = ResourceKey.create(
        Registries.ENTITY_TYPE,
        entityId
      );
      entityRegistry
        .get(key)
        .ifPresent(holder -> {
          if (simulatesBreeding(holder.value(), level)) {
            breedable.add(entityId);
          }
        });
    }
    return breedable;
  }

  static boolean simulatesBreeding(EntityType<?> type, ServerLevel level) {
    Entity first = type.create(level);
    Entity second = type.create(level);
    if (first == null || second == null) {
      return false;
    }

    if (
      first instanceof CommonAnimalBehavior firstAnimal &&
      second instanceof CommonAnimalBehavior secondAnimal
    ) {
      prepareToMate(firstAnimal);
      prepareToMate(secondAnimal);
      firstAnimal.setGender(Gender.MALE);
      secondAnimal.setGender(Gender.FEMALE);
    }

    if (first instanceof Animal male && second instanceof Animal female) {
      if (male.canMate(female) || female.canMate(male)) {
        return true;
      }
    }

    if (
      first instanceof TFCAnimalProperties firstProps &&
      second instanceof TFCAnimalProperties secondProps
    ) {
      return (
        firstProps.checkExtraBreedConditions(secondProps) &&
        secondProps.checkExtraBreedConditions(firstProps)
      );
    }

    return false;
  }

  private static void prepareToMate(CommonAnimalBehavior animal) {
    animal.setFamiliarity(1.0f);
    animal.setLastFedNow();
    animal.setFertilized(false);
    animal.setLastAge(Age.ADULT);
  }
}
