package net.yazloysasha.tfccollectionadvancements.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public record BiomeCollectionSource(
  String modId,
  String biomeNamespace,
  TagKey<Biome> tag
) {
  public String criterionPrefix() {
    return biomeNamespace + "_";
  }
}
