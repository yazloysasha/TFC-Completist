package net.yazloysasha.tfccollectionadvancements.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class SealJarTrigger
  extends SimpleCriterionTrigger<SealJarTrigger.TriggerInstance> {

  public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
    instance ->
      instance
        .group(
          EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(
            TriggerInstance::player
          ),
          ResourceLocation.CODEC.optionalFieldOf("item").forGetter(
            TriggerInstance::item
          )
        )
        .apply(instance, TriggerInstance::new)
  );

  @Override
  public Codec<TriggerInstance> codec() {
    return CODEC;
  }

  public void trigger(ServerPlayer player, ResourceLocation itemId) {
    trigger(player, instance -> instance.matches(itemId));
  }

  public record TriggerInstance(
    Optional<ContextAwarePredicate> player,
    Optional<ResourceLocation> item
  )
    implements SimpleInstance {
    public boolean matches(ResourceLocation sealedItemId) {
      return item.isEmpty() || item.get().equals(sealedItemId);
    }
  }
}
