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
 * Rebuilds Domestication ({@code tfc_collection_advancements:world/domestication}).
 * <p>
 * One {@code familiarized_animal} criterion per discoverable member of
 * {@code #tfc:farm_animals} and {@code #tfc:pets}, minus frog. Parent is
 * {@code tfc:world/familiarity}.
 */
public final class DomesticationAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/domestication"
    );

  private DomesticationAdvancement() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    EntityCollectionAdvancement.patchFamiliarizedAnimalFromTags(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      List.of(TFCTags.Entities.FARM_ANIMALS, TFCTags.Entities.PETS)
    );
  }
}
