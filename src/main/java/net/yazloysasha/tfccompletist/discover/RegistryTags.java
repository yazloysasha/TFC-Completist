package net.yazloysasha.tfccompletist.discover;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;

/**
 * Registry tags are not bound to holders yet when {@code ServerAdvancementManager}
 * applies JSON. Load datapack tag files with {@link DatapackTags}.
 */
public final class RegistryTags {

  private RegistryTags() {}

  public static <T> Set<ResourceLocation> resolveSet(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    TagKey<T> tag
  ) {
    return copyAsSet(tagMembers(resourceManager, registries, tag));
  }

  public static <T> List<ResourceLocation> resolveList(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    TagKey<T> tag
  ) {
    return copyAsList(tagMembers(resourceManager, registries, tag));
  }

  private static <T> Collection<ResourceLocation> tagMembers(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    TagKey<T> tag
  ) {
    Collection<ResourceLocation> members = loadAll(
      resourceManager,
      registries,
      tag.registry()
    ).get(tag.location());
    return members == null ? List.of() : members;
  }

  private static Set<ResourceLocation> copyAsSet(
    Collection<ResourceLocation> members
  ) {
    return members.isEmpty() ? Set.of() : Set.copyOf(members);
  }

  private static List<ResourceLocation> copyAsList(
    Collection<ResourceLocation> members
  ) {
    return members.isEmpty() ? List.of() : List.copyOf(members);
  }

  public static <T> Map<ResourceLocation, Collection<ResourceLocation>> loadAll(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    ResourceKey<? extends Registry<T>> registryKey
  ) {
    var registry = registries.lookupOrThrow(registryKey);
    Function<ResourceLocation, Optional<? extends ResourceLocation>> idToValue =
      id -> {
        ResourceKey<T> key = ResourceKey.create(registryKey, id);
        return registry.get(key).isPresent()
          ? Optional.of(id)
          : Optional.empty();
      };
    return DatapackTags.loadAndBuild(
      resourceManager,
      idToValue,
      net.minecraft.core.registries.Registries.tagsDirPath(registryKey)
    );
  }
}
