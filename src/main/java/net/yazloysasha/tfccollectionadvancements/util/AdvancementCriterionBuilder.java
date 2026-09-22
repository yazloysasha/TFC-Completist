package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public final class AdvancementCriterionBuilder {

  private AdvancementCriterionBuilder() {}

  public static JsonObject consumeItem(ResourceLocation itemId) {
    JsonObject itemPredicate = new JsonObject();
    itemPredicate.addProperty("items", itemId.toString());

    JsonObject conditions = new JsonObject();
    conditions.add("item", itemPredicate);

    JsonObject criterion = new JsonObject();
    criterion.addProperty("trigger", "minecraft:consume_item");
    criterion.add("conditions", conditions);
    return criterion;
  }

  public static JsonObject placedBlock(ResourceLocation blockId) {
    JsonObject blocks = new JsonObject();
    JsonArray blockIds = new JsonArray();
    blockIds.add(blockId.toString());
    blocks.add("blocks", blockIds);

    JsonObject predicate = new JsonObject();
    predicate.add("block", blocks);

    JsonObject locationCheck = new JsonObject();
    locationCheck.addProperty("condition", "minecraft:location_check");
    locationCheck.add("predicate", predicate);

    JsonArray location = new JsonArray();
    location.add(locationCheck);

    JsonObject conditions = new JsonObject();
    conditions.add("location", location);

    JsonObject criterion = new JsonObject();
    criterion.addProperty("trigger", "minecraft:placed_block");
    criterion.add("conditions", conditions);
    return criterion;
  }

  public static JsonObject inventoryChanged(ResourceLocation itemId) {
    JsonObject itemPredicate = new JsonObject();
    itemPredicate.addProperty("items", itemId.toString());

    JsonArray items = new JsonArray();
    items.add(itemPredicate);

    JsonObject conditions = new JsonObject();
    conditions.add("items", items);

    JsonObject criterion = new JsonObject();
    criterion.addProperty("trigger", "minecraft:inventory_changed");
    criterion.add("conditions", conditions);
    return criterion;
  }

  public static JsonObject biomeLocation(ResourceLocation biomeId) {
    JsonObject location = new JsonObject();
    location.addProperty("biomes", biomeId.toString());

    JsonObject predicate = new JsonObject();
    predicate.add("location", location);

    JsonObject playerCondition = new JsonObject();
    playerCondition.addProperty("condition", "minecraft:entity_properties");
    playerCondition.addProperty("entity", "this");
    playerCondition.add("predicate", predicate);

    JsonArray player = new JsonArray();
    player.add(playerCondition);

    JsonObject conditions = new JsonObject();
    conditions.add("player", player);

    JsonObject criterion = new JsonObject();
    criterion.addProperty("trigger", "minecraft:location");
    criterion.add("conditions", conditions);
    return criterion;
  }

  public static void resetCollection(JsonObject root) {
    root.add("criteria", new JsonObject());
    root.add("requirements", new JsonArray());
  }

  public static void addAndRequire(
    JsonObject criteria,
    JsonArray requirements,
    String criterionName,
    JsonObject criterion
  ) {
    criteria.add(criterionName, criterion);
    if (
      CollectionAdvancementDeduplicator.requirementsInclude(
        requirements,
        criterionName
      )
    ) {
      return;
    }
    JsonArray requirement = new JsonArray();
    requirement.add(criterionName);
    requirements.add(requirement);
  }

  /**
   * One AND-group whose members are OR'd. Criteria must already exist.
   */
  public static void addRequirementAny(
    JsonArray requirements,
    Iterable<String> criterionNames
  ) {
    JsonArray requirement = new JsonArray();
    for (String criterionName : criterionNames) {
      requirement.add(criterionName);
    }
    if (requirement.isEmpty()) {
      return;
    }
    requirements.add(requirement);
  }
}
