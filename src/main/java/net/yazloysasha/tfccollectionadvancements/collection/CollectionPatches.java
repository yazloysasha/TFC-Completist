package net.yazloysasha.tfccollectionadvancements.collection;

import com.google.gson.JsonElement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.yazloysasha.tfccollectionadvancements.TFCCollectionAdvancements;
import net.yazloysasha.tfccollectionadvancements.discover.Foods;
import net.yazloysasha.tfccollectionadvancements.discover.Namespaces;

/**
 * Every collection rebuild applied when {@code ServerAdvancementManager} loads
 * JSON. TFC patches run first, then this mod's custom advancements.
 */
public final class CollectionPatches {

  @FunctionalInterface
  public interface Patch {
    void apply(
      Map<ResourceLocation, JsonElement> advancements,
      ResourceManager resourceManager,
      HolderLookup.Provider registries
    );
  }

  private static final TagKey<Biome> IS_NETHER = TagKey.create(
    Registries.BIOME,
    ResourceLocation.withDefaultNamespace("is_nether")
  );

  private static final String POTTED_PLANT_PREFIX = "plant/potted/";

  private static final List<Patch> ALL = List.of(
    // TFC: every non-Minecraft biome; vanilla nether ids when Beneath is loaded
    biomes(
      tfc("adventuring_time"),
      BiomeSource.discovered(),
      new BiomeSource("beneath", "minecraft", IS_NETHER)
    ),
    // Timber saplings ({@code wood/sapling/}); fruit saplings are Orchardist
    inventory(
      tfc("saplings"),
      "goal",
      InventorySource.discovered("wood/sapling/")
    ),
    inventory(tfc("gemologist"), InventorySource.gemPieces()),
    inventory(
      tfc("all_fish"),
      "goal",
      InventorySource.discoveredTag(Tags.Items.FOODS_RAW_FISH)
    ),
    consume(
      tfc("fruit"),
      "goal",
      InventorySource.discoveredTag(Tags.Items.FOODS_FRUIT)
    ),
    moltenIngots(tfc("metallurgist"), "challenge"),
    // Unique drops from prospectable non-metal ores (includes gems; halite → salt)
    inventory(
      tfc("minerologist"),
      "challenge",
      InventorySource.mineralOreDrops()
    ),
    // {@code seeds/} covers Firmalife grapes; TFC CropBlock covers Beneath
    inventory(
      tfc("all_crops"),
      InventorySource.discovered("seeds/"),
      InventorySource.tfcFarmlandCrops()
    ),
    sealedJar(
      world("at_grandmas"),
      InventorySource.discoveredTag(TFCTags.Items.SEALED_PRESERVES)
    ),
    placed(
      world("berry_gardener"),
      InventorySource.discovered("plant/", "_bush")
    ),
    // Path prefix: {@code c:foods/sandwiches} omits jam wheat and lists bread
    consume(
      world("breakfast_lunch_and_dinner"),
      InventorySource.discovered("food/", "_sandwich"),
      InventorySource.discoveredTag(Foods.SOUP),
      InventorySource.discoveredTag(Foods.SALAD)
    ),
    inventory(
      world("butcher"),
      InventorySource.discoveredTagExcluding(
        Tags.Items.FOODS_RAW_MEAT,
        Tags.Items.FOODS_RAW_FISH
      )
    ),
    // Turtle is tagged as cooked fish in TFC; meat advancements treat it as land
    consume(
      world("carnivore"),
      InventorySource.discoveredTagExcluding(
        Tags.Items.FOODS_COOKED_MEAT,
        Tags.Items.FOODS_COOKED_FISH
      ),
      InventorySource.discoveredExactItem(Foods.COOKED_TURTLE)
    ),
    placed(
      world("carpenter"),
      InventorySource.discoveredPrimaryBlock("wood/planks/")
    ),
    familiarized(
      world("domestication"),
      TFCTags.Entities.FARM_ANIMALS,
      TFCTags.Entities.PETS
    ),
    CollectionPatches::florist,
    anyOfLastSegment(
      world("geologist"),
      InventorySource.discovered("rock/loose/"),
      InventorySource.discovered("rock/mossy_loose/")
    ),
    placed(
      world("high_architecture"),
      InventorySource.discoveredPrimaryBlock("mud_bricks/")
    ),
    bred(
      world("husbandman"),
      TFCTags.Entities.FARM_ANIMALS,
      TFCTags.Entities.PETS
    ),
    inventory(world("jeweler"), InventorySource.discovered("gem/")),
    killed(world("king_of_beasts"), TFCTags.Entities.LAND_PREDATORS),
    placed(
      world("orchardist"),
      InventorySource.discovered("plant/", "_sapling")
    ),
    // Colored glazed / large vessels only; plain ceramic vessels are excluded
    inventory(
      world("painter"),
      InventorySource.discovered("ceramic/", "_glazed_vessel"),
      InventorySource.discoveredPrimaryBlock("ceramic/large_vessel/")
    ),
    anyOfLastSegment(
      world("pedologist"),
      InventorySource.discovered("dirt/"),
      InventorySource.discovered("grass/"),
      InventorySource.discovered("duff/"),
      InventorySource.discovered("clay/"),
      InventorySource.discovered("clay_duff/"),
      InventorySource.discovered("mud/"),
      InventorySource.discovered("coarse_dirt/")
    ),
    inventory(
      world("prospector"),
      InventorySource.discoveredTag(TFCTags.Items.SMALL_ORE_PIECES)
    ),
    inventory(world("reef_keeper"), InventorySource.saltWaterCorals()),
    inventory(world("sands_of_the_world"), InventorySource.discovered("sand/")),
    consume(
      world("sea_cook"),
      InventorySource.discoveredTagExcludingItems(
        Tags.Items.FOODS_COOKED_FISH,
        Foods.SEAFOOD_TAG_EXCLUSIONS
      )
    ),
    drink(world("sommelier"), TFCTags.Fluids.ALCOHOLS),
    placed(
      world("stonemason"),
      InventorySource.discoveredPrimaryBlock("rock/bricks/")
    ),
    inventory(world("vagabond"), InventorySource.discovered("groundcover/")),
    consume(
      world("vegetarian"),
      InventorySource.discoveredTag(Tags.Items.FOODS_FRUIT),
      InventorySource.discoveredTag(Tags.Items.FOODS_VEGETABLE)
    )
  );

