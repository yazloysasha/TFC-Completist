package net.yazloysasha.tfccollectionadvancements.advancement;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * TFC's {@code Drinkable.onDrink} only receives the player and volume. Callers
 * always resolve the fluid first via {@code Drinkable.get}, so the last looked-up
 * fluid is the one being consumed.
 */
public final class DrinkFluidTracker {

  private static final ThreadLocal<Fluid> CURRENT = new ThreadLocal<>();

  private DrinkFluidTracker() {}

  public static void set(Fluid fluid) {
    if (fluid != null && !fluid.isSame(Fluids.EMPTY)) {
      CURRENT.set(fluid);
    }
  }

  public static Fluid take() {
    Fluid fluid = CURRENT.get();
    CURRENT.remove();
    return fluid;
  }

  public static ResourceLocation canonicalId(Fluid fluid) {
    if (fluid == null || fluid.isSame(Fluids.EMPTY)) {
      return null;
    }
    Fluid source = fluid instanceof FlowingFluid flowing
      ? flowing.getSource()
      : fluid;
    ResourceLocation id = BuiltInRegistries.FLUID.getKey(source);
    if (id == null) {
      return null;
    }
    String path = id.getPath();
    if (path.startsWith("flowing_")) {
      ResourceLocation still = id.withPath(path.substring("flowing_".length()));
      if (BuiltInRegistries.FLUID.containsKey(still)) {
        return still;
      }
    }
    return id;
  }
}
