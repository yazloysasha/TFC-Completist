package net.yazloysasha.tfccollectionadvancements.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * {@code c:foods/*} tags used when NeoForge {@code Tags.Items} has no constant.
 */
public final class CommonFoodTags {

  public static final TagKey<Item> SOUP = TagKey.create(
    Registries.ITEM,
    ResourceLocation.fromNamespaceAndPath("c", "foods/soup")
  );

  public static final TagKey<Item> SALAD = TagKey.create(
    Registries.ITEM,
    ResourceLocation.fromNamespaceAndPath("c", "foods/salad")
  );

  private CommonFoodTags() {}
}
