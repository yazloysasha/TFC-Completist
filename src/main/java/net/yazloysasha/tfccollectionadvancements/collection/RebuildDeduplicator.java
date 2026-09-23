package net.yazloysasha.tfccollectionadvancements.collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Removes redundant criteria and requirement groups from rebuilt collection
 * advancements (for example two sources that resolve to the same item).
 */
public final class RebuildDeduplicator {

  private RebuildDeduplicator() {}

  public static void deduplicate(JsonObject root) {
    JsonObject criteria = root.getAsJsonObject("criteria");
    JsonArray requirements = root.getAsJsonArray("requirements");
    if (criteria == null || requirements == null) {
      return;
    }
    mergeDuplicateCriteria(criteria, requirements);
    deduplicateRequirements(requirements);
  }

  private static void mergeDuplicateCriteria(
    JsonObject criteria,
    JsonArray requirements
  ) {
    Map<String, String> targetToCanonical = new HashMap<>();
    List<String> duplicateNames = new ArrayList<>();

    for (Map.Entry<String, JsonElement> entry : criteria.entrySet()) {
      String criterionName = entry.getKey();
      String target = criterionTarget(entry.getValue());
      if (target == null) {
        continue;
      }
      String canonical = targetToCanonical.putIfAbsent(target, criterionName);
      if (canonical != null) {
        duplicateNames.add(criterionName);
        renameCriterionInRequirements(requirements, criterionName, canonical);
      }
    }

    for (String duplicate : duplicateNames) {
      criteria.remove(duplicate);
    }
  }

  private static void renameCriterionInRequirements(
    JsonArray requirements,
    String from,
    String to
  ) {
    for (JsonElement requirementElement : requirements) {
      if (!requirementElement.isJsonArray()) {
        continue;
      }
      JsonArray requirement = requirementElement.getAsJsonArray();
      for (int i = 0; i < requirement.size(); i++) {
        if (requirement.get(i).getAsString().equals(from)) {
          requirement.set(i, new JsonPrimitive(to));
        }
      }
    }
  }

  private static void deduplicateRequirements(JsonArray requirements) {
    Set<String> seenGroups = new LinkedHashSet<>();
    List<JsonElement> kept = new ArrayList<>();

    for (JsonElement requirementElement : requirements) {
      if (!requirementElement.isJsonArray()) {
        kept.add(requirementElement);
        continue;
      }
      JsonArray normalized = normalizeRequirementGroup(
        requirementElement.getAsJsonArray()
      );
      String key = requirementGroupKey(normalized);
      if (seenGroups.add(key)) {
        kept.add(normalized);
      }
    }

    requirements.asList().clear();
    for (JsonElement element : kept) {
      requirements.add(element);
    }
  }

  private static JsonArray normalizeRequirementGroup(JsonArray requirement) {
    Set<String> names = new LinkedHashSet<>();
    for (JsonElement element : requirement) {
      names.add(element.getAsString());
    }
    JsonArray normalized = new JsonArray();
    for (String name : names) {
      normalized.add(name);
    }
    return normalized;
  }

  private static String requirementGroupKey(JsonArray requirement) {
    StringBuilder key = new StringBuilder();
    for (JsonElement element : requirement) {
      if (!key.isEmpty()) {
        key.append('\0');
      }
      key.append(element.getAsString());
    }
    return key.toString();
  }

  private static String criterionTarget(JsonElement criterionElement) {
    if (!criterionElement.isJsonObject()) {
      return null;
    }
    JsonObject criterion = criterionElement.getAsJsonObject();
    JsonObject conditions = criterion.getAsJsonObject("conditions");
    if (conditions == null) {
      return null;
    }

    String itemId = itemIdFromConditions(conditions);
    if (itemId != null) {
      return "item:" + itemId;
    }

    String blockId = blockIdFromConditions(conditions);
    if (blockId != null) {
      return "block:" + blockId;
    }

    String biomeId = biomeIdFromConditions(conditions);
    if (biomeId != null) {
      return "biome:" + biomeId;
    }

    if (conditions.has("fluid") && conditions.get("fluid").isJsonPrimitive()) {
      return "fluid:" + conditions.get("fluid").getAsString();
    }

    String entityId = entityIdFromConditions(conditions);
    if (entityId != null) {
      return "entity:" + entityId;
    }

    return null;
  }

