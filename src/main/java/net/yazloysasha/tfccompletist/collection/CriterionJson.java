package net.yazloysasha.tfccompletist.collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public final class CriterionJson {

  private CriterionJson() {}

  public static JsonObject bredAnimal(ResourceLocation entityId) {
    return simpleTrigger("tfc_completist:bred_animal", "entity", entityId);
  }

  public static JsonObject familiarizedAnimal(ResourceLocation entityId) {
    return simpleTrigger(
      "tfc_completist:familiarized_animal",
      "entity",
      entityId
    );
  }

  public static JsonObject sealJar(ResourceLocation itemId) {
    return simpleTrigger("tfc_completist:seal_jar", "item", itemId);
  }

  public static JsonObject drinkFluid(ResourceLocation fluidId) {
    return simpleTrigger("tfc_completist:drink_fluid", "fluid", fluidId);
  }

  public static JsonObject consumeItem(ResourceLocation itemId) {
    JsonObject conditions = new JsonObject();
    conditions.add("item", itemPredicate(itemId));
    return trigger("minecraft:consume_item", conditions);
  }

  public static JsonObject placedBlock(ResourceLocation blockId) {
    return locationBlockTrigger("minecraft:placed_block", blockId);
  }

  /**
   * Fires after a successful block interaction, including potting a plant.
   * The location check sees the block at the position after the use.
   */
  public static JsonObject itemUsedOnBlock(ResourceLocation blockId) {
    return locationBlockTrigger("minecraft:item_used_on_block", blockId);
  }

  public static JsonObject playerKilledEntity(ResourceLocation entityId) {
    JsonObject typePredicate = new JsonObject();
    typePredicate.addProperty("type", entityId.toString());

    JsonObject entityCondition = new JsonObject();
    entityCondition.addProperty("condition", "minecraft:entity_properties");
    entityCondition.add("predicate", typePredicate);
    entityCondition.addProperty("entity", "this");

    JsonArray entity = new JsonArray();
    entity.add(entityCondition);

    JsonObject conditions = new JsonObject();
    conditions.add("entity", entity);
    return trigger("minecraft:player_killed_entity", conditions);
  }

  private static JsonObject locationBlockTrigger(
    String trigger,
    ResourceLocation blockId
  ) {
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
    return trigger(trigger, conditions);
  }

  public static JsonObject inventoryChanged(ResourceLocation itemId) {
    JsonArray items = new JsonArray();
    items.add(itemPredicate(itemId));
    JsonObject conditions = new JsonObject();
    conditions.add("items", items);
    return trigger("minecraft:inventory_changed", conditions);
  }

  private static JsonObject itemPredicate(ResourceLocation itemId) {
    JsonObject predicate = new JsonObject();
    predicate.addProperty("items", itemId.toString());
    return predicate;
  }

  private static JsonObject simpleTrigger(
    String trigger,
    String key,
    ResourceLocation value
  ) {
    JsonObject conditions = new JsonObject();
    conditions.addProperty(key, value.toString());
    return trigger(trigger, conditions);
  }

  private static JsonObject trigger(String trigger, JsonObject conditions) {
    JsonObject criterion = new JsonObject();
    criterion.addProperty("trigger", trigger);
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
    return trigger("minecraft:location", conditions);
  }

  public static void resetCollection(JsonObject root) {
    root.add("criteria", new JsonObject());
    root.add("requirements", new JsonArray());
  }

  public static void setFrame(JsonObject root, String frame) {
    JsonObject display = root.getAsJsonObject("display");
    if (display != null) {
      display.addProperty("frame", frame);
    }
  }

  public static void addAndRequire(
    JsonObject criteria,
    JsonArray requirements,
    String criterionName,
    JsonObject criterion
  ) {
    criteria.add(criterionName, criterion);
    if (RebuildDeduplicator.requirementsInclude(requirements, criterionName)) {
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
