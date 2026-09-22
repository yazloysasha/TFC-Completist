package net.yazloysasha.tfccollectionadvancements.util;

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
 * Removes redundant criteria and requirement groups from patched collection advancements
 * (e.g. duplicate {@code sulfur} entries in TFC Minerologist).
 */
public final class CollectionAdvancementDeduplicator {

  private CollectionAdvancementDeduplicator() {}

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

    String biomeId = biomeIdFromConditions(conditions);
    if (biomeId != null) {
      return "biome:" + biomeId;
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
      JsonElement id = conditions.getAsJsonObject("item").get("items");
      if (id != null && id.isJsonPrimitive()) {
        return id.getAsString();
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
