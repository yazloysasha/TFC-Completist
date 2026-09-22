package net.yazloysasha.tfccollectionadvancements.advancement;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

public final class TFCCollectionAdvancementTriggers {

  public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
    DeferredRegister.create(
      Registries.TRIGGER_TYPE,
      TFCCollectionAdvancements.MOD_ID
    );

  public static final DeferredHolder<
    CriterionTrigger<?>,
    DrinkFluidTrigger
  > DRINK_FLUID = TRIGGERS.register("drink_fluid", DrinkFluidTrigger::new);

  private TFCCollectionAdvancementTriggers() {}

  public static void register(IEventBus modEventBus) {
    TRIGGERS.register(modEventBus);
  }

  public static void onDrinkFluid(Player player, Fluid fluid) {
    if (!(player instanceof ServerPlayer serverPlayer)) {
      return;
    }
    ResourceLocation fluidId = DrinkFluidTracker.canonicalId(fluid);
    if (fluidId != null) {
      DRINK_FLUID.get().trigger(serverPlayer, fluidId);
    }
  }
}
