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
 * Products of TFC berry bushes and fruit trees ({@link SeasonalPlantBlock}).
 * <p>
 * TFC Healthy Diet is generated from {@code BERRIES + FRUITS}, which is exactly
 * these plants — not {@code c:foods/fruit} (that tag also contains crop foods
 * such as {@code melon_slice}). Cocoa beans are a fruit-tree product but not a
 * diet fruit, so they stay out unless tagged as fruit.
 */
public final class SeasonalFruitItems {

  private SeasonalFruitItems() {}

  public static List<ResourceLocation> collect(
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
        if (id == null || !AddonNamespaces.isDiscoverable(id.getNamespace())) {
          return;
        }
        boolean fruit = itemRegistry
          .get(ResourceKey.create(Registries.ITEM, id))
          .map(found -> found.is(Tags.Items.FOODS_FRUIT))
          .orElse(false);
        if (fruit || isBerryProduct(id.getPath())) {
          itemIds.add(id);
        }
      });

    return new ArrayList<>(itemIds);
  }

  static boolean isBerryProduct(String path) {
    return path.contains("berry");
  }
}
