package net.yazloysasha.tfccollectionadvancements.util;

public record InventoryCollectionSource(
  String namespace,
  String pathPrefix,
  String pathSuffix,
  boolean exactPath
) {
  public InventoryCollectionSource(String namespace, String pathPrefix) {
    this(namespace, pathPrefix, null, false);
  }

  public String criterionPrefix() {
    return "tfc".equals(namespace) ? "" : namespace + "_";
  }

  public boolean matchesItemPath(String path) {
    if (exactPath) {
      if (!path.equals(pathPrefix)) {
        return false;
      }
    } else if (
      !InventoryCollectionAdvancementPatch.matchesPathPrefix(path, pathPrefix)
    ) {
      return false;
    }
    return pathSuffix == null || path.endsWith(pathSuffix);
  }
}
