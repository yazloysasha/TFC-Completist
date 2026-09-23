package net.yazloysasha.tfccompletist.collection;

import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccompletist.TFCCompletist;

public final class RebuildGuard {

  private RebuildGuard() {}

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
    TFCCompletist.LOGGER.warn(
      "Skipping {} rebuild: no {} criteria resolved",
      advancementId,
      kind
    );
    return true;
  }
}
