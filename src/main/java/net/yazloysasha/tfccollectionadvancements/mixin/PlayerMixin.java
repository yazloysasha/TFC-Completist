package net.yazloysasha.tfccollectionadvancements.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yazloysasha.tfccollectionadvancements.tracking.FamiliarityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

  @Inject(method = "interactOn", at = @At("HEAD"))
  private void tfcCollectionAdvancements$trackInteraction(
    Entity entity,
    InteractionHand hand,
    CallbackInfoReturnable<InteractionResult> cir
  ) {
    FamiliarityTracker.set((Player) (Object) this);
  }

  @Inject(method = "interactOn", at = @At("RETURN"))
  private void tfcCollectionAdvancements$clearInteraction(
    Entity entity,
    InteractionHand hand,
    CallbackInfoReturnable<InteractionResult> cir
  ) {
    FamiliarityTracker.set(null);
  }
}
