package net.yazloysasha.tfccollectionadvancements.mixin;

import net.dries007.tfc.common.player.IPlayerInfo;
import net.dries007.tfc.util.data.Drinkable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.yazloysasha.tfccollectionadvancements.advancement.DrinkFluidTracker;
import net.yazloysasha.tfccollectionadvancements.advancement.TFCCollectionAdvancementTriggers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Drinkable.class)
public abstract class DrinkableMixin {

  @Inject(method = "get", at = @At("RETURN"))
  private static void tfcCollectionAdvancements$rememberFluid(
    Fluid fluid,
    CallbackInfoReturnable<Drinkable> cir
  ) {
    if (cir.getReturnValue() != null) {
      DrinkFluidTracker.set(fluid);
    }
  }

  @Inject(
    method = "doDrink",
    at = @At(
      value = "INVOKE",
      target = "Lnet/dries007/tfc/util/data/Drinkable;onDrink(Lnet/minecraft/world/entity/player/Player;I)V"
    )
  )
  private static void tfcCollectionAdvancements$rememberWorldFluid(
    Level level,
    Player player,
    BlockState state,
    BlockPos pos,
    IPlayerInfo info,
    Drinkable drinkable,
    CallbackInfo ci
  ) {
    DrinkFluidTracker.set(state.getFluidState().getType());
  }

  @Inject(method = "onDrink", at = @At("HEAD"))
  private void tfcCollectionAdvancements$trackDrink(
    Player player,
    int amount,
    CallbackInfo ci
  ) {
    TFCCollectionAdvancementTriggers.onDrinkFluid(
      player,
      DrinkFluidTracker.take()
    );
  }
}
