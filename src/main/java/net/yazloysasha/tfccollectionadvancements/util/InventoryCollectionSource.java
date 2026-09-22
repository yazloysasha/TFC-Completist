package net.yazloysasha.tfccollectionadvancements.util;

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
  boolean includeTfc
) {
  public InventoryCollectionSource(String namespace, String pathPrefix) {
    this(namespace, pathPrefix, null, false, null, false, null, true);
  }

  public InventoryCollectionSource(
    String namespace,
    String pathPrefix,
    String pathSuffix,
    boolean exactPath
  ) {
    this(namespace, pathPrefix, pathSuffix, exactPath, null, false, null, true);
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
      true
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
      false
    );
  }

  public static InventoryCollectionSource mineralPieces() {
    return new InventoryCollectionSource(
      null,
      "ore/",
      null,
      false,
      null,
      true,
      null,
      true
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
      true
    );
  }

  public String displayNamespace() {
    if (namespace != null) {
      return namespace;
    }
    return includeTfc ? "discovered" : "addon";
  }

  public boolean matches(Holder<Item> holder) {
    var itemId = holder.getKey().location();
    if (!matchesNamespace(itemId.getNamespace())) {
      return false;
    }
    if (tag != null && !holder.is(tag)) {
      return false;
    }
    if (!matchesItemPath(itemId.getPath())) {
      return false;
    }
    if (rejectBlockItems && holder.value() instanceof BlockItem) {
      return false;
    }
    if (isOrePieceSource() && holder.is(TFCTags.Items.METAL_ORES)) {
      return false;
    }
    if (gemOre != null && hasGemCounterpart(itemId) != gemOre) {
      return false;
    }
    return true;
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
      !InventoryCollectionAdvancementPatch.matchesPathPrefix(path, pathPrefix)
    ) {
      return false;
    }
    if (pathSuffix != null && !path.endsWith(pathSuffix)) {
      return false;
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
