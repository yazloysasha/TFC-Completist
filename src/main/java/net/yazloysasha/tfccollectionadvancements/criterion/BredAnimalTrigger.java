package net.yazloysasha.tfccollectionadvancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class BredAnimalTrigger
  extends SimpleCriterionTrigger<BredAnimalTrigger.TriggerInstance> {

  public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
    instance ->
      instance
        .group(
          EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(
            TriggerInstance::player
          ),
          ResourceLocation.CODEC.optionalFieldOf("entity").forGetter(
            TriggerInstance::entity
          )
        )
        .apply(instance, TriggerInstance::new)
  );

  @Override
  public Codec<TriggerInstance> codec() {
    return CODEC;
  }

  public void trigger(ServerPlayer player, ResourceLocation entityId) {
    trigger(player, instance -> instance.matches(entityId));
  }

  public record TriggerInstance(
    Optional<ContextAwarePredicate> player,
    Optional<ResourceLocation> entity
  )
    implements SimpleInstance {
    public boolean matches(ResourceLocation bredEntityId) {
      return entity.isEmpty() || entity.get().equals(bredEntityId);
    }
  }
}
