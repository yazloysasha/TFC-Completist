package net.yazloysasha.afcadvancement.advancement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.yazloysasha.afcadvancement.AFCAdvancement;

public final class SaplingsAdvancementPatch {

  public static final ResourceLocation TFC_SAPLINGS_ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/saplings");

  private static final String AFC_NAMESPACE = "afc";
  private static final String SAPLING_PATH_PREFIX = "wood/sapling/";
  private static final String CRITERION_PREFIX = "afc_";

  private SaplingsAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    HolderLookup.Provider registries
  ) {
    JsonElement saplingsElement = advancements.get(TFC_SAPLINGS_ADVANCEMENT);
    if (saplingsElement == null || !saplingsElement.isJsonObject()) {
      return;
    }

    JsonObject root = saplingsElement.getAsJsonObject();
    JsonObject criteria = root.getAsJsonObject("criteria");
    JsonArray requirements = root.getAsJsonArray("requirements");
    if (criteria == null || requirements == null) {
      return;
    }

    int added = 0;
    var itemRegistry = registries.lookupOrThrow(Registries.ITEM);
    var saplings = itemRegistry.listElements().toList();
    for (var holder : saplings) {
      ResourceLocation itemId = holder.getKey().location();
      if (!AFC_NAMESPACE.equals(itemId.getNamespace())) {
        continue;
      }
      if (!itemId.getPath().startsWith(SAPLING_PATH_PREFIX)) {
        continue;
      }

      String woodId = itemId.getPath().substring(SAPLING_PATH_PREFIX.length());
      String criterionName = CRITERION_PREFIX + woodId;
      if (criteria.has(criterionName)) {
        continue;
      }

      criteria.add(criterionName, createInventoryCriterion(itemId));
      JsonArray requirement = new JsonArray();
      requirement.add(criterionName);
      requirements.add(requirement);
      added++;
    }

    if (added > 0) {
      AFCAdvancement.LOGGER.info(
        "Extended {} with {} ArborFirmaCraft sapling criteria",
        TFC_SAPLINGS_ADVANCEMENT,
        added
      );
    }
  }

  private static JsonObject createInventoryCriterion(ResourceLocation itemId) {
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
}
