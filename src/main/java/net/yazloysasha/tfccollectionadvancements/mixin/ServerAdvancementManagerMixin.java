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
import net.yazloysasha.tfccollectionadvancements.advancement.GoneFishingAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.HealthyDietAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.MetallurgistAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.MinerologistAdvancementPatch;
import net.yazloysasha.tfccollectionadvancements.advancement.TrueFarmerAdvancementPatch;
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
    ArboristAdvancementPatch.patch(advancements, this.registries);
    GemologistAdvancementPatch.patch(advancements, this.registries);
    GoneFishingAdvancementPatch.patch(advancements, this.registries);
    HealthyDietAdvancementPatch.patch(advancements, this.registries);
    MetallurgistAdvancementPatch.patch(advancements, this.registries);
    MinerologistAdvancementPatch.patch(advancements, this.registries);
    TrueFarmerAdvancementPatch.patch(advancements, this.registries);
  }
}
