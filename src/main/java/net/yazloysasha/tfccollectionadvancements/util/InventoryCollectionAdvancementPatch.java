package net.yazloysasha.tfccollectionadvancements.util;

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
import java.util.stream.Collectors;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.crop.CropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
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

  public static void patchPlacedBlock(
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
      InventoryCollectionAdvancementPatch::placedBlockCriterion
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
    return AdvancementCriterionBuilder.placedBlock(blockId);
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
    List<InventoryCollectionSource> sources
  ) {
    JsonElement advancementElement = advancements.get(advancementId);
    if (advancementElement == null || !advancementElement.isJsonObject()) {
      return;
    }

    JsonObject root = advancementElement.getAsJsonObject();
    if (
      root.getAsJsonObject("criteria") == null ||
      root.getAsJsonArray("requirements") == null
    ) {
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
      CollectionAdvancementRebuildGuard.shouldSkipRebuild(
        advancementId,
        groups.size(),
        "item"
      )
    ) {
      return;
    }

    AdvancementCriterionBuilder.resetCollection(root);
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
          AdvancementCriterionBuilder.inventoryChanged(entry.getValue())
        );
        alternatives++;
      }
      AdvancementCriterionBuilder.addRequirementAny(
        requirements,
        group.keySet()
      );
    }

    CollectionAdvancementDeduplicator.deduplicate(root);
    if (alternatives > 0) {
      TFCCollectionAdvancements.LOGGER.info(
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
    List<InventoryCollectionSource> sources
  ) {
    Map<String, Map<String, ResourceLocation>> groups = new LinkedHashMap<>();
    Set<String> usedNames = new HashSet<>();
    Set<ResourceLocation> emptyTags = Set.of();
    Set<ResourceLocation> emptyOres = Set.of();

    for (InventoryCollectionSource source : sources) {
      for (var holder : items) {
        if (!source.matches(holder, emptyTags, emptyOres)) {
          continue;
        }
        ResourceLocation itemId = holder.getKey().location();
        String groupKey = groupKey(itemId);
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

  private static String groupKey(ResourceLocation itemId) {
    String rock = lastPathSegment(itemId.getPath());
    return AddonNamespaces.isTfc(itemId.getNamespace())
      ? rock
      : itemId.getNamespace() + "_" + rock;
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
    String folder = lastPathSegment(
      pathPrefix != null && pathPrefix.endsWith("/")
        ? pathPrefix.substring(0, pathPrefix.length() - 1)
        : pathPrefix
    );
    String fallback = AddonNamespaces.isTfc(itemId.getNamespace())
      ? folder + "_" + lastPathSegment(itemId.getPath())
      : itemId.getNamespace() +
      "_" +
      folder +
      "_" +
      lastPathSegment(itemId.getPath());
    usedNames.add(fallback);
    return fallback;
  }

  private static String lastPathSegment(String path) {
    if (path == null || path.isEmpty()) {
      return "";
    }
    int slash = path.lastIndexOf('/');
    return slash >= 0 ? path.substring(slash + 1) : path;
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

    var items = registries
      .lookupOrThrow(Registries.ITEM)
      .listElements()
      .toList();
    Map<ResourceLocation, Collection<ResourceLocation>> itemTags =
      RegistryTagResolver.loadAll(resourceManager, registries, Registries.ITEM);

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
    for (ResourceLocation fluidId : RegistryTagResolver.resolveSet(
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

    int resolved = 0;
    for (String metal : metals) {
      if (resolveIngotForMetal(metal, tfcStyleIngots, itemTags) != null) {
        resolved++;
      }
    }
    if (
      CollectionAdvancementRebuildGuard.shouldSkipRebuild(
        advancementId,
        resolved,
        "ingot"
      )
    ) {
      return;
    }

    AdvancementCriterionBuilder.resetCollection(root);
    criteria = root.getAsJsonObject("criteria");
    requirements = root.getAsJsonArray("requirements");

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
    if (added > 0) {
      TFCCollectionAdvancements.LOGGER.info(
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

    boolean needsMetalOres = sources
      .stream()
      .anyMatch(source -> source.rejectBlockItems() || source.gemOre() != null);
    Set<ResourceLocation> metalOres = needsMetalOres
      ? RegistryTagResolver.resolveSet(
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

    int resolvedCriteria = countResolvedItemCriteria(
      items,
      sources,
      resourceManager,
      registries,
      mineralOreDrops,
      tfcFarmlandSeeds,
      metalOres
    );
    if (
      CollectionAdvancementRebuildGuard.shouldSkipRebuild(
        advancementId,
        resolvedCriteria,
        "item"
      )
    ) {
      return;
    }

    AdvancementCriterionBuilder.resetCollection(root);
    criteria = root.getAsJsonObject("criteria");
    requirements = root.getAsJsonArray("requirements");

    for (InventoryCollectionSource source : sources) {
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
          tfcFarmlandSeeds
        ),
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

  private static int countResolvedItemCriteria(
    List<Holder.Reference<Item>> items,
    List<InventoryCollectionSource> sources,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    Set<ResourceLocation> mineralOreDrops,
    Set<ResourceLocation> tfcFarmlandSeeds,
    Set<ResourceLocation> metalOres
  ) {
    Set<String> criterionNames = new HashSet<>();
    for (InventoryCollectionSource source : sources) {
      Set<ResourceLocation> tagMembers = tagMembersForSource(
        source,
        resourceManager,
        registries,
        mineralOreDrops,
        tfcFarmlandSeeds
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
    InventoryCollectionSource source,
    ResourceManager resourceManager,
    HolderLookup.Provider registries,
    Set<ResourceLocation> mineralOreDrops,
    Set<ResourceLocation> tfcFarmlandSeeds
  ) {
    return switch (source.collectedItems()) {
      case TFC_FARMLAND_SEEDS -> tfcFarmlandSeeds;
      case MINERAL_ORE_DROPS -> mineralOreDrops;
      case NONE -> resolveTagMembers(source, resourceManager, registries);
    };
  }

  private static Set<ResourceLocation> resolveTagMembers(
    InventoryCollectionSource source,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    if (source.tag() == null) {
      return Set.of();
    }
    Set<ResourceLocation> members = RegistryTagResolver.resolveSet(
      resourceManager,
      registries,
      source.tag()
    );
    if (source.counterpartTag() == null) {
      return members;
    }
    Set<ResourceLocation> rawMembers = RegistryTagResolver.resolveSet(
      resourceManager,
      registries,
      source.counterpartTag()
    );
    return members
      .stream()
      .filter(id -> {
        ResourceLocation raw = rawCounterpartFromCooked(id);
        return raw != null && rawMembers.contains(raw);
      })
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  static ResourceLocation rawCounterpartFromCooked(ResourceLocation cookedId) {
    String path = cookedId.getPath();
    String prefix = "food/cooked_";
    if (!path.startsWith(prefix)) {
      return null;
    }
    String rawSegment = path.substring(prefix.length());
    if (rawSegment.isEmpty() || rawSegment.indexOf('/') >= 0) {
      return null;
    }
    return ResourceLocation.fromNamespaceAndPath(
      cookedId.getNamespace(),
      "food/" + rawSegment
    );
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

      JsonObject criterion = criterionFactory.apply(itemId);
      if (criterion == null) {
        continue;
      }
      AdvancementCriterionBuilder.addAndRequire(
        criteria,
        requirements,
        criterionName,
        criterion
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
