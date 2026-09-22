package net.yazloysasha.tfccollectionadvancements.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public record BiomeCollectionSource(
  String namespace,
  String biomeNamespace,
  TagKey<Biome> tag
) {
  public static BiomeCollectionSource discovered() {
    return new BiomeCollectionSource(null, null, null);
  }

  public String displayNamespace() {
    if (namespace != null) {
      return namespace;
    }
    if (biomeNamespace != null) {
      return biomeNamespace;
    }
    return "discovered";
  }
}
