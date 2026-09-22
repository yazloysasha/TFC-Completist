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
import net.minecraft.world.level.block.Block;

/**
 * Block tags are not bound to holders yet when {@code ServerAdvancementManager}
 * applies JSON. Load datapack tag files the same way item patches do.
 */
public final class BlockTagResolver {

  private BlockTagResolver() {}

  public static Set<ResourceLocation> resolve(
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    TagKey<Block> tag
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
    var blockRegistry = registries.lookupOrThrow(Registries.BLOCK);
    TagLoader<ResourceLocation> loader = new TagLoader<>(
      id -> {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        return blockRegistry.get(key).isPresent()
          ? Optional.of(id)
          : Optional.empty();
      },
      Registries.tagsDirPath(Registries.BLOCK)
    );
    return loader.loadAndBuild(resourceManager);
  }
}
