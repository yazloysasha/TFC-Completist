package net.yazloysasha.tfccollectionadvancements.tracking;

import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Last player who called {@code TFCAnimalProperties#eatFood} on this animal.
 */
public final class LastFedAnimalTracker {

  private static final WeakHashMap<LivingEntity, UUID> LAST_FEEDER =
    new WeakHashMap<>();

  private LastFedAnimalTracker() {}

  public static void remember(LivingEntity animal, Player player) {
    if (player instanceof ServerPlayer serverPlayer) {
      LAST_FEEDER.put(animal, serverPlayer.getUUID());
    }
  }

  public static ServerPlayer feeder(LivingEntity animal) {
    UUID id = LAST_FEEDER.get(animal);
    if (id == null || !(animal.level() instanceof ServerLevel level)) {
      return null;
    }
    return level.getServer().getPlayerList().getPlayer(id);
  }
}
