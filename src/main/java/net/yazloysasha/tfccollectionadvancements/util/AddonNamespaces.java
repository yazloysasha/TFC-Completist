package net.yazloysasha.tfccollectionadvancements.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;

public final class AddonNamespaces {

  private AddonNamespaces() {}

  public static boolean isPresent(
    HolderLookup.Provider registries,
    String namespace
  ) {
    return registries
      .lookupOrThrow(Registries.ITEM)
      .listElementIds()
      .anyMatch(id -> namespace.equals(id.location().getNamespace()));
  }
}
