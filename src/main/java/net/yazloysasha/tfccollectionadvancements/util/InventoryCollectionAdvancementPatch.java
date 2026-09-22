package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.crop.CropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class InventoryCollectionAdvancementPatch {

  private static final String METAL_INGOT_PREFIX = "metal/ingot/";
  private static final String MOLTEN_METAL_PREFIX = "metal/";
  private static final String COMMON_INGOTS_PATH = "ingots/";

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

  /**
   * Discovers metals from molten metal fluids, then rebuilds the matching
   * ingot criteria.
   */
  public static void patchIngotsFromMoltenMetals(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
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

    AdvancementCriterionBuilder.resetCollection(root);
    criteria = root.getAsJsonObject("criteria");
    requirements = root.getAsJsonArray("requirements");

    var items = registries
      .lookupOrThrow(Registries.ITEM)
      .listElements()
      .toList();
    Map<ResourceLocation, Collection<ResourceLocation>> itemTags =
      ItemTagResolver.loadAll(resourceManager, registries);

    Map<String, ResourceLocation> tfcStyleIngots = new LinkedHashMap<>();
    for (var holder : items) {
      ResourceLocation itemId = holder.getKey().location();
      if (!AddonNamespaces.isDiscoverable(itemId.getNamespace())) {
        continue;
      }
      String path = itemId.getPath();
      if (matchesPathPrefix(path, METAL_INGOT_PREFIX)) {
        putMetalItem(
          tfcStyleIngots,
          path.substring(METAL_INGOT_PREFIX.length()),
          itemId
        );
      }
    }

    Set<String> metals = new LinkedHashSet<>();
    for (ResourceLocation fluidId : FluidTagResolver.resolve(
      resourceManager,
      registries,
      TFCTags.Fluids.MOLTEN_METALS
    )) {
      if (!AddonNamespaces.isDiscoverable(fluidId.getNamespace())) {
        continue;
      }
      String metal = metalNameFromMoltenFluid(fluidId);
      if (metal != null) {
        metals.add(metal);
      }
    }

    int added = 0;
    for (String metal : metals) {
      ResourceLocation ingotId = resolveIngotForMetal(
        metal,
        tfcStyleIngots,
        itemTags
      );
      if (ingotId == null) {
        continue;
      }
      String criterionName = metalCriterionName(ingotId, metal);
      if (criteria.has(criterionName)) {
        continue;
      }

      AdvancementCriterionBuilder.addAndRequire(
        criteria,
        requirements,
        criterionName,
        AdvancementCriterionBuilder.inventoryChanged(ingotId)
      );
      added++;
    }

    CollectionAdvancementDeduplicator.deduplicate(root);
    TFCCollectionAdvancements.LOGGER.info(
      "Rebuilt {} with {} molten-metal ingot criteria",
      advancementId,
      added
    );
  }

  private static void putMetalItem(
    Map<String, ResourceLocation> metals,
    String metal,
    ResourceLocation itemId
  ) {
    if (AddonNamespaces.isTfc(itemId.getNamespace())) {
      metals.put(metal, itemId);
    } else {
      metals.putIfAbsent(metal, itemId);
    }
  }

  private static ResourceLocation resolveIngotForMetal(
    String metal,
    Map<String, ResourceLocation> tfcStyleIngots,
    Map<ResourceLocation, Collection<ResourceLocation>> tags
  ) {
    ResourceLocation tfcStyle = tfcStyleIngots.get(metal);
    if (tfcStyle != null) {
      return tfcStyle;
    }

    Collection<ResourceLocation> taggedIngots = tags.get(
      ResourceLocation.fromNamespaceAndPath("c", COMMON_INGOTS_PATH + metal)
    );
    if (taggedIngots != null) {
      ResourceLocation fallback = null;
      for (ResourceLocation itemId : taggedIngots) {
        if (!AddonNamespaces.isDiscoverable(itemId.getNamespace())) {
          continue;
        }
        if (AddonNamespaces.isTfc(itemId.getNamespace())) {
          return itemId;
        }
        if (fallback == null) {
          fallback = itemId;
        }
      }
      if (fallback != null) {
        return fallback;
      }
    }

    return null;
  }

  /** {@code tfc:molten_metals} lists source fluids as {@code {ns}:metal/{name}}. */
  private static String metalNameFromMoltenFluid(ResourceLocation fluidId) {
    String path = fluidId.getPath();
    if (!matchesPathPrefix(path, MOLTEN_METAL_PREFIX)) {
      return null;
    }
    String metal = path.substring(MOLTEN_METAL_PREFIX.length());
    return metal.isEmpty() || metal.indexOf('/') >= 0 ? null : metal;
  }

  private static String metalCriterionName(
    ResourceLocation ingotId,
    String metal
  ) {
    return AddonNamespaces.isTfc(ingotId.getNamespace())
      ? metal
      : ingotId.getNamespace() + "_" + metal;
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
    AdvancementCriterionBuilder.resetCollection(root);
    criteria = root.getAsJsonObject("criteria");
    requirements = root.getAsJsonArray("requirements");

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

    boolean needsFarmlandSeeds = sources
      .stream()
      .anyMatch(
        source ->
          source.collectedItems() ==
          InventoryCollectionSource.CollectedItems.TFC_FARMLAND_SEEDS
      );
    Set<ResourceLocation> tfcFarmlandSeeds = needsFarmlandSeeds
      ? collectTfcCropSeeds()
      : Set.of();
    boolean needsMineralOreDrops = sources
      .stream()
      .anyMatch(
        source ->
          source.collectedItems() ==
          InventoryCollectionSource.CollectedItems.MINERAL_ORE_DROPS
      );
    Set<ResourceLocation> mineralOreDrops = needsMineralOreDrops
      ? MineralOreDropCollector.collect(resourceManager, registries)
      : Set.of();

    for (InventoryCollectionSource source : sources) {
      Set<ResourceLocation> tagMembers =
        switch (source.collectedItems()) {
          case TFC_FARMLAND_SEEDS -> tfcFarmlandSeeds;
          case MINERAL_ORE_DROPS -> mineralOreDrops;
          case NONE -> source.tag() == null
            ? Set.of()
            : ItemTagResolver.resolve(
              resourceManager,
              registries,
              source.tag()
            );
        };
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
          "Rebuilt {} with {} {} item criteria",
          advancementId,
          added,
          source.displayNamespace()
        );
      }
    }

    CollectionAdvancementDeduplicator.deduplicate(root);
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

  private static Set<ResourceLocation> collectTfcCropSeeds() {
    Set<ResourceLocation> seeds = new LinkedHashSet<>();
    for (Block block : BuiltInRegistries.BLOCK) {
      if (!(block instanceof CropBlock crop)) {
        continue;
      }
      Item seed = crop
        .getCloneItemStack(null, BlockPos.ZERO, crop.defaultBlockState())
        .getItem();
      if (seed == Items.AIR) {
        continue;
      }
      ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(seed);
      if (
        itemId != null && AddonNamespaces.isDiscoverable(itemId.getNamespace())
      ) {
        seeds.add(itemId);
      }
    }
    return seeds;
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
