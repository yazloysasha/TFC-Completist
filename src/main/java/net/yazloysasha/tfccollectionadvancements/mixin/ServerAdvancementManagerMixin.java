package net.yazloysasha.tfccollectionadvancements.mixin;

import com.google.gson.JsonElement;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.yazloysasha.tfccollectionadvancements.advancement.AdventuringTimeAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.ArboristAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.GemologistAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.GeologistAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.GoneFishingAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.HealthyDietAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.MetallurgistAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.MinerologistAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.PedologistAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.SandsOfTheWorldAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.StonemasonAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.TrueFarmerAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.VegetarianAdvancementPatch;
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
    AdventuringTimeAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
    ArboristAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
    GemologistAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
    GeologistAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
    GoneFishingAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
    HealthyDietAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
    MetallurgistAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
    MinerologistAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
    PedologistAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
    SandsOfTheWorldAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
    StonemasonAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
    TrueFarmerAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
    VegetarianAdvancementPatch.patch(
      advancements,
      resourceManager,
      this.registries
    );
  }
}
