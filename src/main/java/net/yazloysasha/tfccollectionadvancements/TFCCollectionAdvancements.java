package net.yazloysasha.tfccollectionadvancements;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.yazloysasha.tfccollectionadvancements.criterion.CollectionCriteria;
import org.slf4j.Logger;

@Mod(TFCCollectionAdvancements.MOD_ID)
public final class TFCCollectionAdvancements {

  public static final String MOD_ID = "tfc_collection_advancements";
  public static final Logger LOGGER = LogUtils.getLogger();

  public TFCCollectionAdvancements(IEventBus modEventBus) {
    CollectionCriteria.register(modEventBus);
  }
}
