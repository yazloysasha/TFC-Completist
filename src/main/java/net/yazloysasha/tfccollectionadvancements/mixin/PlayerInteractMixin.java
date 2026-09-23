package net.yazloysasha.tfccollectionadvancements.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yazloysasha.tfccollectionadvancements.tracking.Familiarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerInteractMixin {

  @Inject(method = "interactOn", at = @At("HEAD"))
  private void tfcCollectionAdvancements$trackInteraction(
    Entity entity,
    InteractionHand hand,
    CallbackInfoReturnable<InteractionResult> cir
  ) {
    Familiarity.set((Player) (Object) this);
  }

  @Inject(method = "interactOn", at = @At("RETURN"))
  private void tfcCollectionAdvancements$clearInteraction(
    Entity entity,
    InteractionHand hand,
    CallbackInfoReturnable<InteractionResult> cir
  ) {
    Familiarity.set(null);
  }
}
