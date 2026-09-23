package net.yazloysasha.tfccompletist.collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.HashSet;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.yazloysasha.tfccompletist.TFCCompletist;
import net.yazloysasha.tfccompletist.discover.MineralOreDrops;
import net.yazloysasha.tfccompletist.discover.Namespaces;
import net.yazloysasha.tfccompletist.discover.RegistryTags;

public final class InventoryRebuild {

  private static final String METAL_INGOT_PREFIX = "metal/ingot/";
  private static final String MOLTEN_METAL_PREFIX = "metal/";
  private static final String COMMON_INGOTS_PATH = "ingots/";

  private InventoryRebuild() {}

  public static void patch(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<InventorySource> sources
  ) {
    patch(
      advancementId,
      advancements,
      resourceManager,
      registries,
      sources,
      CriterionJson::inventoryChanged
    );
  }

  public static void patchConsume(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<InventorySource> sources
  ) {
    patch(
      advancementId,
      advancements,
      resourceManager,
      registries,
      sources,
      CriterionJson::consumeItem
    );
  }

  public static void patchSealedJar(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<InventorySource> sources
  ) {
    patch(
      advancementId,
      advancements,
      resourceManager,
      registries,
      sources,
      CriterionJson::sealJar
    );
  }

  public static void patchPlacedBlock(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<InventorySource> sources
  ) {
    patch(
      advancementId,
      advancements,
      resourceManager,
      registries,
      sources,
      InventoryRebuild::placedBlockCriterion
    );
  }

  private static JsonObject placedBlockCriterion(ResourceLocation itemId) {
    Item item = BuiltInRegistries.ITEM.get(itemId);
    if (!(item instanceof BlockItem blockItem)) {
      return null;
    }
    ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(
      blockItem.getBlock()
    );
    return CriterionJson.placedBlock(blockId);
  }

  /**
   * Items from every source that share the same last path segment are OR'd in
   * one requirement group. Groups are AND'd. Used when several item forms
   * count as the same collectible (loose rock vs mossy loose rock).
   */
  public static void patchAnyOfByLastPathSegment(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<InventorySource> sources
  ) {
    JsonObject root = RebuildJson.collectionRootOrNull(
      advancementId,
      advancements
    );
    if (root == null) {
      return;
    }

    var items = registries
      .lookupOrThrow(Registries.ITEM)
      .listElements()
      .toList();
    Map<String, Map<String, ResourceLocation>> groups = groupByLastPathSegment(
      items,
      sources
    );
    if (
      RebuildJson.resetIfResolved(advancementId, root, groups.size(), "item") ==
      null
    ) {
      return;
    }
    JsonObject criteria = root.getAsJsonObject("criteria");
    JsonArray requirements = root.getAsJsonArray("requirements");

    int alternatives = 0;
    for (Map<String, ResourceLocation> group : groups.values()) {
      for (var entry : group.entrySet()) {
        if (criteria.has(entry.getKey())) {
          continue;
        }
        criteria.add(
          entry.getKey(),
          CriterionJson.inventoryChanged(entry.getValue())
        );
        alternatives++;
      }
      CriterionJson.addRequirementAny(requirements, group.keySet());
    }

    RebuildDeduplicator.deduplicate(root);
    if (alternatives > 0) {
      TFCCompletist.LOGGER.info(
        "Rebuilt {} with {} item groups ({} alternatives)",
        advancementId,
        groups.size(),
        alternatives
      );
    }
  }

  private static Map<
    String,
    Map<String, ResourceLocation>
  > groupByLastPathSegment(
    List<Holder.Reference<Item>> items,
    List<InventorySource> sources
  ) {
    Map<String, Map<String, ResourceLocation>> groups = new LinkedHashMap<>();
    Set<String> usedNames = new HashSet<>();
    Set<ResourceLocation> emptyTags = Set.of();
    Set<ResourceLocation> emptyOres = Set.of();

    for (InventorySource source : sources) {
      for (var holder : items) {
        if (!source.matches(holder, emptyTags, emptyOres)) {
          continue;
        }
        ResourceLocation itemId = holder.getKey().location();
        String groupKey = RebuildJson.qualifiedCriterion(
          itemId,
          RebuildJson.lastPathSegment(itemId.getPath())
        );
        Map<String, ResourceLocation> group = groups.computeIfAbsent(
          groupKey,
          key -> new LinkedHashMap<>()
        );
        String criterionName = uniqueCriterionName(
          itemId,
          source.pathPrefix(),
          usedNames
        );
        group.putIfAbsent(criterionName, itemId);
      }
    }
    return groups;
  }

