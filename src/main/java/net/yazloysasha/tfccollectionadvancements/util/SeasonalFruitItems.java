package net.yazloysasha.tfccollectionadvancements.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.dries007.tfc.common.blocks.plant.fruit.SeasonalPlantBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

/**
 * Addon berry-bush and fruit-tree products that TFC-style plants expose at
 * runtime. Complements {@code c:foods/fruit}: bushes such as nightshade are
 * berries but are not always tagged as fruit.
 */
public final class SeasonalFruitItems {

  private SeasonalFruitItems() {}

  public static List<ResourceLocation> collectAddonProducts(
    HolderLookup.Provider registries
  ) {
    var itemRegistry = registries.lookupOrThrow(Registries.ITEM);
    var blockRegistry = registries.lookupOrThrow(Registries.BLOCK);
    RandomSource random = RandomSource.create(0L);
    Set<ResourceLocation> itemIds = new LinkedHashSet<>();

    blockRegistry
      .listElements()
      .forEach(holder -> {
        if (!(holder.value() instanceof SeasonalPlantBlock plant)) {
          return;
        }
        ItemStack product = plant.getProductItem(random);
        if (product.isEmpty()) {
          return;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(product.getItem());
        if (id == null || !AddonNamespaces.isAddon(id.getNamespace())) {
          return;
        }
        boolean fruit = itemRegistry
          .get(ResourceKey.create(Registries.ITEM, id))
          .map(found -> found.is(Tags.Items.FOODS_FRUIT))
          .orElse(false);
        if (fruit || id.getPath().contains("berry")) {
          itemIds.add(id);
        }
      });

    return new ArrayList<>(itemIds);
  }
}