  private CollectionPatches() {}

  public static void applyAll(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    for (Patch patch : ALL) {
      patch.apply(advancements, resourceManager, registries);
    }
  }

  private static ResourceLocation tfc(String path) {
    return ResourceLocation.fromNamespaceAndPath("tfc", "world/" + path);
  }

  private static ResourceLocation world(String path) {
    return ResourceLocation.fromNamespaceAndPath(
      TFCCollectionAdvancements.MOD_ID,
      "world/" + path
    );
  }

  private static Patch inventory(
    ResourceLocation id,
    InventorySource... sources
  ) {
    return inventory(id, null, sources);
  }

  private static Patch inventory(
    ResourceLocation id,
    String frame,
    InventorySource... sources
  ) {
    return (advancements, resourceManager, registries) -> {
      InventoryRebuild.patch(
        id,
        advancements,
        resourceManager,
        registries,
        List.of(sources)
      );
      applyFrame(advancements, id, frame);
    };
  }

  private static Patch consume(
    ResourceLocation id,
    InventorySource... sources
  ) {
    return consume(id, null, sources);
  }

  private static Patch consume(
    ResourceLocation id,
    String frame,
    InventorySource... sources
  ) {
    return (advancements, resourceManager, registries) -> {
      InventoryRebuild.patchConsume(
        id,
        advancements,
        resourceManager,
        registries,
        List.of(sources)
      );
      applyFrame(advancements, id, frame);
    };
  }

  private static Patch placed(ResourceLocation id, InventorySource... sources) {
    return (advancements, resourceManager, registries) ->
      InventoryRebuild.patchPlacedBlock(
        id,
        advancements,
        resourceManager,
        registries,
        List.of(sources)
      );
  }

