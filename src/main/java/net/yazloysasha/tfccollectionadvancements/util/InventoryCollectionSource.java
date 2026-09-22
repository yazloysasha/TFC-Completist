package net.yazloysasha.tfccollectionadvancements.util;

import java.util.Set;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public record InventoryCollectionSource(
  String namespace,
  String pathPrefix,
  String pathSuffix,
  boolean exactPath,
  TagKey<Item> tag,
  boolean rejectBlockItems,
  Boolean gemOre,
  boolean includeTfc,
  CollectedItems collectedItems,
  boolean singleSegmentAfterPrefix
) {
  public enum CollectedItems {
    NONE,
    TFC_FARMLAND_SEEDS,
    MINERAL_ORE_DROPS,
  }

  public InventoryCollectionSource(String namespace, String pathPrefix) {
    this(
      namespace,
      pathPrefix,
      null,
      false,
      null,
      false,
      null,
      true,
      CollectedItems.NONE,
      false
    );
  }

  public InventoryCollectionSource(
    String namespace,
    String pathPrefix,
    String pathSuffix,
    boolean exactPath
  ) {
    this(
      namespace,
      pathPrefix,
      pathSuffix,
      exactPath,
      null,
      false,
      null,
      true,
      CollectedItems.NONE,
      false
    );
  }

  public static InventoryCollectionSource discovered(String pathPrefix) {
    return new InventoryCollectionSource(
      null,
      pathPrefix,
      null,
      false,
      null,
      false,
      null,
      true,
      CollectedItems.NONE,
      false
    );
  }

  /** Base block only ({@code rock/smooth/granite}), not slabs, stairs, or walls. */
  public static InventoryCollectionSource discoveredPrimaryBlock(
    String pathPrefix
  ) {
    return new InventoryCollectionSource(
      null,
      pathPrefix,
      null,
      false,
      null,
      false,
      null,
      true,
      CollectedItems.NONE,
      true
    );
  }

  public static InventoryCollectionSource discoveredTag(TagKey<Item> tag) {
    return new InventoryCollectionSource(
      null,
      null,
      null,
      false,
      tag,
      false,
      null,
      true,
      CollectedItems.NONE,
      false
    );
  }

  public static InventoryCollectionSource addonTag(TagKey<Item> tag) {
    return new InventoryCollectionSource(
      null,
      null,
      null,
      false,
      tag,
      false,
      null,
      false,
      CollectedItems.NONE,
      false
    );
  }

  public static InventoryCollectionSource gemPieces() {
    return new InventoryCollectionSource(
      null,
      "ore/",
      null,
      false,
      null,
      true,
      true,
      true,
      CollectedItems.NONE,
      false
    );
  }

  public static InventoryCollectionSource tfcFarmlandCrops() {
    return new InventoryCollectionSource(
      null,
      null,
      null,
      false,
      null,
      false,
      null,
      true,
      CollectedItems.TFC_FARMLAND_SEEDS,
      false
    );
  }

  public static InventoryCollectionSource mineralOreDrops() {
    return new InventoryCollectionSource(
      null,
      null,
      null,
      false,
      null,
      false,
      null,
      true,
      CollectedItems.MINERAL_ORE_DROPS,
      false
    );
  }

  public String displayNamespace() {
    if (namespace != null) {
      return namespace;
    }
    return switch (collectedItems) {
      case TFC_FARMLAND_SEEDS -> "tfc farmland";
      case MINERAL_ORE_DROPS -> "mineral ore drops";
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
      return AddonNamespaces.isDiscoverable(itemNamespace);
    }
    return AddonNamespaces.isAddon(itemNamespace);
  }

  public boolean matchesItemPath(String path) {
    if (exactPath) {
      if (!path.equals(pathPrefix)) {
        return false;
      }
    } else if (
      pathPrefix != null &&
      !InventoryCollectionAdvancement.matchesPathPrefix(path, pathPrefix)
    ) {
      return false;
    }
    if (pathSuffix != null && !path.endsWith(pathSuffix)) {
      return false;
    }
    if (singleSegmentAfterPrefix && pathPrefix != null) {
      if (!InventoryCollectionAdvancement.matchesPathPrefix(path, pathPrefix)) {
        return false;
      }
      String remainder = path.substring(pathPrefix.length());
      if (
        remainder.isEmpty() ||
        remainder.indexOf('/') >= 0 ||
        remainder.indexOf('_') >= 0
      ) {
        return false;
      }
    }
    return !rejectBlockItems || isHeldOrePiecePath(path);
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
