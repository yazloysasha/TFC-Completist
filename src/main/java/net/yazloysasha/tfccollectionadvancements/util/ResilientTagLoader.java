package net.yazloysasha.tfccollectionadvancements.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;

/**
 * Vanilla {@link TagLoader#loadAndBuild} drops an entire tag when any required
 * entry is missing. Broken addon tags (for example MekaTFC galena on TFC 4.2)
 * therefore empty {@code #c:ores} and {@code tfc:prospectable}. Skip invalid
 * entries instead of failing the whole tag.
 */
public final class ResilientTagLoader {

  private ResilientTagLoader() {}

  public static <T> Map<ResourceLocation, Collection<T>> loadAndBuild(
    ResourceManager resourceManager,
    Function<ResourceLocation, Optional<? extends T>> idToValue,
    String directory
  ) {
    TagLoader<T> loader = new TagLoader<>(idToValue, directory);
    Map<ResourceLocation, List<TagLoader.EntryWithSource>> raw = loader.load(
      resourceManager
    );
    return build(raw, idToValue);
  }

  static <T> Map<ResourceLocation, Collection<T>> build(
    Map<ResourceLocation, List<TagLoader.EntryWithSource>> raw,
    Function<ResourceLocation, Optional<? extends T>> idToValue
  ) {
    Map<ResourceLocation, Collection<T>> resolved = new HashMap<>();
    for (ResourceLocation tagId : raw.keySet()) {
      resolveTag(tagId, raw, idToValue, resolved, new LinkedHashSet<>());
    }
    return Map.copyOf(resolved);
  }

  private static <T> Collection<T> resolveTag(
    ResourceLocation tagId,
    Map<ResourceLocation, List<TagLoader.EntryWithSource>> raw,
    Function<ResourceLocation, Optional<? extends T>> idToValue,
    Map<ResourceLocation, Collection<T>> resolved,
    Set<ResourceLocation> visiting
  ) {
    Collection<T> cached = resolved.get(tagId);
    if (cached != null) {
      return cached;
    }
    if (!visiting.add(tagId)) {
      return List.of();
    }

    List<TagLoader.EntryWithSource> entries = raw.get(tagId);
    if (entries == null || entries.isEmpty()) {
      visiting.remove(tagId);
      Collection<T> empty = List.of();
      resolved.put(tagId, empty);
      return empty;
    }

    LinkedHashSet<T> builder = new LinkedHashSet<>();
    TagEntry.Lookup<T> lookup = new TagEntry.Lookup<>() {
      @Override
      @Nullable
      public T element(ResourceLocation id) {
        return idToValue.apply(id).orElse(null);
      }

      @Override
      @Nullable
      public Collection<T> tag(ResourceLocation id) {
        if (!raw.containsKey(id)) {
          return List.of();
        }
        return resolveTag(id, raw, idToValue, resolved, visiting);
      }
    };

    for (TagLoader.EntryWithSource entry : entries) {
      entry
        .entry()
        .build(lookup, entry.remove() ? builder::remove : builder::add);
    }

    visiting.remove(tagId);
    Collection<T> built = List.copyOf(builder);
    resolved.put(tagId, built);
    return built;
  }
}
