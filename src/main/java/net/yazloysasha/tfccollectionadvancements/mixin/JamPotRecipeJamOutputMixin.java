package net.yazloysasha.tfccollectionadvancements.mixin;

import net.dries007.tfc.common.blockentities.IPotInventory;
import net.dries007.tfc.common.recipes.JamPotRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yazloysasha.tfccollectionadvancements.advancement.TFCCollectionAdvancementTriggers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JamPotRecipe.JamOutput.class)
public abstract class JamPotRecipeJamOutputMixin {

  @Shadow
  private ItemStack sealedStack;

  @Inject(
    method = "onInteract",
    at = @At(
      value = "INVOKE",
      target = "Lnet/neoforged/neoforge/items/ItemHandlerHelper;giveItemToPlayer(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
      ordinal = 1,
      shift = At.Shift.BEFORE
    )
  )
  private void tfcCollectionAdvancements$trackSealedJar(
    IPotInventory entity,
    Player player,
    ItemStack clickedWith,
    CallbackInfoReturnable<ItemInteractionResult> cir
  ) {
    if (player instanceof ServerPlayer serverPlayer && !sealedStack.isEmpty()) {
      ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(
        sealedStack.getItem()
      );
      if (itemId != null) {
        TFCCollectionAdvancementTriggers.onSealedJar(serverPlayer, itemId);
      }
    }
  }
}
