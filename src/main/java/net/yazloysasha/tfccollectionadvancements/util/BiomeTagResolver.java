package net.yazloysasha.tfccollectionadvancements.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class BiomeTagResolver {

  private BiomeTagResolver() {}

  public static List<ResourceLocation> resolve(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    TagKey<Biome> tag
  ) {
    var biomeRegistry = registries.lookupOrThrow(Registries.BIOME);
    Function<ResourceLocation, Optional<? extends ResourceLocation>> idToValue =
      id -> {
        ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, id);
        return biomeRegistry.get(key).isPresent()
          ? Optional.of(id)
          : Optional.empty();
      };

    Map<ResourceLocation, Collection<ResourceLocation>> tags =
      ResilientTagLoader.loadAndBuild(
        resourceManager,
        idToValue,
        Registries.tagsDirPath(Registries.BIOME)
      );
    Collection<ResourceLocation> members = tags.get(tag.location());
    if (members == null || members.isEmpty()) {
      return List.of();
    }
    return List.copyOf(members);
  }
}
