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
import net.minecraft.world.item.Item;

/**
 * Item tags are not bound to holders yet when {@code ServerAdvancementManager}
 * applies JSON, so {@code Holder#is(TagKey)} is always false there. Load the
 * datapack tag files the same way biome patches do.
 */
public final class ItemTagResolver {

  private ItemTagResolver() {}

  public static Set<ResourceLocation> resolve(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    TagKey<Item> tag
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
    var itemRegistry = registries.lookupOrThrow(Registries.ITEM);
    TagLoader<ResourceLocation> loader = new TagLoader<>(
      id -> {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return itemRegistry.get(key).isPresent()
          ? Optional.of(id)
          : Optional.empty();
      },
      Registries.tagsDirPath(Registries.ITEM)
    );
    return loader.loadAndBuild(resourceManager);
  }
}
