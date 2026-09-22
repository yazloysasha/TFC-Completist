package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class BlockCollectionAdvancement {

  private BlockCollectionAdvancement() {}

  public static void patchItemUsedOnBlocks(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    Set<ResourceLocation> blockIds,
    String pathPrefix
  ) {
    patch(
      advancementId,
      advancements,
      blockIds,
      pathPrefix,
      AdvancementCriterionBuilder::itemUsedOnBlock,
      "potted-plant"
    );
  }

  private static void patch(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    Set<ResourceLocation> blockIds,
    String pathPrefix,
    Function<ResourceLocation, JsonObject> criterionFactory,
    String kind
  ) {
    JsonElement advancementElement = advancements.get(advancementId);
    if (advancementElement == null || !advancementElement.isJsonObject()) {
      return;
    }

    if (
      CollectionAdvancementRebuildGuard.shouldSkipRebuild(
        advancementId,
        blockIds.size(),
        kind
      )
    ) {
      return;
    }

    JsonObject root = advancementElement.getAsJsonObject();
    AdvancementCriterionBuilder.resetCollection(root);
    JsonObject criteria = root.getAsJsonObject("criteria");
    JsonArray requirements = root.getAsJsonArray("requirements");

    int added = 0;
    Set<String> usedNames = new LinkedHashSet<>();
    for (ResourceLocation blockId : blockIds) {
      String criterionName = InventoryCollectionAdvancement.criterionName(
        blockId,
        pathPrefix
      );
      if (!usedNames.add(criterionName) || criteria.has(criterionName)) {
        continue;
      }
      AdvancementCriterionBuilder.addAndRequire(
        criteria,
        requirements,
        criterionName,
        criterionFactory.apply(blockId)
      );
      added++;
    }

    CollectionAdvancementDeduplicator.deduplicate(root);
    if (added > 0) {
      TFCCollectionAdvancements.LOGGER.info(
        "Rebuilt {} with {} {} criteria",
        advancementId,
        added,
        kind
      );
    }
  }
}
