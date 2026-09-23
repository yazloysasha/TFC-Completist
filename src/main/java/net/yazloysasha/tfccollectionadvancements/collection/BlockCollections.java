package net.yazloysasha.tfccollectionadvancements.collection;

import com.google.gson.JsonElement;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class BlockCollections {

  private BlockCollections() {}

  public static void patchItemUsedOnBlocks(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    Set<ResourceLocation> blockIds,
    String pathPrefix
  ) {
    CollectionJson.rebuildIdCriteria(
      advancementId,
      advancements,
      blockIds,
      "potted-plant",
      "potted-plant",
      false,
      blockId -> InventoryCollections.criterionName(blockId, pathPrefix),
      CriterionBuilder::itemUsedOnBlock
    );
  }
}
