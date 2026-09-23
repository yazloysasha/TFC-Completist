package net.yazloysasha.tfccompletist;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.yazloysasha.tfccompletist.criterion.CollectionTriggers;
import org.slf4j.Logger;

@Mod(TFCCompletist.MOD_ID)
public final class TFCCompletist {

  public static final String MOD_ID = "tfc_completist";
  public static final Logger LOGGER = LogUtils.getLogger();

  public TFCCompletist(IEventBus modEventBus) {
    CollectionTriggers.register(modEventBus);
  }
}
