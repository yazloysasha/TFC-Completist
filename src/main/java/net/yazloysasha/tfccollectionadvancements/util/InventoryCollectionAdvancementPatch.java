package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class InventoryCollectionAdvancementPatch {

  private InventoryCollectionAdvancementPatch() {}

  public static void patch(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<InventoryCollectionSource> sources
  ) {
    patch(
      advancementId,
      advancements,
      resourceManager,
      registries,
      sources,
      AdvancementCriterionBuilder::inventoryChanged
    );
  }

  public static void patchConsume(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<InventoryCollectionSource> sources
  ) {
    patch(
      advancementId,
      advancements,
      resourceManager,
      registries,
      sources,
      AdvancementCriterionBuilder::consumeItem
    );
  }

  private static void patch(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
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
    boolean needsMetalOres = sources
      .stream()
      .anyMatch(source -> source.rejectBlockItems() || source.gemOre() != null);
    Set<ResourceLocation> metalOres = needsMetalOres
      ? ItemTagResolver.resolve(
        resourceManager,
        registries,
        TFCTags.Items.METAL_ORES
      )
      : Set.of();

    for (InventoryCollectionSource source : sources) {
      Set<ResourceLocation> tagMembers = source.tag() == null
        ? Set.of()
        : ItemTagResolver.resolve(resourceManager, registries, source.tag());
      int added = patchSource(
        criteria,
        requirements,
        items,
        source,
        tagMembers,
        metalOres,
        criterionFactory
      );
      if (added > 0) {
        TFCCollectionAdvancements.LOGGER.info(
          "Extended {} with {} {} item criteria",
          advancementId,
          added,
          source.displayNamespace()
        );
      }
    }
  }

  private static int patchSource(
    JsonObject criteria,
    JsonArray requirements,
    List<Holder.Reference<Item>> items,
    InventoryCollectionSource source,
    Set<ResourceLocation> tagMembers,
    Set<ResourceLocation> metalOres,
    Function<ResourceLocation, JsonObject> criterionFactory
  ) {
    int added = 0;
    for (var holder : items) {
      if (!source.matches(holder, tagMembers, metalOres)) {
        continue;
      }

      ResourceLocation itemId = holder.getKey().location();
      String criterionName = criterionName(itemId, source.pathPrefix());
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

  static String criterionName(ResourceLocation itemId, String pathPrefix) {
    String suffix = criterionSuffix(itemId.getPath(), pathPrefix);
    return AddonNamespaces.isTfc(itemId.getNamespace())
      ? suffix
      : itemId.getNamespace() + "_" + suffix;
  }

  private static String criterionSuffix(String path, String pathPrefix) {
    if (pathPrefix == null || pathPrefix.isEmpty()) {
      return path.substring(path.lastIndexOf('/') + 1);
    }
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
