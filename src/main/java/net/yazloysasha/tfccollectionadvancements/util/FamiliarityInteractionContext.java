package net.yazloysasha.tfccollectionadvancements.util;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Player in {@code interactOn} while familiarity may change on the server.
 */
public final class FamiliarityInteractionContext {

  private static final ThreadLocal<Player> INTERACTING = new ThreadLocal<>();

  private FamiliarityInteractionContext() {}

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
}
