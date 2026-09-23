package net.yazloysasha.tfccollectionadvancements.discover;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Food tags and item ids that NeoForge / TFC classify differently than this
 * mod's collection advancements. Soup and salad have no {@code Tags.Items}
 * constant. TFC lists {@code cooked_turtle} under seafood; meat collections
 * treat it as land meat.
 */
public final class Foods {

  public static final TagKey<Item> SOUP = TagKey.create(
    Registries.ITEM,
    ResourceLocation.fromNamespaceAndPath("c", "foods/soup")
  );

  public static final TagKey<Item> SALAD = TagKey.create(
    Registries.ITEM,
    ResourceLocation.fromNamespaceAndPath("c", "foods/salad")
  );

  public static final ResourceLocation COOKED_TURTLE =
    ResourceLocation.fromNamespaceAndPath("tfc", "food/cooked_turtle");

  public static final List<ResourceLocation> SEAFOOD_TAG_EXCLUSIONS = List.of(
    COOKED_TURTLE
  );

  private Foods() {}
}