  private static String uniqueCriterionName(
    ResourceLocation itemId,
    String pathPrefix,
    Set<String> usedNames
  ) {
    String name = criterionName(itemId, pathPrefix);
    if (usedNames.add(name)) {
      return name;
    }
    String folder = RebuildJson.lastPathSegment(
      pathPrefix != null && pathPrefix.endsWith("/")
        ? pathPrefix.substring(0, pathPrefix.length() - 1)
        : pathPrefix
    );
    String fallback = RebuildJson.qualifiedCriterion(
      itemId,
      folder + "_" + RebuildJson.lastPathSegment(itemId.getPath())
    );
    usedNames.add(fallback);
    return fallback;
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
    JsonObject root = RebuildJson.collectionRootOrNull(
      advancementId,
      advancements
    );
    if (root == null) {
      return;
    }

    var items = registries
      .lookupOrThrow(Registries.ITEM)
      .listElements()
      .toList();
    Map<ResourceLocation, Collection<ResourceLocation>> itemTags =
      RegistryTags.loadAll(resourceManager, registries, Registries.ITEM);

    Map<String, ResourceLocation> tfcStyleIngots = new LinkedHashMap<>();
    for (var holder : items) {
      ResourceLocation itemId = holder.getKey().location();
      if (!Namespaces.isDiscoverable(itemId.getNamespace())) {
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
    for (ResourceLocation fluidId : RegistryTags.resolveSet(
      resourceManager,
      registries,
      TFCTags.Fluids.MOLTEN_METALS
    )) {
      if (!Namespaces.isDiscoverable(fluidId.getNamespace())) {
        continue;
      }
      String metal = metalNameFromMoltenFluid(fluidId);
      if (metal != null) {
        metals.add(metal);
      }
    }

    int resolved = 0;
    for (String metal : metals) {
      if (resolveIngotForMetal(metal, tfcStyleIngots, itemTags) != null) {
        resolved++;
      }
    }
    if (
      RebuildJson.resetIfResolved(advancementId, root, resolved, "ingot") ==
      null
    ) {
      return;
    }

    JsonObject criteria = root.getAsJsonObject("criteria");
    JsonArray requirements = root.getAsJsonArray("requirements");

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

      CriterionJson.addAndRequire(
        criteria,
        requirements,
        criterionName,
        CriterionJson.inventoryChanged(ingotId)
      );
      added++;
    }

    RebuildDeduplicator.deduplicate(root);
    if (added > 0) {
      TFCCompletist.LOGGER.info(
        "Rebuilt {} with {} molten-metal ingot criteria",
        advancementId,
        added
      );
    }
  }

  private static void putMetalItem(
    Map<String, ResourceLocation> metals,
    String metal,
    ResourceLocation itemId
  ) {
    if (Namespaces.isTfc(itemId.getNamespace())) {
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
        if (!Namespaces.isDiscoverable(itemId.getNamespace())) {
          continue;
        }
        if (Namespaces.isTfc(itemId.getNamespace())) {
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
    return RebuildJson.qualifiedCriterion(ingotId, metal);
  }

  private static void patch(
    ResourceLocation advancementId,
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    List<InventorySource> sources,
    Function<ResourceLocation, JsonObject> criterionFactory
  ) {
    JsonObject root = RebuildJson.collectionRootOrNull(
      advancementId,
      advancements
    );
    if (root == null) {
      return;
    }

    var itemRegistry = registries.lookupOrThrow(Registries.ITEM);
    var items = itemRegistry.listElements().toList();

    boolean needsMineralOreDrops = sources
      .stream()
      .anyMatch(
        source ->
          source.collectedItems() ==
          InventorySource.CollectedItems.MINERAL_ORE_DROPS
      );
    Set<ResourceLocation> mineralOreDrops = needsMineralOreDrops
      ? MineralOreDrops.collect(resourceManager, registries)
      : Set.of();

    boolean needsMetalOres = sources
      .stream()
      .anyMatch(source -> source.rejectBlockItems() || source.gemOre() != null);
    Set<ResourceLocation> metalOres = needsMetalOres
      ? RegistryTags.resolveSet(
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
          InventorySource.CollectedItems.TFC_FARMLAND_SEEDS
      );
    Set<ResourceLocation> tfcFarmlandSeeds = needsFarmlandSeeds
      ? collectTfcCropSeeds()
      : Set.of();

    boolean needsSaltWaterCoralItems = sources
      .stream()
      .anyMatch(
        source ->
          source.collectedItems() ==
          InventorySource.CollectedItems.SALT_WATER_CORAL_ITEMS
      );
    Set<ResourceLocation> saltWaterCoralItems = needsSaltWaterCoralItems
      ? collectSaltWaterCoralItems(resourceManager, registries)
      : Set.of();

    int resolvedCriteria = countResolvedItemCriteria(
      items,
      sources,
      resourceManager,
      registries,
      mineralOreDrops,
      tfcFarmlandSeeds,
      saltWaterCoralItems,
      metalOres
    );
    if (
      RebuildJson.resetIfResolved(
        advancementId,
        root,
        resolvedCriteria,
        "item"
      ) ==
      null
    ) {
      return;
    }

    JsonObject criteria = root.getAsJsonObject("criteria");
    JsonArray requirements = root.getAsJsonArray("requirements");

    for (InventorySource source : sources) {
      int added = patchSource(
        criteria,
        requirements,
        items,
        source,
        tagMembersForSource(
          source,
          resourceManager,
          registries,
          mineralOreDrops,
          tfcFarmlandSeeds,
          saltWaterCoralItems
        ),
        metalOres,
        criterionFactory
      );
      if (added > 0) {
        TFCCompletist.LOGGER.info(
          "Rebuilt {} with {} {} item criteria",
          advancementId,
          added,
          source.displayNamespace()
        );
      }
    }

    RebuildDeduplicator.deduplicate(root);
  }

  private static int countResolvedItemCriteria(
    List<Holder.Reference<Item>> items,
    List<InventorySource> sources,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    Set<ResourceLocation> mineralOreDrops,
    Set<ResourceLocation> tfcFarmlandSeeds,
    Set<ResourceLocation> saltWaterCoralItems,
    Set<ResourceLocation> metalOres
  ) {
    Set<String> criterionNames = new HashSet<>();
    for (InventorySource source : sources) {
      Set<ResourceLocation> tagMembers = tagMembersForSource(
        source,
        resourceManager,
        registries,
        mineralOreDrops,
        tfcFarmlandSeeds,
        saltWaterCoralItems
      );
      for (var holder : items) {
        if (!source.matches(holder, tagMembers, metalOres)) {
          continue;
        }
        ResourceLocation itemId = holder.getKey().location();
        criterionNames.add(criterionName(itemId, source.pathPrefix()));
      }
    }
    return criterionNames.size();
  }

  private static Set<ResourceLocation> tagMembersForSource(
    InventorySource source,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    Set<ResourceLocation> mineralOreDrops,
    Set<ResourceLocation> tfcFarmlandSeeds,
    Set<ResourceLocation> saltWaterCoralItems
  ) {
    return switch (source.collectedItems()) {
      case TFC_FARMLAND_SEEDS -> tfcFarmlandSeeds;
      case MINERAL_ORE_DROPS -> mineralOreDrops;
      case SALT_WATER_CORAL_ITEMS -> saltWaterCoralItems;
      case NONE -> {
        if (source.tag() == null) {
          yield Set.of();
        }
        Set<ResourceLocation> members = new LinkedHashSet<>(
          RegistryTags.resolveSet(resourceManager, registries, source.tag())
        );
        for (var excludeTag : source.excludeTags()) {
          members.removeAll(
            RegistryTags.resolveSet(resourceManager, registries, excludeTag)
          );
        }
        members.removeAll(source.excludeItems());
        yield members;
      }
    };
  }

  private static int patchSource(
    JsonObject criteria,
    JsonArray requirements,
    List<Holder.Reference<Item>> items,
    InventorySource source,
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

      JsonObject criterion = criterionFactory.apply(itemId);
      if (criterion == null) {
        continue;
      }
      CriterionJson.addAndRequire(
        criteria,
        requirements,
        criterionName,
        criterion
      );
      added++;
    }
    return added;
  }

  private static Set<ResourceLocation> collectSaltWaterCoralItems(
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    Set<ResourceLocation> blockIds = RegistryTags.resolveSet(
      resourceManager,
      registries,
      TFCTags.Blocks.SALT_WATER_CORALS
    );
    var blockRegistry = registries.lookupOrThrow(Registries.BLOCK);
    Set<ResourceLocation> items = new LinkedHashSet<>();
    for (ResourceLocation blockId : blockIds) {
      if (!Namespaces.isDiscoverable(blockId.getNamespace())) {
        continue;
      }
      var blockKey = ResourceKey.create(Registries.BLOCK, blockId);
      var blockHolder = blockRegistry.get(blockKey);
      if (blockHolder.isEmpty()) {
        continue;
      }
      Item item = blockHolder.get().value().asItem();
      if (item == Items.AIR) {
        continue;
      }
      ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
      if (itemId != null && Namespaces.isDiscoverable(itemId.getNamespace())) {
        items.add(itemId);
      }
    }
    return items;
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
      if (itemId != null && Namespaces.isDiscoverable(itemId.getNamespace())) {
        seeds.add(itemId);
      }
    }
    return seeds;
  }

  static String criterionName(ResourceLocation itemId, String pathPrefix) {
    return RebuildJson.qualifiedCriterion(
      itemId,
      criterionSuffix(itemId.getPath(), pathPrefix)
    );
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
