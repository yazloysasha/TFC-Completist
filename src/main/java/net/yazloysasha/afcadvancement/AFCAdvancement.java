package net.yazloysasha.afcadvancement;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(AFCAdvancement.MOD_ID)
public final class AFCAdvancement {

  public static final String MOD_ID = "afc_advancement";
  public static final Logger LOGGER = LogUtils.getLogger();

  public AFCAdvancement(IEventBus modEventBus) {}
}
