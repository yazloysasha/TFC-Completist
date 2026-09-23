package net.yazloysasha.tfccollectionadvancements.collection;

import java.util.List;
import java.util.Set;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.yazloysasha.tfccollectionadvancements.discover.Namespaces;

public record InventorySource(
  String namespace,
  String pathPrefix,
  String pathSuffix,
  boolean exactPath,
  TagKey<Item> tag,
  boolean rejectBlockItems,
  Boolean gemOre,
  boolean includeTfc,
  CollectedItems collectedItems,
  boolean singleSegmentAfterPrefix,
  List<TagKey<Item>> excludeTags,
  List<ResourceLocation> excludeItems
) {
  public enum CollectedItems {
    NONE,
    TFC_FARMLAND_SEEDS,
    MINERAL_ORE_DROPS,
    SALT_WATER_CORAL_ITEMS,
  }

  public static InventorySource discovered(String pathPrefix) {
    return discovered(pathPrefix, null);
  }

  public static InventorySource discovered(
    String pathPrefix,
    String pathSuffix
  ) {
    return new InventorySource(
      null,
      pathPrefix,
      pathSuffix,
      false,
      null,
      false,
      null,
      true,
      CollectedItems.NONE,
      false,
      List.of(),
      List.of()
    );
  }

  /** Base block only ({@code rock/smooth/granite}), not slabs, stairs, or walls. */
  public static InventorySource discoveredPrimaryBlock(String pathPrefix) {
    return new InventorySource(
      null,
      pathPrefix,
      null,
      false,
      null,
      false,
      null,
      true,
      CollectedItems.NONE,
      true,
      List.of(),
      List.of()
    );
  }

  public static InventorySource discoveredExactItem(ResourceLocation itemId) {
    return new InventorySource(
      null,
      itemId.getPath(),
      null,
      true,
      null,
      false,
      null,
      true,
      CollectedItems.NONE,
      false,
      List.of(),
      List.of()
    );
  }

  public static InventorySource discoveredTag(TagKey<Item> tag) {
    return discoveredTagExcluding(tag);
  }

  public static InventorySource discoveredTagExcluding(
    TagKey<Item> tag,
    TagKey<Item>... excludeTags
  ) {
    return new InventorySource(
      null,
      null,
      null,
      false,
      tag,
      false,
      null,
      true,
      CollectedItems.NONE,
      false,
      List.of(excludeTags),
      List.of()
    );
  }

  public static InventorySource discoveredTagExcludingItems(
    TagKey<Item> tag,
    List<ResourceLocation> excludeItems,
    TagKey<Item>... excludeTags
  ) {
    return new InventorySource(
      null,
      null,
      null,
      false,
      tag,
      false,
      null,
      true,
      CollectedItems.NONE,
      false,
      List.of(excludeTags),
      excludeItems
    );
  }

  public static InventorySource gemPieces() {
    return new InventorySource(
      null,
      "ore/",
      null,
      false,
      null,
      true,
      true,
      true,
      CollectedItems.NONE,
      false,
      List.of(),
      List.of()
    );
  }

  public static InventorySource tfcFarmlandCrops() {
    return new InventorySource(
      null,
      null,
      null,
      false,
      null,
      false,
      null,
      true,
      CollectedItems.TFC_FARMLAND_SEEDS,
      false,
      List.of(),
      List.of()
    );
  }

  public static InventorySource mineralOreDrops() {
    return new InventorySource(
      null,
      null,
      null,
      false,
      null,
      false,
      null,
      true,
      CollectedItems.MINERAL_ORE_DROPS,
      false,
      List.of(),
      List.of()
    );
  }

  public static InventorySource saltWaterCorals() {
    return new InventorySource(
      null,
      null,
      null,
      false,
      null,
      false,
      null,
      true,
      CollectedItems.SALT_WATER_CORAL_ITEMS,
      false,
      List.of(),
      List.of()
    );
  }

  public String displayNamespace() {
    if (namespace != null) {
      return namespace;
    }
    return switch (collectedItems) {
      case TFC_FARMLAND_SEEDS -> "tfc farmland";
      case MINERAL_ORE_DROPS -> "mineral ore drops";
      case SALT_WATER_CORAL_ITEMS -> "salt water coral";
      case NONE -> includeTfc ? "discovered" : "addon";
    };
  }

  public boolean matches(
    Holder<Item> holder,
    Set<ResourceLocation> tagMembers,
    Set<ResourceLocation> metalOres
  ) {
    var itemId = holder.getKey().location();
    if (!matchesNamespace(itemId.getNamespace())) {
      return false;
    }
    if (collectedItems != CollectedItems.NONE) {
      return tagMembers.contains(itemId);
    }
    if (tag != null && !isInTag(itemId, holder, tagMembers)) {
      return false;
    }
    if (!matchesItemPath(itemId.getPath())) {
      return false;
    }
    if (rejectBlockItems && holder.value() instanceof BlockItem) {
      return false;
    }
    if (isOrePieceSource() && isMetalOre(itemId, holder, metalOres)) {
      return false;
    }
    if (gemOre != null && hasGemCounterpart(itemId) != gemOre) {
      return false;
    }
    return true;
  }

  private boolean isInTag(
    ResourceLocation itemId,
    Holder<Item> holder,
    Set<ResourceLocation> tagMembers
  ) {
    if (!tagMembers.isEmpty()) {
      return tagMembers.contains(itemId);
    }
    return holder.is(tag);
  }

  private boolean isMetalOre(
    ResourceLocation itemId,
    Holder<Item> holder,
    Set<ResourceLocation> metalOres
  ) {
    if (!metalOres.isEmpty()) {
      return metalOres.contains(itemId);
    }
    return holder.is(TFCTags.Items.METAL_ORES);
  }

  public boolean matchesNamespace(String itemNamespace) {
    if (namespace != null) {
      return namespace.equals(itemNamespace);
    }
    if (includeTfc) {
      return Namespaces.isDiscoverable(itemNamespace);
    }
    return Namespaces.isAddon(itemNamespace);
  }

  public boolean matchesItemPath(String path) {
    if (exactPath) {
      if (!path.equals(pathPrefix)) {
        return false;
      }
    } else if (
      pathPrefix != null &&
      !InventoryCollections.matchesPathPrefix(path, pathPrefix)
    ) {
      return false;
    }
    if (pathSuffix != null && !path.endsWith(pathSuffix)) {
      return false;
    }
    if (
      pathSuffix != null &&
      pathPrefix != null &&
      pathPrefix.endsWith("/") &&
      path.substring(pathPrefix.length()).indexOf('/') >= 0
    ) {
      return false;
    }
    if (singleSegmentAfterPrefix && pathPrefix != null) {
      if (!InventoryCollections.matchesPathPrefix(path, pathPrefix)) {
        return false;
      }
      String remainder = path.substring(pathPrefix.length());
      if (
        remainder.isEmpty() ||
        remainder.indexOf('/') >= 0 ||
        isDecorationVariant(remainder)
      ) {
        return false;
      }
    }
    return !rejectBlockItems || isHeldOrePiecePath(path);
  }

  /**
   * Slabs, stairs, and walls of {@code name}, not names that merely contain {@code _}.
   */
  private static boolean isDecorationVariant(String remainder) {
    return (
      remainder.endsWith("_slab") ||
      remainder.endsWith("_stairs") ||
      remainder.endsWith("_wall")
    );
  }

  private boolean isOrePieceSource() {
    return rejectBlockItems || gemOre != null;
  }

  static boolean hasGemCounterpart(ResourceLocation oreId) {
    String path = oreId.getPath();
    if (!path.startsWith("ore/")) {
      return false;
    }
    String name = path.substring("ore/".length());
    if (name.isEmpty() || name.indexOf('/') >= 0) {
      return false;
    }
    return (
      BuiltInRegistries.ITEM.containsKey(
        ResourceLocation.fromNamespaceAndPath(
          oreId.getNamespace(),
          "gem/" + name
        )
      ) ||
      BuiltInRegistries.ITEM.containsKey(
        ResourceLocation.fromNamespaceAndPath("tfc", "gem/" + name)
      )
    );
  }

  static boolean isHeldOrePiecePath(String path) {
    if (!path.startsWith("ore/")) {
      return true;
    }
    String remainder = path.substring("ore/".length());
    if (remainder.isEmpty() || remainder.indexOf('/') >= 0) {
      return false;
    }
    return (
      !remainder.startsWith("poor_") &&
      !remainder.startsWith("normal_") &&
      !remainder.startsWith("rich_") &&
      !remainder.startsWith("small_")
    );
  }
}
