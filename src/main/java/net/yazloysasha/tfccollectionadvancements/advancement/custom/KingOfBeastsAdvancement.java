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
 * Rebuilds King of Beasts
 * ({@code tfc_collection_advancements:world/king_of_beasts}).
 * <p>
 * One {@code player_killed_entity} criterion per discoverable member of
 * {@code #tfc:land_predators}.
 */
public final class KingOfBeastsAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/king_of_beasts"
    );

  private KingOfBeastsAdvancement() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    EntityCollectionAdvancement.patchKilledEntityFromTags(
      ADVANCEMENT,
      advancements,
      resourceManager,
      registries,
      List.of(TFCTags.Entities.LAND_PREDATORS)
    );
  }
}
