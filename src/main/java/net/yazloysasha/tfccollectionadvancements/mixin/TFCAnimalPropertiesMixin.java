package net.yazloysasha.tfccollectionadvancements.mixin;

import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yazloysasha.tfccollectionadvancements.criterion.CollectionTriggers;
import net.yazloysasha.tfccollectionadvancements.tracking.LastFedAnimalTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TFC livestock do not call {@code minecraft:bred_animals}. Mating goes through
 * {@link TFCAnimalProperties#onFertilized}. Feeding is remembered so breed
 * credit is the last player who fed, not everyone nearby.
 */
@Mixin(TFCAnimalProperties.class)
public interface TFCAnimalPropertiesMixin {
  @Inject(method = "eatFood", at = @At("TAIL"), remap = false)
  private void tfcCollectionAdvancements$rememberFeeder(
    ItemStack stack,
    InteractionHand hand,
    Player player,
    CallbackInfoReturnable<InteractionResult> cir
  ) {
    LastFedAnimalTracker.remember(
      ((TFCAnimalProperties) this).getEntity(),
      player
    );
  }

  @Inject(method = "onFertilized", at = @At("TAIL"), remap = false)
  private void tfcCollectionAdvancements$trackBreed(
    TFCAnimalProperties male,
    CallbackInfo ci
  ) {
    CollectionTriggers.onBredAnimal(
      ((TFCAnimalProperties) this).getEntity(),
      male.getEntity()
    );
  }
}
