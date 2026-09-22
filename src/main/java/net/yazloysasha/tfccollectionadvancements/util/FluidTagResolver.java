package net.yazloysasha.tfccollectionadvancements.util;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.material.Fluid;

/**
 * Fluid tags are not bound to holders yet when {@code ServerAdvancementManager}
 * applies JSON. Load datapack tag files the same way item patches do.
 */
public final class FluidTagResolver {

  private FluidTagResolver() {}

  public static Set<ResourceLocation> resolve(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    TagKey<Fluid> tag
  ) {
    Collection<ResourceLocation> members = loadAll(
      resourceManager,
      registries
    ).get(tag.location());
    if (members == null || members.isEmpty()) {
      return Set.of();
    }
    return Set.copyOf(members);
  }

  public static Map<ResourceLocation, Collection<ResourceLocation>> loadAll(
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    var fluidRegistry = registries.lookupOrThrow(Registries.FLUID);
    TagLoader<ResourceLocation> loader = new TagLoader<>(
      id -> {
        ResourceKey<Fluid> key = ResourceKey.create(Registries.FLUID, id);
        return fluidRegistry.get(key).isPresent()
          ? Optional.of(id)
          : Optional.empty();
      },
      Registries.tagsDirPath(Registries.FLUID)
    );
    return loader.loadAndBuild(resourceManager);
  }
}
