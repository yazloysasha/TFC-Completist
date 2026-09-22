package net.yazloysasha.tfccollectionadvancements.util;

public record InventoryCollectionSource(String namespace, String pathPrefix) {
  public String criterionPrefix() {
    return namespace + "_";
  }
}
