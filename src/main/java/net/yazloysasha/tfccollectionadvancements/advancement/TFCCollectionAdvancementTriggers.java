package net.yazloysasha.tfccollectionadvancements.advancement;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;
import net.yazloysasha.tfccollectionadvancements.util.AddonNamespaces;
import net.yazloysasha.tfccollectionadvancements.util.BredAnimalTrigger;
import net.yazloysasha.tfccollectionadvancements.util.DrinkFluidTracker;
import net.yazloysasha.tfccollectionadvancements.util.DrinkFluidTrigger;
import net.yazloysasha.tfccollectionadvancements.util.EntityCollectionAdvancement;
import net.yazloysasha.tfccollectionadvancements.util.FamiliarizedAnimalTrigger;
import net.yazloysasha.tfccollectionadvancements.util.LastFedAnimalTracker;
import net.yazloysasha.tfccollectionadvancements.util.SealJarTrigger;

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

  public static final DeferredHolder<
    CriterionTrigger<?>,
    FamiliarizedAnimalTrigger
  > FAMILIARIZED_ANIMAL = TRIGGERS.register(
    "familiarized_animal",
    FamiliarizedAnimalTrigger::new
  );

  public static final DeferredHolder<
    CriterionTrigger<?>,
    SealJarTrigger
  > SEAL_JAR = TRIGGERS.register("seal_jar", SealJarTrigger::new);

  private TFCCollectionAdvancementTriggers() {}

  public static void register(IEventBus modEventBus) {
    TRIGGERS.register(modEventBus);
  }

  public static void onSealedJar(ServerPlayer player, ResourceLocation itemId) {
    if (!AddonNamespaces.isDiscoverable(itemId.getNamespace())) {
      return;
    }
    SEAL_JAR.get().trigger(player, itemId);
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

  public static void onFamiliarized(ServerPlayer player, LivingEntity animal) {
    ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(
      animal.getType()
    );
    if (
      entityId == null ||
      !AddonNamespaces.isDiscoverable(entityId.getNamespace()) ||
      EntityCollectionAdvancement.isExcludedFromFamiliarization(entityId)
    ) {
      return;
    }
    FAMILIARIZED_ANIMAL.get().trigger(player, entityId);
  }

  public static void onBredAnimal(LivingEntity female, LivingEntity male) {
    ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(
      female.getType()
    );
    if (
      entityId == null ||
      !AddonNamespaces.isDiscoverable(entityId.getNamespace()) ||
      EntityCollectionAdvancement.isExcludedFromBreeding(entityId)
    ) {
      return;
    }
    Set<ServerPlayer> breeders = new LinkedHashSet<>();
    ServerPlayer femaleFeeder = LastFedAnimalTracker.feeder(female);
    if (femaleFeeder != null) {
      breeders.add(femaleFeeder);
    }
    ServerPlayer maleFeeder = LastFedAnimalTracker.feeder(male);
    if (maleFeeder != null) {
      breeders.add(maleFeeder);
    }
    for (ServerPlayer player : breeders) {
      BRED_ANIMAL.get().trigger(player, entityId);
    }
  }
}
