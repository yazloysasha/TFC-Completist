package net.yazloysasha.tfccompletist.tracking;

import java.util.IdentityHashMap;
import java.util.Map;
import net.dries007.tfc.common.entities.livestock.Age;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.yazloysasha.tfccompletist.criterion.CollectionTriggers;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks the player in {@code interactOn} and fires familiarized-animal when
 * TFC familiarity crosses the adult cap (or 100% as a child).
 */
public final class FamiliarityTracker {

  private static final float EPSILON = 1e-4f;
  private static final ThreadLocal<Player> INTERACTING = new ThreadLocal<>();
  private static final ThreadLocal<Map<Object, Float>> PREVIOUS =
    ThreadLocal.withInitial(IdentityHashMap::new);

  private FamiliarityTracker() {}

  public static void set(@Nullable Player player) {
    if (player == null) {
      INTERACTING.remove();
    } else {
      INTERACTING.set(player);
    }
  }

  @Nullable
  public static Player player() {
    return INTERACTING.get();
  }

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
    if (!(player() instanceof ServerPlayer serverPlayer)) {
      return;
    }
    CollectionTriggers.onFamiliarized(serverPlayer, entity);
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
