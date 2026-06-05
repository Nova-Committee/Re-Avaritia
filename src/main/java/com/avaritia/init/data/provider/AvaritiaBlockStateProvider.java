package com.avaritia.init.data.provider;

import com.avaritia.Const;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModItems;
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
            Map.entry("extreme_anvil", mod("block/machine/extreme_anvil")),
            Map.entry("compressed_chest", mod("block/chest/compressed_chest")),
            Map.entry("infinity_chest", mod("block/chest/infinity_chest")),
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
        horizontalBlockWithItem(ModBlocks.extreme_anvil.get());

        simpleBlockWithItem(ModBlocks.compressed_chest.get());
        simpleBlockWithItem(ModBlocks.infinity_chest.get());

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
            case "fake_end_portal_frame" -> topModel(id, vanilla("block/end_portal_frame_top"), vanilla("block/end_portal_frame_side"));
            case "endless_cake" -> bottomTopModel(id, mod("block/cake/endless_cake_bottom"), mod("block/cake/endless_cake_top"), mod("block/cake/endless_cake_side"));
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
            this.generatedClientItems.put(itemKey, new ClientItem(ItemModelUtils.plainModel(model), ClientItem.Properties.DEFAULT));
        }
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
}
