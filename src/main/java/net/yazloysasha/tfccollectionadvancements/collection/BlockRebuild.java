package net.yazloysasha.tfccollectionadvancements.collection;

import com.google.gson.JsonElement;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class BlockRebuild {

  private BlockRebuild() {}

  public static void patchItemUsedOnBlocks(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    Set<ResourceLocation> blockIds,
    String pathPrefix
  ) {
    RebuildJson.rebuildIdCriteria(
      advancementId,
      advancements,
      blockIds,
      "potted-plant",
      "potted-plant",
      false,
      blockId -> InventoryRebuild.criterionName(blockId, pathPrefix),
      CriterionJson::itemUsedOnBlock
    );
  }
}
