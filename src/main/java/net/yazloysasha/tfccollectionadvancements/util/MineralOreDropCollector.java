package net.yazloysasha.tfccollectionadvancements.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;

/**
 * Unique item drops from {@code tfc:prospectable} blocks that are not metal
 * ores or {@code #c:raw_materials}. Loot tables are read from datapack JSON
 * because they are not loaded yet when advancements are patched.
 */
public final class MineralOreDropCollector {

  private static final TagKey<Item> RAW_MATERIALS = TagKey.create(
    Registries.ITEM,
    ResourceLocation.fromNamespaceAndPath("c", "raw_materials")
  );

  private MineralOreDropCollector() {}

  public static Set<ResourceLocation> collect(
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    Set<ResourceLocation> prospectable = BlockTagResolver.resolve(
      resourceManager,
      registries,
      TFCTags.Blocks.PROSPECTABLE
    );
    Set<ResourceLocation> metalOres = ItemTagResolver.resolve(
      resourceManager,
      registries,
      TFCTags.Items.METAL_ORES
    );
    Set<ResourceLocation> smallOrePieces = ItemTagResolver.resolve(
      resourceManager,
      registries,
      TFCTags.Items.SMALL_ORE_PIECES
    );
    Set<ResourceLocation> rawMaterials = ItemTagResolver.resolve(
      resourceManager,
      registries,
      RAW_MATERIALS
    );
    var items = registries.lookupOrThrow(Registries.ITEM);
    var blocks = registries.lookupOrThrow(Registries.BLOCK);
    Map<ResourceLocation, Set<ResourceLocation>> lootCache = new HashMap<>();
    Set<ResourceLocation> drops = new LinkedHashSet<>();

    for (ResourceLocation blockId : prospectable) {
      ResourceLocation lootTableId = lootTableId(blocks, blockId);
      for (ResourceLocation itemId : lootItems(
        resourceManager,
        lootTableId,
        lootCache
      )) {
        if (!AddonNamespaces.isDiscoverable(itemId.getNamespace())) {
          continue;
        }
        if (
          metalOres.contains(itemId) ||
          smallOrePieces.contains(itemId) ||
          rawMaterials.contains(itemId)
        ) {
          continue;
        }
        Optional<Item> item = items
          .get(ResourceKey.create(Registries.ITEM, itemId))
          .map(holder -> holder.value());
        if (item.isEmpty() || item.get() instanceof BlockItem) {
          continue;
        }
        drops.add(itemId);
      }
    }
    return drops;
  }

  private static ResourceLocation lootTableId(
    HolderLookup.RegistryLookup<Block> blocks,
    ResourceLocation blockId
  ) {
    return blocks
      .get(ResourceKey.create(Registries.BLOCK, blockId))
      .map(holder -> holder.value().getLootTable())
      .filter(key -> key != null && !BuiltInLootTables.EMPTY.equals(key))
      .map(ResourceKey::location)
      .orElseGet(() ->
        ResourceLocation.fromNamespaceAndPath(
          blockId.getNamespace(),
          "blocks/" + blockId.getPath()
        )
      );
  }

  private static Set<ResourceLocation> lootItems(
    ResourceManager resourceManager,
    ResourceLocation lootTableId,
    Map<ResourceLocation, Set<ResourceLocation>> cache
  ) {
    Set<ResourceLocation> cached = cache.get(lootTableId);
    if (cached != null) {
      return cached;
    }

    Set<ResourceLocation> visiting = new LinkedHashSet<>();
    Set<ResourceLocation> items = readLootTable(
      resourceManager,
      lootTableId,
      cache,
      visiting
    );
    cache.put(lootTableId, items);
    return items;
  }

  private static Set<ResourceLocation> readLootTable(
    ResourceManager resourceManager,
    ResourceLocation lootTableId,
    Map<ResourceLocation, Set<ResourceLocation>> cache,
    Set<ResourceLocation> visiting
  ) {
    Set<ResourceLocation> cached = cache.get(lootTableId);
    if (cached != null) {
      return cached;
    }
    if (!visiting.add(lootTableId)) {
      return Set.of();
    }

    Optional<Resource> resource = resourceManager.getResource(
      ResourceLocation.fromNamespaceAndPath(
        lootTableId.getNamespace(),
        "loot_table/" + lootTableId.getPath() + ".json"
      )
    );
    if (resource.isEmpty()) {
      visiting.remove(lootTableId);
      cache.put(lootTableId, Set.of());
      return Set.of();
    }

    Set<ResourceLocation> items = new LinkedHashSet<>();
    try (Reader reader = resource.get().openAsReader()) {
      collectItemIds(
        JsonParser.parseReader(reader),
        items,
        resourceManager,
        cache,
        visiting
      );
    } catch (Exception e) {
      TFCCollectionAdvancements.LOGGER.warn(
        "Failed to read loot table {}",
        lootTableId,
        e
      );
    }
    visiting.remove(lootTableId);
    Set<ResourceLocation> frozen = Set.copyOf(items);
    cache.put(lootTableId, frozen);
    return frozen;
  }

  private static void collectItemIds(
    JsonElement element,
    Set<ResourceLocation> items,
    ResourceManager resourceManager,
    Map<ResourceLocation, Set<ResourceLocation>> cache,
    Set<ResourceLocation> visiting
  ) {
    if (element == null || element.isJsonNull()) {
      return;
    }
    if (element.isJsonArray()) {
      JsonArray array = element.getAsJsonArray();
      for (JsonElement child : array) {
        collectItemIds(child, items, resourceManager, cache, visiting);
      }
      return;
    }
    if (!element.isJsonObject()) {
      return;
    }

    JsonObject object = element.getAsJsonObject();
    String type = primitiveString(object, "type");
    if (isLootType(type, "item")) {
      ResourceLocation itemId = parseId(object);
      if (itemId != null) {
        items.add(itemId);
      }
      return;
    }
    if (isLootType(type, "loot_table")) {
      ResourceLocation nestedId = parseId(object);
      if (nestedId != null) {
        items.addAll(readLootTable(resourceManager, nestedId, cache, visiting));
      }
      return;
    }

    if (object.has("pools")) {
      collectItemIds(
        object.get("pools"),
        items,
        resourceManager,
        cache,
        visiting
      );
    }
    if (object.has("entries")) {
      collectItemIds(
        object.get("entries"),
        items,
        resourceManager,
        cache,
        visiting
      );
    }
    if (object.has("children")) {
      collectItemIds(
        object.get("children"),
        items,
        resourceManager,
        cache,
        visiting
      );
    }
  }

  private static boolean isLootType(String type, String expectedPath) {
    if (type == null) {
      return false;
    }
    int colon = type.indexOf(':');
    String path = colon >= 0 ? type.substring(colon + 1) : type;
    return expectedPath.equals(path);
  }

  private static ResourceLocation parseId(JsonObject object) {
    String value = primitiveString(object, "name");
    if (value == null) {
      value = primitiveString(object, "id");
    }
    if (value == null) {
      value = primitiveString(object, "value");
    }
    return value == null ? null : ResourceLocation.tryParse(value);
  }

  private static String primitiveString(JsonObject object, String key) {
    if (!object.has(key)) {
      return null;
    }
    JsonElement value = object.get(key);
    return value.isJsonPrimitive() ? value.getAsString() : null;
  }
}
