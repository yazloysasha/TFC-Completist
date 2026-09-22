package net.yazloysasha.tfccollectionadvancements.mixin;

import com.google.gson.JsonElement;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.yazloysasha.tfccollectionadvancements.advancement.SaplingsAdvancementPatch;
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
  private void tfcCollectionAdvancements$extendSaplingsAdvancement(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    ProfilerFiller profiler,
    CallbackInfo ci
  ) {
    SaplingsAdvancementPatch.patch(advancements, this.registries);
  }
}
