package net.yazloysasha.tfccollectionadvancements.advancement;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;
import net.yazloysasha.tfccollectionadvancements.util.AddonNamespaces;
import net.yazloysasha.tfccollectionadvancements.util.BredAnimalTrigger;
import net.yazloysasha.tfccollectionadvancements.util.DrinkFluidTracker;
import net.yazloysasha.tfccollectionadvancements.util.DrinkFluidTrigger;

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

  public static final DeferredHolder<
    CriterionTrigger<?>,
    BredAnimalTrigger
  > BRED_ANIMAL = TRIGGERS.register("bred_animal", BredAnimalTrigger::new);

  private static final double BRED_ANIMAL_RANGE = 16.0;

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

  public static void onBredAnimal(LivingEntity animal) {
    if (!(animal.level() instanceof ServerLevel level)) {
      return;
    }
    ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(
      animal.getType()
    );
    if (
      entityId == null ||
      !AddonNamespaces.isDiscoverable(entityId.getNamespace())
    ) {
      return;
    }
    AABB box = animal.getBoundingBox().inflate(BRED_ANIMAL_RANGE);
    for (ServerPlayer player : level.getEntitiesOfClass(
      ServerPlayer.class,
      box
    )) {
      BRED_ANIMAL.get().trigger(player, entityId);
    }
  }
}