  private static Patch sealedJar(
    ResourceLocation id,
    InventorySource... sources
  ) {
    return (advancements, resourceManager, registries) ->
      InventoryRebuild.patchSealedJar(
        id,
        advancements,
        resourceManager,
        registries,
        List.of(sources)
      );
  }

  private static Patch anyOfLastSegment(
    ResourceLocation id,
    InventorySource... sources
  ) {
    return (advancements, resourceManager, registries) ->
      InventoryRebuild.patchAnyOfByLastPathSegment(
        id,
        advancements,
        resourceManager,
        registries,
        List.of(sources)
      );
  }

  private static Patch moltenIngots(ResourceLocation id, String frame) {
    return (advancements, resourceManager, registries) -> {
      InventoryRebuild.patchIngotsFromMoltenMetals(
        id,
        advancements,
        resourceManager,
        registries
      );
      applyFrame(advancements, id, frame);
    };
  }

  private static Patch biomes(ResourceLocation id, BiomeSource... sources) {
    return (advancements, resourceManager, registries) ->
      BiomeRebuild.patch(
        id,
        advancements,
        resourceManager,
        registries,
        List.of(sources)
      );
  }

  @SafeVarargs
  private static Patch drink(ResourceLocation id, TagKey<Fluid>... tags) {
    return (advancements, resourceManager, registries) ->
      FluidRebuild.patchDrinkFromTags(
        id,
        advancements,
        resourceManager,
        registries,
        List.of(tags)
      );
  }

  @SafeVarargs
  private static Patch familiarized(
    ResourceLocation id,
    TagKey<EntityType<?>>... tags
  ) {
    return (advancements, resourceManager, registries) ->
      EntityRebuild.patchFamiliarizedAnimalFromTags(
        id,
        advancements,
        resourceManager,
        registries,
        List.of(tags)
      );
  }

  @SafeVarargs
  private static Patch bred(
    ResourceLocation id,
    TagKey<EntityType<?>>... tags
  ) {
    return (advancements, resourceManager, registries) ->
      EntityRebuild.patchBredAnimalFromTags(
        id,
        advancements,
        resourceManager,
        registries,
        List.of(tags)
      );
  }

  @SafeVarargs
  private static Patch killed(
    ResourceLocation id,
    TagKey<EntityType<?>>... tags
  ) {
    return (advancements, resourceManager, registries) ->
      EntityRebuild.patchKilledEntityFromTags(
        id,
        advancements,
        resourceManager,
        registries,
        List.of(tags)
      );
  }

  private static void applyFrame(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceLocation id,
    String frame
  ) {
    if (frame == null) {
      return;
    }
    JsonElement advancement = advancements.get(id);
    if (advancement != null && advancement.isJsonObject()) {
      CriterionJson.setFrame(advancement.getAsJsonObject(), frame);
    }
  }

  /**
   * Discoverable {@link FlowerPotBlock}s except the empty pot, timber/fruit
   * saplings (Arborist / Orchardist), and krummholz. Criterion is
   * {@code item_used_on_block}: {@code placed_block} does not fire for potting.
   */
  private static void florist(
    Map<ResourceLocation, JsonElement> advancements,
    ResourceManager resourceManager,
    HolderLookup.Provider registries
  ) {
    BlockRebuild.patchItemUsedOnBlocks(
      world("florist"),
      advancements,
      collectPottedPlants(registries),
      POTTED_PLANT_PREFIX
    );
  }

  private static Set<ResourceLocation> collectPottedPlants(
    HolderLookup.Provider registries
  ) {
    Set<ResourceLocation> potted = new LinkedHashSet<>();
    for (var holder : registries
      .lookupOrThrow(Registries.BLOCK)
      .listElements()
      .toList()) {
      if (!(holder.value() instanceof FlowerPotBlock)) {
        continue;
      }
      if (holder.value() == Blocks.FLOWER_POT) {
        continue;
      }
      ResourceLocation blockId = holder.getKey().location();
      if (!Namespaces.isDiscoverable(blockId.getNamespace())) {
        continue;
      }
      String path = blockId.getPath();
      if (path.contains("sapling") || path.contains("krummholz")) {
        continue;
      }
      potted.add(blockId);
    }
    return potted;
  }
}