  private static String itemIdFromConditions(JsonObject conditions) {
    if (conditions.has("items")) {
      JsonArray items = conditions.getAsJsonArray("items");
      if (!items.isEmpty() && items.get(0).isJsonObject()) {
        JsonElement id = items.get(0).getAsJsonObject().get("items");
        if (id != null && id.isJsonPrimitive()) {
          return id.getAsString();
        }
      }
    }
    if (conditions.has("item")) {
      JsonElement item = conditions.get("item");
      if (item.isJsonPrimitive()) {
        return item.getAsString();
      }
      if (item.isJsonObject()) {
        JsonElement id = item.getAsJsonObject().get("items");
        if (id != null && id.isJsonPrimitive()) {
          return id.getAsString();
        }
      }
    }
    return null;
  }

  private static String blockIdFromConditions(JsonObject conditions) {
    if (!conditions.has("location")) {
      return null;
    }
    JsonArray location = conditions.getAsJsonArray("location");
    for (JsonElement element : location) {
      if (!element.isJsonObject()) {
        continue;
      }
      JsonObject condition = element.getAsJsonObject();
      if (
        !"minecraft:location_check".equals(
            condition.get("condition").getAsString()
          )
      ) {
        continue;
      }
      JsonObject predicate = condition.getAsJsonObject("predicate");
      if (predicate == null || !predicate.has("block")) {
        continue;
      }
      JsonObject block = predicate.getAsJsonObject("block");
      if (block.has("blocks")) {
        JsonElement blocks = block.get("blocks");
        if (blocks.isJsonPrimitive()) {
          return blocks.getAsString();
        }
        if (blocks.isJsonArray() && !blocks.getAsJsonArray().isEmpty()) {
          return blocks.getAsJsonArray().get(0).getAsString();
        }
      }
    }
    return null;
  }

  private static String entityIdFromConditions(JsonObject conditions) {
    if (!conditions.has("entity")) {
      return null;
    }
    JsonElement entity = conditions.get("entity");
    if (entity.isJsonPrimitive()) {
      return entity.getAsString();
    }
    if (entity.isJsonObject()) {
      JsonElement type = entity.getAsJsonObject().get("type");
      if (type != null && type.isJsonPrimitive()) {
        return type.getAsString();
      }
    }
    return null;
  }

  private static String biomeIdFromConditions(JsonObject conditions) {
    if (!conditions.has("player")) {
      return null;
    }
    JsonArray player = conditions.getAsJsonArray("player");
    if (player.isEmpty() || !player.get(0).isJsonObject()) {
      return null;
    }
    JsonObject predicate = player
      .get(0)
      .getAsJsonObject()
      .getAsJsonObject("predicate");
    if (predicate == null) {
      return null;
    }
    JsonObject location = predicate.getAsJsonObject("location");
    if (location == null) {
      return null;
    }
    JsonElement biomes = location.get("biomes");
    if (biomes != null && biomes.isJsonPrimitive()) {
      return biomes.getAsString();
    }
    return null;
  }

  static boolean requirementsInclude(
    JsonArray requirements,
    String criterionName
  ) {
    for (JsonElement requirementElement : requirements) {
      if (!requirementElement.isJsonArray()) {
        continue;
      }
      JsonArray requirement = requirementElement.getAsJsonArray();
      for (JsonElement element : requirement) {
        if (element.getAsString().equals(criterionName)) {
          return true;
        }
      }
    }
    return false;
  }
}
