package net.yazloysasha.tfccollectionadvancements.advancement.custom;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;
import net.yazloysasha.tfccollectionadvancements.util.EntityCollectionAdvancement;

/**
 * Rebuilds Husbandman ({@code tfc_collection_advancements:world/husbandman}).
 * <p>
 * Same tag sources as {@link DomesticationAdvancement}, but only entity types
 * where a scratch pair can mate in TFC ({@code canMate} /
 * {@code checkExtraBreedConditions}), like True Farmer discovers seeds from
 * {@code CropBlock}. Criterion is {@code bred_animal} from
 * {@code TFCAnimalProperties#onFertilized}.
 */
public final class HusbandmanAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/husbandman"
    );

  private HusbandmanAdvancement() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    EntityCollectionAdvancement.patchBredAnimalFromTags(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      List.of(TFCTags.Entities.FARM_ANIMALS, TFCTags.Entities.PETS)
    );
  }
}
