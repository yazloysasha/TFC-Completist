package net.yazloysasha.tfccollectionadvancements.collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;
import net.yazloysasha.tfccollectionadvancements.discover.Namespaces;

/**
 * Shared JSON open/reset/write helpers for rebuilt collection advancements.
 */
public final class RebuildJson {

  private RebuildJson() {}

  public static JsonObject advancementRootOrNull(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements
  ) {
    JsonElement element = advancements.get(advancementId);
    return element != null && element.isJsonObject()
      ? element.getAsJsonObject()
      : null;
  }

  public static JsonObject collectionRootOrNull(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements
  ) {
    JsonObject root = advancementRootOrNull(advancementId, advancements);
    if (
      root == null ||
      root.getAsJsonObject("criteria") == null ||
      root.getAsJsonArray("requirements") == null
    ) {
      return null;
    }
    return root;
  }

  public static JsonObject resetIfResolved(
    ResourceLocation advancementId,
    JsonObject root,
    int resolvedCriteria,
    String kind
  ) {
    if (RebuildGuard.shouldSkipRebuild(advancementId, resolvedCriteria, kind)) {
      return null;
    }
    CriterionJson.resetCollection(root);
    return root;
  }

  public static String qualifiedCriterion(ResourceLocation id, String suffix) {
    return Namespaces.isTfc(id.getNamespace())
      ? suffix
      : id.getNamespace() + "_" + suffix;
  }

  public static String lastPathSegment(String path) {
    if (path == null || path.isEmpty()) {
      return "";
    }
    int slash = path.lastIndexOf('/');
    return slash >= 0 ? path.substring(slash + 1) : path;
  }

  public static String lastSegmentCriterion(ResourceLocation id) {
    return qualifiedCriterion(id, lastPathSegment(id.getPath()));
  }

  public static String pathCriterion(ResourceLocation id) {
    return qualifiedCriterion(id, id.getPath());
  }

  public static int addCriteria(
    JsonObject criteria,
    JsonArray requirements,
    Iterable<ResourceLocation> ids,
    Function<ResourceLocation, String> nameFactory,
    Function<ResourceLocation, JsonObject> criterionFactory
  ) {
    int added = 0;
    for (ResourceLocation id : ids) {
      String name = nameFactory.apply(id);
      if (criteria.has(name)) {
        continue;
      }
      JsonObject criterion = criterionFactory.apply(id);
      if (criterion == null) {
        continue;
      }
      CriterionJson.addAndRequire(criteria, requirements, name, criterion);
      added++;
    }
    return added;
  }

  public static void finish(
    JsonObject root,
    ResourceLocation advancementId,
    int added,
    String kind
  ) {
    RebuildDeduplicator.deduplicate(root);
    if (added > 0) {
      TFCCollectionAdvancements.LOGGER.info(
        "Rebuilt {} with {} {} criteria",
        advancementId,
        added,
        kind
      );
    }
  }

  /**
   * @param requireCollectionShape when {@code true}, skip advancements that
   * have no {@code criteria}/{@code requirements} yet (inventory/entity/fluid).
   * Block rebuilds still write those keys via {@code resetCollection}.
   */
  public static void rebuildIdCriteria(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    Collection<ResourceLocation> ids,
    String skipKind,
    String logKind,
    boolean requireCollectionShape,
    Function<ResourceLocation, String> nameFactory,
    Function<ResourceLocation, JsonObject> criterionFactory
  ) {
    JsonObject root = requireCollectionShape
      ? collectionRootOrNull(advancementId, advancements)
      : advancementRootOrNull(advancementId, advancements);
    if (root == null) {
      return;
    }
    if (resetIfResolved(advancementId, root, ids.size(), skipKind) == null) {
      return;
    }
    int added = addCriteria(
      root.getAsJsonObject("criteria"),
      root.getAsJsonArray("requirements"),
      ids,
      nameFactory,
      criterionFactory
    );
    finish(root, advancementId, added, logKind);
  }
}
