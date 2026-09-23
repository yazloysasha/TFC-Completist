package net.yazloysasha.tfccollectionadvancements.util;

import java.util.List;
import net.minecraft.resources.ResourceLocation;

/**
 * TFC tags {@code cooked_turtle} under seafood; meat advancements treat turtle as
 * land meat instead.
 */
public final class MeatCollectionItems {

  public static final ResourceLocation COOKED_TURTLE =
    ResourceLocation.fromNamespaceAndPath("tfc", "food/cooked_turtle");

  public static final List<ResourceLocation> SEAFOOD_TAG_EXCLUSIONS = List.of(
    COOKED_TURTLE
  );

  private MeatCollectionItems() {}
}
