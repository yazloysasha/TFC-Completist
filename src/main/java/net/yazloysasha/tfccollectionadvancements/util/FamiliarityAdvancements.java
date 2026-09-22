package net.yazloysasha.tfccollectionadvancements.util;

import java.util.IdentityHashMap;
import java.util.Map;
import net.dries007.tfc.common.entities.livestock.Age;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.yazloysasha.tfccollectionadvancements.advancement.TFCCollectionAdvancementTriggers;

public final class FamiliarityAdvancements {

  private static final float EPSILON = 1e-4f;
  private static final ThreadLocal<Map<Object, Float>> PREVIOUS =
    ThreadLocal.withInitial(IdentityHashMap::new);

  private FamiliarityAdvancements() {}

  public static void beforeFamiliarityChange(Object behavior, float previous) {
    PREVIOUS.get().put(behavior, previous);
  }

  public static void afterFamiliarityChange(
    Object behavior,
    LivingEntity entity,
    float updated
  ) {
    Float previous = PREVIOUS.get().remove(behavior);
    if (previous == null || entity.level().isClientSide()) {
      return;
    }
    if (!(entity instanceof TFCAnimalProperties properties)) {
      return;
    }
    float cap = familiarityCap(properties);
    if (!crossedCap(previous, updated, cap)) {
      return;
    }
    if (
      !(FamiliarityInteractionContext.player() instanceof ServerPlayer player)
    ) {
      return;
    }
    TFCCollectionAdvancementTriggers.onFamiliarized(player, entity);
  }

  private static float familiarityCap(TFCAnimalProperties properties) {
    return properties.getAgeType() == Age.CHILD
      ? 1.0f
      : properties.getAdultFamiliarityCap();
  }

  private static boolean crossedCap(float before, float after, float cap) {
    return before < cap - EPSILON && after >= cap - EPSILON;
  }
}
