package com.avaritia.init.data.provider;

import com.avaritia.Const;
import com.avaritia.client.render.item.InfinityChestItemRender;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModItems;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AvaritiaBlockStateProvider implements DataProvider {
    private static final Map<String, Identifier> CUBE_ALL_TEXTURES = Map.ofEntries(
            Map.entry("neutron", mod("block/resource/neutron")),
            Map.entry("infinity", mod("block/resource/infinity")),
            Map.entry("crystal_matrix", mod("block/resource/crystal_matrix")),
            Map.entry("blaze_cube_block", mod("block/resource/blaze_cube_block")),
            Map.entry("diamond_lattice_block", mod("block/resource/diamond_lattice_block")),
            Map.entry("star_fuel_block", mod("block/resource/star_fuel_block")),
            Map.entry("refined_coal_block", mod("block/resource/refined_coal_block")),
            Map.entry("compressed_crafting_table", mod("block/machine/craft/compressed")),
            Map.entry("double_compressed_crafting_table", mod("block/machine/craft/double_compressed")),
            Map.entry("soul_farmland", mod("block/resource/soul_farmland")),
            Map.entry("fake_bedrock", vanilla("block/bedrock")),
            Map.entry("fake_end_portal", vanilla("block/black_concrete"))
    );
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider modelPathProvider;
    private final PackOutput.PathProvider itemInfoPathProvider;
    private final Map<Identifier, BlockStateModelDispatcher> generatedBlockStates = new LinkedHashMap<>();
    private final Map<Identifier, ModelInstance> generatedModels = new LinkedHashMap<>();
    private final Map<Identifier, ClientItem> generatedClientItems = new LinkedHashMap<>();

    public AvaritiaBlockStateProvider(PackOutput output) {
        this.blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        this.itemInfoPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        registerStatesAndModels();
        return CompletableFuture.allOf(
                DataProvider.saveAll(output, BlockStateModelDispatcher.CODEC, this.blockStatePathProvider, this.generatedBlockStates),
                DataProvider.saveAll(output, ModelInstance::get, this.modelPathProvider::json, this.generatedModels),
                DataProvider.saveAll(output, ClientItem.CODEC, this.itemInfoPathProvider, this.generatedClientItems)
        );
    }

    @Override
    public String getName() {
        return "Avaritia BlockStates";
    }

    protected void registerStatesAndModels() {
        simpleBlockWithItem(ModBlocks.neutron.get());
        simpleBlockWithItem(ModBlocks.infinity.get());
        simpleBlockWithItem(ModBlocks.crystal_matrix.get());
        simpleBlockWithItem(ModBlocks.blaze_cube_block.get());
        simpleBlockWithItem(ModBlocks.diamond_lattice_block.get());
        simpleBlockWithItem(ModBlocks.star_fuel_block.get());
        simpleBlockWithItem(ModBlocks.refined_coal_block.get());

        simpleBlock(ModBlocks.fake_bedrock.get());
        simpleBlock(ModBlocks.fake_end_portal_frame.get());
        simpleBlock(ModBlocks.fake_end_portal.get());

        simpleBlockWithItem(ModBlocks.compressed_crafting_table.get());
        simpleBlockWithItem(ModBlocks.double_compressed_crafting_table.get());
        simpleBlockWithItem(ModBlocks.sculk_crafting_table.get());
        simpleBlockWithItem(ModBlocks.nether_crafting_table.get());
        simpleBlockWithItem(ModBlocks.end_crafting_table.get());
        simpleBlockWithItem(ModBlocks.extreme_crafting_table.get());

        horizontalBlockWithItem(ModBlocks.neutron_collector.get());
        horizontalBlockWithItem(ModBlocks.dense_neutron_collector.get());
        horizontalBlockWithItem(ModBlocks.denser_neutron_collector.get());
        horizontalBlockWithItem(ModBlocks.densest_neutron_collector.get());

        horizontalBlockWithItem(ModBlocks.neutron_compressor.get());
        horizontalBlockWithItem(ModBlocks.dense_neutron_compressor.get());
        horizontalBlockWithItem(ModBlocks.denser_neutron_compressor.get());
        horizontalBlockWithItem(ModBlocks.densest_neutron_compressor.get());

        simpleBlockWithItem(ModBlocks.extreme_smithing_table.get());
        horizontalBlockWithItem(ModBlocks.extreme_anvil.get(), Quadrant.R180, Quadrant.R270, Quadrant.R0, Quadrant.R90);

        simpleBlockWithItem(ModBlocks.compressed_chest.get());
        horizontalBlockWithItem(ModBlocks.infinity_chest.get());

        simpleBlockWithItem(ModBlocks.soul_farmland.get());
        simpleBlockWithItem(ModBlocks.endless_cake.get());
    }

    private void simpleBlockWithItem(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        Identifier model = blockModel(id);
        this.generatedBlockStates.put(id, MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(new Variant(model)))).create());
        blockItem(block, model);
    }

    private void simpleBlock(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        Identifier model = blockModel(id);
        this.generatedBlockStates.put(id, MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(new Variant(model)))).create());
    }

    private void horizontalBlockWithItem(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        Identifier model = blockModel(id);
        this.generatedBlockStates.put(id, MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(new Variant(model))))
                .with(PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                        .select(Direction.NORTH, VariantMutator.Y_ROT.withValue(Quadrant.R0))
                        .select(Direction.EAST, VariantMutator.Y_ROT.withValue(Quadrant.R90))
                        .select(Direction.SOUTH, VariantMutator.Y_ROT.withValue(Quadrant.R180))
                        .select(Direction.WEST, VariantMutator.Y_ROT.withValue(Quadrant.R270))
                )
                .create());
        blockItem(block, model);
    }

    private void horizontalBlockWithItem(Block block, Quadrant north, Quadrant east, Quadrant south, Quadrant west) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        Identifier model = blockModel(id);
        this.generatedBlockStates.put(id, MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(new Variant(model))))
                .with(PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                        .select(Direction.NORTH, VariantMutator.Y_ROT.withValue(north))
                        .select(Direction.EAST, VariantMutator.Y_ROT.withValue(east))
                        .select(Direction.SOUTH, VariantMutator.Y_ROT.withValue(south))
                        .select(Direction.WEST, VariantMutator.Y_ROT.withValue(west))
                )
                .create());
        blockItem(block, model);
    }

    private Identifier blockModel(Identifier id) {
        String path = id.getPath();
        if (path.endsWith("_neutron_collector") || path.equals("neutron_collector")) {
            return collectorModel(id);
        }
        if (path.endsWith("_neutron_compressor") || path.equals("neutron_compressor")) {
            return compressorModel(id);
        }
        return switch (path) {
            case "sculk_crafting_table" -> bottomTopModel(id, mod("block/machine/craft/sculk_crafting_table_bottom"), mod("block/machine/craft/sculk_crafting_table_top"), mod("block/machine/craft/sculk_crafting_table_side"));
            case "nether_crafting_table" -> bottomTopModel(id, mod("block/machine/craft/nether_crafting_table_bottom"), mod("block/machine/craft/nether_crafting_table_top"), mod("block/machine/craft/nether_crafting_table_side"));
            case "end_crafting_table" -> bottomTopModel(id, mod("block/machine/craft/end_crafting_table_bottom"), mod("block/machine/craft/end_crafting_table_top"), mod("block/machine/craft/end_crafting_table_side"));
            case "extreme_crafting_table" -> topModel(id, mod("block/machine/craft/extreme_top"), mod("block/machine/craft/extreme_side"));
            case "extreme_smithing_table" -> smithingModel(id);
            case "extreme_anvil" -> extremeAnvilModel();
            case "compressed_chest" -> compressedChestModel();
            case "infinity_chest" -> infinityChestModel();
            case "fake_end_portal_frame" -> topModel(id, vanilla("block/end_portal_frame_top"), vanilla("block/end_portal_frame_side"));
            case "endless_cake" -> endlessCakeModel();
            default -> cubeAllModel(id, CUBE_ALL_TEXTURES.getOrDefault(path, mod("block/" + path)));
        };
    }

    private Identifier collectorModel(Identifier id) {
        String prefix = switch (id.getPath()) {
            case "dense_neutron_collector" -> "dense";
            case "denser_neutron_collector" -> "double";
            case "densest_neutron_collector" -> "triple";
            default -> "collector";
        };
        String textureBase = prefix.equals("collector") ? "collector" : prefix + "_collector";
        Identifier frame = mod("block/machine/collector/frame");
        Identifier top = mod("block/machine/collector/" + textureBase + "_top");
        return cubeModel(id,
                top,
                frame,
                mod("block/machine/collector/" + textureBase + "_front"),
                mod("block/machine/collector/" + textureBase + "_side_left"),
                mod("block/machine/collector/" + textureBase + "_side_right"),
                top,
                frame);
    }

    private Identifier compressorModel(Identifier id) {
        String prefix = switch (id.getPath()) {
            case "dense_neutron_compressor" -> "dense";
            case "denser_neutron_compressor" -> "double";
            case "densest_neutron_compressor" -> "triple";
            default -> "compressor";
        };
        Identifier frame = mod("block/machine/compressor/frame");
        if (prefix.equals("compressor")) {
            Identifier top = mod("block/machine/compressor/compressor_top");
            return cubeModel(id,
                    top,
                    frame,
                    mod("block/machine/compressor/compressor_front"),
                    mod("block/machine/compressor/compressor_side_left"),
                    mod("block/machine/compressor/compressor_side_right"),
                    top,
                    frame);
        }
        Identifier front = mod("block/machine/compressor/" + prefix + "_compressor_front");
        return cubeModel(id,
                front,
                front,
                frame,
                mod("block/machine/compressor/" + prefix + "_compressor_side_right"),
                mod("block/machine/compressor/" + prefix + "_compressor_side_left"),
                mod("block/machine/compressor/compressor_top_2"),
                frame);
    }

    private Identifier smithingModel(Identifier id) {
        return cubeModel(id,
                mod("block/machine/smithing/extreme_smithing_table_front"),
                mod("block/machine/smithing/extreme_smithing_table_side"),
                mod("block/machine/smithing/extreme_smithing_table_side"),
                mod("block/machine/smithing/extreme_smithing_table_front"),
                mod("block/machine/smithing/extreme_smithing_table_front"),
                mod("block/machine/smithing/extreme_smithing_table_top"),
                mod("block/machine/smithing/extreme_smithing_table_bottom"));
    }

    private Identifier compressedChestModel() {
        return jsonModel(mod("block/chest/compressed_chest"), COMPRESSED_CHEST_MODEL);
    }

    private Identifier infinityChestModel() {
        return jsonModel(mod("block/chest/infinity_chest"), INFINITY_CHEST_MODEL);
    }

    private Identifier extremeAnvilModel() {
        return jsonModel(mod("block/machine/extreme_anvil"), EXTREME_ANVIL_MODEL);
    }

    private Identifier endlessCakeModel() {
        return jsonModel(mod("block/resource/endless_cake"), ENDLESS_CAKE_MODEL);
    }

    private Identifier cubeAllModel(Identifier id, Identifier texture) {
        Identifier modelId = id.withPrefix("block/");
        ModelTemplates.CUBE_ALL.create(modelId, new TextureMapping().put(TextureSlot.ALL, texture(texture)), this.generatedModels::put);
        return modelId;
    }

    private Identifier topModel(Identifier id, Identifier top, Identifier side) {
        Identifier modelId = id.withPrefix("block/");
        ModelTemplates.CUBE_TOP.create(modelId, new TextureMapping()
                .put(TextureSlot.TOP, texture(top))
                .put(TextureSlot.SIDE, texture(side)), this.generatedModels::put);
        return modelId;
    }

    private Identifier bottomTopModel(Identifier id, Identifier bottom, Identifier top, Identifier side) {
        Identifier modelId = id.withPrefix("block/");
        ModelTemplates.CUBE_BOTTOM_TOP.create(modelId, new TextureMapping()
                .put(TextureSlot.BOTTOM, texture(bottom))
                .put(TextureSlot.TOP, texture(top))
                .put(TextureSlot.SIDE, texture(side)), this.generatedModels::put);
        return modelId;
    }

    private Identifier cubeModel(Identifier id, Identifier particle, Identifier north, Identifier south, Identifier east, Identifier west, Identifier up, Identifier down) {
        Identifier modelId = id.withPrefix("block/");
        ModelTemplates.CUBE.create(modelId, new TextureMapping()
                .put(TextureSlot.PARTICLE, texture(particle))
                .put(TextureSlot.NORTH, texture(north))
                .put(TextureSlot.SOUTH, texture(south))
                .put(TextureSlot.EAST, texture(east))
                .put(TextureSlot.WEST, texture(west))
                .put(TextureSlot.UP, texture(up))
                .put(TextureSlot.DOWN, texture(down)), this.generatedModels::put);
        return modelId;
    }

    private void blockItem(Block block, Identifier model) {
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        if (ModItems.BLOCK_ITEMS.containsKey(path)) {
            Identifier itemKey = BuiltInRegistries.ITEM.getKey(block.asItem());
            Identifier itemModel = switch (path) {
                case "endless_cake" -> flatItemModel("endless_cake", mod("item/misc/endless_cake"));
                case "extreme_anvil" -> jsonModel(mod("item/extreme_anvil"), EXTREME_ANVIL_ITEM_MODEL);
                case "infinity_chest" -> jsonModel(mod("item/infinity_chest"), INFINITY_CHEST_ITEM_MODEL);
                default -> model;
            };
            var clientModel = path.equals("infinity_chest")
                    ? ItemModelUtils.specialModel(itemModel, new InfinityChestItemRender.Unbaked())
                    : ItemModelUtils.plainModel(itemModel);
            this.generatedClientItems.put(itemKey, new ClientItem(clientModel, ClientItem.Properties.DEFAULT));
        }
    }

    private Identifier flatItemModel(String modelPath, Identifier layer0) {
        Identifier modelId = mod("item/" + modelPath);
        ModelTemplates.FLAT_ITEM.create(modelId, new TextureMapping().put(TextureSlot.LAYER0, texture(layer0)), this.generatedModels::put);
        return modelId;
    }

    private Identifier jsonModel(Identifier modelId, String json) {
        JsonObject model = JsonParser.parseString(json).getAsJsonObject();
        this.generatedModels.put(modelId, () -> model);
        return modelId;
    }

    private static Identifier mod(String path) {
        return Identifier.fromNamespaceAndPath(Const.MOD_ID, path);
    }

    private static Identifier vanilla(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    private Material texture(Identifier id) {
        return new Material(id);
    }

    private static final String COMPRESSED_CHEST_MODEL = """
            {
              "parent": "block/block",
              "textures": {
                "texture": "avaritia:block/chest/compressed_chest",
                "particle": "avaritia:block/chest/compressed_chest_break"
              },
              "elements": [
                {
                  "from": [1, 0, 1],
                  "to": [15, 10, 15],
                  "faces": {
                    "down": {"uv": [7, 4.75, 10.5, 8.25], "texture": "#texture"},
                    "up": {"uv": [3.5, 4.75, 7, 8.25], "texture": "#texture"},
                    "north": {"uv": [3.5, 8.25, 7, 10.75], "texture": "#texture"},
                    "south": {"uv": [10.5, 8.25, 14, 10.75], "texture": "#texture"},
                    "west": {"uv": [7, 8.25, 10.5, 10.75], "texture": "#texture"},
                    "east": {"uv": [0, 8.25, 3.5, 10.75], "texture": "#texture"}
                  }
                },
                {
                  "from": [1, 9, 1],
                  "to": [15, 14, 15],
                  "faces": {
                    "up": {"uv": [7, 0, 10.5, 3.5], "texture": "#texture"},
                    "down": {"uv": [3.5, 0, 7, 3.5], "texture": "#texture"},
                    "north": {"uv": [3.5, 3.5, 7, 4.75], "texture": "#texture"},
                    "south": {"uv": [10.5, 3.5, 14, 4.75], "texture": "#texture"},
                    "west": {"uv": [7, 3.5, 10.5, 4.75], "texture": "#texture"},
                    "east": {"uv": [0, 3.5, 3.5, 4.75], "texture": "#texture"}
                  }
                },
                {
                  "from": [7, 7, 0],
                  "to": [9, 11, 1],
                  "faces": {
                    "down": {"uv": [0, 0.75, 1.25, 0.5], "texture": "#texture"},
                    "up": {"uv": [0, 0.25, 0.75, 0.5], "texture": "#texture"},
                    "north": {"uv": [0.25, 0.25, 0.75, 1.25], "texture": "#texture"},
                    "south": {"uv": [1, 0.25, 1.5, 1.25], "texture": "#texture"},
                    "west": {"uv": [0.75, 0.25, 1, 1.25], "texture": "#texture"},
                    "east": {"uv": [0, 0.25, 0.25, 1.25], "texture": "#texture"}
                  }
                }
              ]
            }
            """;

    private static final String INFINITY_CHEST_MODEL = """
            {
              "textures": {
                "particle": "avaritia:block/resource/infinity"
              }
            }
            """;

    private static final String ENDLESS_CAKE_MODEL = """
            {
              "textures": {
                "particle": "avaritia:block/cake/endless_cake_side",
                "bottom": "avaritia:block/cake/endless_cake_bottom",
                "top": "avaritia:block/cake/endless_cake_top",
                "side": "avaritia:block/cake/endless_cake_side"
              },
              "elements": [
                {
                  "from": [1, 0, 1],
                  "to": [15, 8, 15],
                  "faces": {
                    "down": {"texture": "#bottom", "cullface": "down"},
                    "up": {"texture": "#top"},
                    "north": {"texture": "#side"},
                    "south": {"texture": "#side"},
                    "west": {"texture": "#side"},
                    "east": {"texture": "#side"}
                  }
                }
              ]
            }
            """;

    private static final String EXTREME_ANVIL_MODEL = """
            {
              "credit": "Made with Blockbench",
              "texture_size": [64, 64],
              "textures": {
                "1": "avaritia:block/machine/extreme_anvil",
                "particle": "avaritia:block/resource/neutron"
              },
              "elements": [
                {
                  "from": [2, 0, 2],
                  "to": [14, 4, 14],
                  "faces": {
                    "north": {"uv": [3, 7, 6, 8], "texture": "#1"},
                    "east": {"uv": [0, 7, 3, 8], "texture": "#1"},
                    "south": {"uv": [9, 7, 12, 8], "texture": "#1"},
                    "west": {"uv": [6, 7, 9, 8], "texture": "#1"},
                    "up": {"uv": [6, 7, 3, 4], "texture": "#1"},
                    "down": {"uv": [9, 4, 6, 7], "texture": "#1"}
                  }
                },
                {
                  "from": [3, 4, 4],
                  "to": [13, 5, 12],
                  "faces": {
                    "north": {"uv": [2, 10, 4.5, 10.25], "texture": "#1"},
                    "east": {"uv": [0, 10, 2, 10.25], "texture": "#1"},
                    "south": {"uv": [6.5, 10, 9, 10.25], "texture": "#1"},
                    "west": {"uv": [4.5, 10, 6.5, 10.25], "texture": "#1"},
                    "up": {"uv": [4.5, 10, 2, 8], "texture": "#1"},
                    "down": {"uv": [7, 8, 4.5, 10], "texture": "#1"}
                  }
                },
                {
                  "from": [6, 5, 6],
                  "to": [10, 10, 10],
                  "faces": {
                    "north": {"uv": [5, 11.25, 6, 12.5], "texture": "#1"},
                    "east": {"uv": [4, 11.25, 5, 12.5], "texture": "#1"},
                    "south": {"uv": [7, 11.25, 8, 12.5], "texture": "#1"},
                    "west": {"uv": [6, 11.25, 7, 12.5], "texture": "#1"},
                    "up": {"uv": [6, 11.25, 5, 10.25], "texture": "#1"},
                    "down": {"uv": [7, 10.25, 6, 11.25], "texture": "#1"}
                  }
                },
                {
                  "from": [4, 5, 5],
                  "to": [6, 10, 11],
                  "faces": {
                    "north": {"uv": [10.5, 9.5, 11, 10.75], "texture": "#1"},
                    "east": {"uv": [9, 9.5, 10.5, 10.75], "texture": "#1"},
                    "south": {"uv": [12.5, 9.5, 13, 10.75], "texture": "#1"},
                    "west": {"uv": [11, 9.5, 12.5, 10.75], "texture": "#1"},
                    "up": {"uv": [11, 9.5, 10.5, 8], "texture": "#1"},
                    "down": {"uv": [11.5, 8, 11, 9.5], "texture": "#1"}
                  }
                },
                {
                  "from": [10, 5, 5],
                  "to": [12, 10, 11],
                  "faces": {
                    "north": {"uv": [1.5, 11.75, 2, 13], "texture": "#1"},
                    "east": {"uv": [0, 11.75, 1.5, 13], "texture": "#1"},
                    "south": {"uv": [3.5, 11.75, 4, 13], "texture": "#1"},
                    "west": {"uv": [2, 11.75, 3.5, 13], "texture": "#1"},
                    "up": {"uv": [2, 11.75, 1.5, 10.25], "texture": "#1"},
                    "down": {"uv": [2.5, 10.25, 2, 11.75], "texture": "#1"}
                  }
                },
                {
                  "from": [0, 10, 3],
                  "to": [16, 16, 13],
                  "faces": {
                    "north": {"uv": [2.5, 2.5, 6.5, 4], "texture": "#1"},
                    "east": {"uv": [0, 2.5, 2.5, 4], "texture": "#1"},
                    "south": {"uv": [9, 2.5, 13, 4], "texture": "#1"},
                    "west": {"uv": [6.5, 2.5, 9, 4], "texture": "#1"},
                    "up": {"uv": [6.5, 2.5, 2.5, 0], "texture": "#1"},
                    "down": {"uv": [10.5, 0, 6.5, 2.5], "texture": "#1"}
                  }
                }
              ]
            }
            """;

    private static final String EXTREME_ANVIL_ITEM_MODEL = """
            {
              "parent": "avaritia:block/machine/extreme_anvil",
              "display": {
                "thirdperson_righthand": {"rotation": [75, -45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
                "thirdperson_lefthand": {"rotation": [75, -45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
                "firstperson_righthand": {"rotation": [0, -45, 0], "scale": [0.4, 0.4, 0.4]},
                "firstperson_lefthand": {"rotation": [0, -45, 0], "scale": [0.4, 0.4, 0.4]},
                "ground": {"translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
                "gui": {"rotation": [30, -45, 0], "scale": [0.625, 0.625, 0.625]},
                "fixed": {"scale": [0.5, 0.5, 0.5]}
              }
            }
            """;

    private static final String INFINITY_CHEST_ITEM_MODEL = """
            {
              "parent": "minecraft:builtin/entity",
              "textures": {
                "particle": "avaritia:block/resource/infinity"
              },
              "display": {
                "gui": {"rotation": [30, 45, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
                "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
                "head": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
                "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
                "thirdperson_righthand": {"rotation": [75, 315, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
                "firstperson_righthand": {"rotation": [0, 315, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]}
              }
            }
            """;
}
