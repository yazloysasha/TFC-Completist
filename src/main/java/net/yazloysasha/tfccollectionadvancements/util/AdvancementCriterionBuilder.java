package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public final class AdvancementCriterionBuilder {

  private AdvancementCriterionBuilder() {}

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

  public static void addAndRequire(
    JsonObject criteria,
    JsonArray requirements,
    String criterionName,
    JsonObject criterion
  ) {
    criteria.add(criterionName, criterion);
    JsonArray requirement = new JsonArray();
    requirement.add(criterionName);
    requirements.add(requirement);
  }
}
