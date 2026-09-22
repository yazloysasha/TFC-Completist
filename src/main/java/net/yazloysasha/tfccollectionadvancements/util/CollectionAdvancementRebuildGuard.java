package net.yazloysasha.tfccollectionadvancements.util;

import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class CollectionAdvancementRebuildGuard {

  private CollectionAdvancementRebuildGuard() {}

  /**
   * @return {@code true} when the patch should abort and leave vanilla criteria
   */
  public static boolean shouldSkipRebuild(
    ResourceLocation advancementId,
    int resolvedCriteria,
    String kind
  ) {
    if (resolvedCriteria > 0) {
      return false;
    }
    TFCCollectionAdvancements.LOGGER.warn(
      "Skipping {} rebuild: no {} criteria resolved",
      advancementId,
      kind
    );
    return true;
  }
}
