package net.yazloysasha.tfccollectionadvancements.collection;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public record BiomeSource(
  String namespace,
  String biomeNamespace,
  TagKey<Biome> tag
) {
  public static BiomeSource discovered() {
    return new BiomeSource(null, null, null);
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
