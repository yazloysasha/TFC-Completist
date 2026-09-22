package net.yazloysasha.tfccollectionadvancements.advancement.custom;

import com.google.gson.JsonElement;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;
import net.yazloysasha.tfccollectionadvancements.util.AddonNamespaces;
import net.yazloysasha.tfccollectionadvancements.util.BlockCollectionAdvancement;

/**
 * Rebuilds Florist ({@code tfc_collection_advancements:world/florist}).
 * <p>
 * Every discoverable {@link FlowerPotBlock} except the empty pot, timber or
 * fruit saplings (Arborist / Orchardist), and krummholz. Criterion is
 * {@code item_used_on_block} after potting: {@code placed_block} does not fire
 * for flower-pot interactions.
 */
public final class FloristAdvancement {

  public static final ResourceLocation ADVANCEMENT =
    ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/florist"
    );

  private static final String POTTED_PLANT_PREFIX = "plant/potted/";

  private FloristAdvancement() {}

  public static void patch(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    BlockCollectionAdvancement.patchItemUsedOnBlocks(
      ADVANCEMENT,
      advancements,
      collectPottedPlants(registries),
      POTTED_PLANT_PREFIX
    );
  }

  private static Set<ResourceLocation> collectPottedPlants(
    HolderLookup.Provider registries
  ) {
    Set<ResourceLocation> potted = new LinkedHashSet<>();
    for (var holder : registries
      .lookupOrThrow(Registries.BLOCK)
      .listElements()
      .toList()) {
      if (!(holder.value() instanceof FlowerPotBlock)) {
        continue;
      }
      if (holder.value() == Blocks.FLOWER_POT) {
        continue;
      }
      ResourceLocation blockId = holder.getKey().location();
      if (!AddonNamespaces.isDiscoverable(blockId.getNamespace())) {
        continue;
      }
      String path = blockId.getPath();
      if (path.contains("sapling") || path.contains("krummholz")) {
        continue;
      }
      potted.add(blockId);
    }
    return potted;
  }
}
