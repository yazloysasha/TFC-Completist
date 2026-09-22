package net.yazloysasha.tfccollectionadvancements.advancement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.ModList;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class AdventuringTimeAdvancementPatch {

  public static final ResourceLocation TFC_ADVENTURING_TIME_ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath("tfc", "world/adventuring_time");

  private static final String BENEATH_MOD_ID = "beneath";
  private static final String CRITERION_PREFIX = "minecraft_";
  private static final TagKey<Biome> IS_NETHER = TagKey.create(
    Registries.BIOME,
    ResourceLocation.withDefaultNamespace("is_nether")
  );

  private AdventuringTimeAdvancementPatch() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    HolderLookup.Provider registries
  ) {
    if (!ModList.get().isLoaded(BENEATH_MOD_ID)) {
      return;
    }

    JsonElement adventuringTimeElement = advancements.get(
      TFC_ADVENTURING_TIME_ADVANCEMENT
    );
    if (
      adventuringTimeElement == null || !adventuringTimeElement.isJsonObject()
    ) {
      return;
    }

    JsonObject root = adventuringTimeElement.getAsJsonObject();
    JsonObject criteria = root.getAsJsonObject("criteria");
    JsonArray requirements = root.getAsJsonArray("requirements");
    if (criteria == null || requirements == null) {
      return;
    }

    int added = 0;
    var biomeRegistry = registries.lookupOrThrow(Registries.BIOME);
    var netherBiomes = biomeRegistry.get(IS_NETHER);
    if (netherBiomes.isEmpty()) {
      return;
    }

    for (Holder<Biome> holder : netherBiomes.get()) {
      ResourceLocation biomeId = holder.unwrapKey().orElseThrow().location();
      if (!ResourceLocation.DEFAULT_NAMESPACE.equals(biomeId.getNamespace())) {
        continue;
      }

      String criterionName = CRITERION_PREFIX + biomeId.getPath();
      if (criteria.has(criterionName)) {
        continue;
      }

      AdvancementPatchUtil.addCriterion(
        criteria,
        requirements,
        criterionName,
        AdvancementPatchUtil.createBiomeLocationCriterion(biomeId)
      );
      added++;
    }

    if (added > 0) {
      TFCCollectionAdvancements.LOGGER.info(
        "Extended {} with {} Nether biome criteria for Beneath",
        TFC_ADVENTURING_TIME_ADVANCEMENT,
        added
      );
    }
  }
}
