package net.yazloysasha.tfccompletist.discover;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;

public final class Namespaces {

  private Namespaces() {}

  public static boolean isMinecraft(String namespace) {
    return "minecraft".equals(namespace);
  }

  public static boolean isTfc(String namespace) {
    return "tfc".equals(namespace);
  }

  public static boolean isAddon(String namespace) {
    return !isTfc(namespace) && !isMinecraft(namespace);
  }

  public static boolean isDiscoverable(String namespace) {
    return !isMinecraft(namespace);
  }

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
