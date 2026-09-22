package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class SaplingsAdvancementPatch {

  public static final ResourceLocation TFC_SAPLINGS_ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/saplings");

  private static final String SAPLING_PATH_PREFIX = "wood/sapling/";

  private record SaplingSource(String namespace, String criterionPrefix) {}

  private static final List<SaplingSource> SAPLING_SOURCES = List.of(
    new SaplingSource("afc", "afc_"),
    new SaplingSource("beneath", "beneath_")
  );

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

    var itemRegistry = registries.lookupOrThrow(Registries.ITEM);
    var saplings = itemRegistry.listElements().toList();

    for (SaplingSource source : SAPLING_SOURCES) {
      int added = patchSource(criteria, requirements, saplings, source);
      if (added > 0) {
        TFCCollectionAdvancements.LOGGER.info(
          "Extended {} with {} {} sapling criteria",
          TFC_SAPLINGS_ADVANCEMENT,
          added,
          source.namespace()
        );
      }
    }
  }

  private static int patchSource(
    JsonObject criteria,
    JsonArray requirements,
    List<Holder.Reference<Item>> saplings,
    SaplingSource source
  ) {
    int added = 0;
    for (var holder : saplings) {
      ResourceLocation itemId = holder.getKey().location();
      if (!source.namespace().equals(itemId.getNamespace())) {
        continue;
      }
      if (!itemId.getPath().startsWith(SAPLING_PATH_PREFIX)) {
        continue;
      }

      String woodId = itemId.getPath().substring(SAPLING_PATH_PREFIX.length());
      String criterionName = source.criterionPrefix() + woodId;
      if (criteria.has(criterionName)) {
        continue;
      }

      AdvancementPatchUtil.addCriterion(
        criteria,
        requirements,
        criterionName,
        AdvancementPatchUtil.createInventoryCriterion(itemId)
      );
      added++;
    }
    return added;
  }
}
