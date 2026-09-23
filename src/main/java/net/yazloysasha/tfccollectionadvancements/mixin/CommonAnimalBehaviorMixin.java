package net.yazloysasha.tfccollectionadvancements.mixin;

import net.dries007.tfc.common.entities.livestock.CommonAnimalBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.yazloysasha.tfccollectionadvancements.tracking.Familiarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommonAnimalBehavior.class)
public interface CommonAnimalBehaviorMixin {
  @Inject(method = "setFamiliarity", at = @At("HEAD"), remap = false)
  private void tfcCollectionAdvancements$beforeFamiliarity(
    float value,
    CallbackInfo ci
  ) {
    CommonAnimalBehavior behavior = (CommonAnimalBehavior) this;
    Familiarity.beforeFamiliarityChange(behavior, behavior.getFamiliarity());
  }

  @Inject(method = "setFamiliarity", at = @At("TAIL"), remap = false)
  private void tfcCollectionAdvancements$afterFamiliarity(
    float value,
    CallbackInfo ci
  ) {
    CommonAnimalBehavior behavior = (CommonAnimalBehavior) this;
    Familiarity.afterFamiliarityChange(
      this,
      (LivingEntity) this,
      behavior.getFamiliarity()
    );
  }
}
