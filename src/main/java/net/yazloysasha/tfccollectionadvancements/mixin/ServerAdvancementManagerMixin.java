package net.yazloysasha.tfccollectionadvancements.mixin;

import com.google.gson.JsonElement;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.AtGrandmasAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.BerryGardenerAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.BreakfastLunchAndDinnerAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.ButcherAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.CarnivoreAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.CarpenterAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.DomesticationAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.FloristAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.GeologistAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.HighArchitectureAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.HusbandmanAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.JewelerAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.KingOfBeastsAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.OrchardistAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.PedologistAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.ProspectorAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.ReefKeeperAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.SandsOfTheWorldAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.SeaCookAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.SommelierAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.StonemasonAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.VagabondAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.custom.VegetarianAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.patch.AdventuringTimeAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.patch.ArboristAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.patch.GemologistAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.patch.GoneFishingAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.patch.HealthyDietAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.patch.MetallurgistAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.patch.MinerologistAdvancement;
import net.yazloysasha.tfccollectionadvancements.advancement.patch.TrueFarmerAdvancement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerAdvancementManager.class)
public abstract class ServerAdvancementManagerMixin {

  @Shadow
  @Final
  private HolderLookup.Provider registries;

  @Inject(method = "apply", at = @At("HEAD"))
  private void tfcCollectionAdvancements$extendCollectionAdvancements(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    ProfilerFiller profiler,
    CallbackInfo ci
  ) {
    // Patch
    AdventuringTimeAdvancement.patch(
      advancements,
      resourceManager,
      this.registries
    );
    ArboristAdvancement.patch(advancements, resourceManager, this.registries);
    GemologistAdvancement.patch(advancements, resourceManager, this.registries);
    GoneFishingAdvancement.patch(
      advancements,
      resourceManager,
      this.registries
    );
    HealthyDietAdvancement.patch(
      advancements,
      resourceManager,
      this.registries
    );
    MetallurgistAdvancement.patch(
      advancements,
      resourceManager,
      this.registries
    );
    MinerologistAdvancement.patch(
      advancements,
      resourceManager,
      this.registries
    );
    TrueFarmerAdvancement.patch(advancements, resourceManager, this.registries);

    // Custom
    AtGrandmasAdvancement.patch(advancements, resourceManager, this.registries);
    BerryGardenerAdvancement.patch(
      advancements,
      resourceManager,
      this.registries
    );
    BreakfastLunchAndDinnerAdvancement.patch(
      advancements,
      resourceManager,
      this.registries
    );
    ButcherAdvancement.patch(advancements, resourceManager, this.registries);
    CarnivoreAdvancement.patch(advancements, resourceManager, this.registries);
    CarpenterAdvancement.patch(advancements, resourceManager, this.registries);
    DomesticationAdvancement.patch(
      advancements,
      resourceManager,
      this.registries
    );
    FloristAdvancement.patch(advancements, resourceManager, this.registries);
    GeologistAdvancement.patch(advancements, resourceManager, this.registries);
    HighArchitectureAdvancement.patch(
      advancements,
      resourceManager,
      this.registries
    );
    HusbandmanAdvancement.patch(advancements, resourceManager, this.registries);
    JewelerAdvancement.patch(advancements, resourceManager, this.registries);
    KingOfBeastsAdvancement.patch(
      advancements,
      resourceManager,
      this.registries
    );
    OrchardistAdvancement.patch(advancements, resourceManager, this.registries);
    PedologistAdvancement.patch(advancements, resourceManager, this.registries);
    ProspectorAdvancement.patch(advancements, resourceManager, this.registries);
    ReefKeeperAdvancement.patch(advancements, resourceManager, this.registries);
    SandsOfTheWorldAdvancement.patch(
      advancements,
      resourceManager,
      this.registries
    );
    SeaCookAdvancement.patch(advancements, resourceManager, this.registries);
    SommelierAdvancement.patch(advancements, resourceManager, this.registries);
    StonemasonAdvancement.patch(advancements, resourceManager, this.registries);
    VagabondAdvancement.patch(advancements, resourceManager, this.registries);
    VegetarianAdvancement.patch(advancements, resourceManager, this.registries);
  }
}
