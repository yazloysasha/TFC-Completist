package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class InventoryCollectionAdvancementPatch {

  private InventoryCollectionAdvancementPatch() {}

  public static void patch(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    HolderLookup.Provider registries,
    List<InventoryCollectionSource> sources
  ) {
    patch(
      advancementId,
      advancements,
      registries,
      sources,
      AdvancementCriterionBuilder::inventoryChanged
    );
  }

  public static void patchConsume(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    HolderLookup.Provider registries,
    List<InventoryCollectionSource> sources
  ) {
    patch(
      advancementId,
      advancements,
      registries,
      sources,
      AdvancementCriterionBuilder::consumeItem
    );
  }

  private static void patch(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    HolderLookup.Provider registries,
    List<InventoryCollectionSource> sources,
    Function<ResourceLocation, JsonObject> criterionFactory
  ) {
    JsonElement advancementElement = advancements.get(advancementId);
    if (advancementElement == null || !advancementElement.isJsonObject()) {
      return;
    }

    JsonObject root = advancementElement.getAsJsonObject();
    JsonObject criteria = root.getAsJsonObject("criteria");
    JsonArray requirements = root.getAsJsonArray("requirements");
    if (criteria == null || requirements == null) {
      return;
    }

    var itemRegistry = registries.lookupOrThrow(Registries.ITEM);
    var items = itemRegistry.listElements().toList();

    for (InventoryCollectionSource source : sources) {
      int added = patchSource(
        criteria,
        requirements,
        items,
        source,
        criterionFactory
      );
      if (added > 0) {
        TFCCollectionAdvancements.LOGGER.info(
          "Extended {} with {} {} item criteria",
          advancementId,
          added,
          source.namespace()
        );
      }
    }
  }

  private static int patchSource(
    JsonObject criteria,
    JsonArray requirements,
    List<Holder.Reference<Item>> items,
    InventoryCollectionSource source,
    Function<ResourceLocation, JsonObject> criterionFactory
  ) {
    int added = 0;
    for (var holder : items) {
      ResourceLocation itemId = holder.getKey().location();
      if (!source.namespace().equals(itemId.getNamespace())) {
        continue;
      }
      if (!source.matchesItemPath(itemId.getPath())) {
        continue;
      }

      String criterionSuffix = criterionSuffix(
        itemId.getPath(),
        source.pathPrefix()
      );
      String criterionName = source.criterionPrefix() + criterionSuffix;
      if (criteria.has(criterionName)) {
        continue;
      }

      AdvancementCriterionBuilder.addAndRequire(
        criteria,
        requirements,
        criterionName,
        criterionFactory.apply(itemId)
      );
      added++;
    }
    return added;
  }

  private static String criterionSuffix(String path, String pathPrefix) {
    String remainder;
    if (path.length() == pathPrefix.length()) {
      remainder = path.substring(path.lastIndexOf('/') + 1);
    } else {
      remainder = path.substring(pathPrefix.length());
    }
    int slash = remainder.lastIndexOf('/');
    return slash >= 0 ? remainder.substring(slash + 1) : remainder;
  }

  static boolean matchesPathPrefix(String path, String pathPrefix) {
    if (!path.startsWith(pathPrefix)) {
      return false;
    }
    if (path.length() == pathPrefix.length()) {
      return true;
    }
    if (pathPrefix.endsWith("/")) {
      return true;
    }
    return path.charAt(pathPrefix.length()) == '/';
  }
}
