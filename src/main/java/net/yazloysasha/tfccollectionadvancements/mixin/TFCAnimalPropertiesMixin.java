package net.yazloysasha.tfccollectionadvancements.mixin;

import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.yazloysasha.tfccollectionadvancements.advancement.TFCCollectionAdvancementTriggers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TFC livestock do not call {@code minecraft:bred_animals}. Mating only goes
 * through {@link TFCAnimalProperties#onFertilized}: brain {@code BreedBehavior},
 * horse {@code findFemaleMate} + vanilla love, and addon animals that use the
 * same API. Birth via {@code getBreedOffspring(this, this)} does not call this.
 */
@Mixin(TFCAnimalProperties.class)
public interface TFCAnimalPropertiesMixin {
  @Inject(method = "onFertilized", at = @At("TAIL"), remap = false)
  private void tfcCollectionAdvancements$trackBreed(
    TFCAnimalProperties male,
    CallbackInfo ci
  ) {
    TFCCollectionAdvancementTriggers.onBredAnimal(
      ((TFCAnimalProperties) this).getEntity()
    );
  }
}
