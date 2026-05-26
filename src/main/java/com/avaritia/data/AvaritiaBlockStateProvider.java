package com.avaritia.data;

import com.avaritia.Avaritia;
import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModItems;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
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

/**
 * Avaritia 方块状态与方块模型数据生成器（26.1.2 原版 API 重写版）。
 * <p>
 * 为所有注册方块生成 blockstate JSON 和 block model JSON，
 * 输出到 {@code src/generated/resources/assets/avaritia/blockstates/} 和
 * {@code models/block/}。支持简单方块（cube_all）和水平朝向方块。
 */
public class AvaritiaBlockStateProvider implements DataProvider {
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
        // ==================== 简单资源方块 (cube_all) ====================
        simpleBlockWithItem(ModBlocks.neutron.get());
        simpleBlockWithItem(ModBlocks.infinity.get());
        simpleBlockWithItem(ModBlocks.crystal_matrix.get());
        simpleBlockWithItem(ModBlocks.blaze_cube_block.get());
        simpleBlockWithItem(ModBlocks.diamond_lattice_block.get());
        simpleBlockWithItem(ModBlocks.star_fuel_block.get());
        simpleBlockWithItem(ModBlocks.refined_coal_block.get());

        // ==================== 假方块（无 BlockItem） ====================
        simpleBlock(ModBlocks.fake_bedrock.get());
        simpleBlock(ModBlocks.fake_end_portal_frame.get());
        simpleBlock(ModBlocks.fake_end_portal.get());

        // ==================== 合成台（水平朝向） ====================
        horizontalBlockWithItem(ModBlocks.compressed_crafting_table.get());
        horizontalBlockWithItem(ModBlocks.double_compressed_crafting_table.get());
        horizontalBlockWithItem(ModBlocks.sculk_crafting_table.get());
        horizontalBlockWithItem(ModBlocks.nether_crafting_table.get());
        horizontalBlockWithItem(ModBlocks.end_crafting_table.get());
        horizontalBlockWithItem(ModBlocks.extreme_crafting_table.get());

        // ==================== 中子素收集器（水平朝向） ====================
        horizontalBlockWithItem(ModBlocks.neutron_collector.get());
        horizontalBlockWithItem(ModBlocks.dense_neutron_collector.get());
        horizontalBlockWithItem(ModBlocks.denser_neutron_collector.get());
        horizontalBlockWithItem(ModBlocks.densest_neutron_collector.get());

        // ==================== 中子素压缩机（水平朝向） ====================
        horizontalBlockWithItem(ModBlocks.neutron_compressor.get());
        horizontalBlockWithItem(ModBlocks.dense_neutron_compressor.get());
        horizontalBlockWithItem(ModBlocks.denser_neutron_compressor.get());
        horizontalBlockWithItem(ModBlocks.densest_neutron_compressor.get());

        // ==================== 特殊朝向方块 ====================
        horizontalBlockWithItem(ModBlocks.extreme_smithing_table.get());
        horizontalBlockWithItem(ModBlocks.extreme_anvil.get());

        // ==================== 箱子 ====================
        simpleBlockWithItem(ModBlocks.compressed_chest.get());
        simpleBlockWithItem(ModBlocks.infinity_chest.get());

        // ==================== 特殊功能方块 ====================
        simpleBlockWithItem(ModBlocks.soul_farmland.get());
        simpleBlockWithItem(ModBlocks.endless_cake.get());
    }

    private void simpleBlockWithItem(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        Identifier model = cubeAllModel(block, id);
        this.generatedBlockStates.put(id, MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(new Variant(model)))).create());
        blockItem(block, model);
    }

    private void simpleBlock(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        Identifier model = cubeAllModel(block, id);
        this.generatedBlockStates.put(id, MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(new Variant(model)))).create());
    }

    private void horizontalBlockWithItem(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        Identifier model = cubeAllModel(block, id);
        this.generatedBlockStates.put(id, MultiVariantGenerator.dispatch(block, new MultiVariant(WeightedList.of(new Variant(model))))
                .with(PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                        .select(Direction.NORTH, VariantMutator.Y_ROT.with(Quadrant.ZERO))
                        .select(Direction.EAST, VariantMutator.Y_ROT.with(Quadrant.P90))
                        .select(Direction.SOUTH, VariantMutator.Y_ROT.with(Quadrant.P180))
                        .select(Direction.WEST, VariantMutator.Y_ROT.with(Quadrant.P270))
                )
                .create());
        blockItem(block, model);
    }

    private Identifier cubeAllModel(Block block, Identifier id) {
        Identifier modelId = id.withPrefix("block/");
        ModelTemplates.CUBE_ALL.create(modelId, new TextureMapping().put(TextureSlot.ALL, texture(id, "block")), this.generatedModels::put);
        return modelId;
    }

    private void blockItem(Block block, Identifier model) {
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        if (ModItems.BLOCK_ITEMS.containsKey(path)) {
            Identifier itemKey = BuiltInRegistries.ITEM.getKey(block.asItem());
            this.generatedClientItems.put(itemKey, new ClientItem(ItemModel.plainModel(model), ClientItem.Properties.DEFAULT));
        }
    }

    private Material texture(Identifier id, String folder) {
        return new Material(Identifier.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath()));
    }
}
