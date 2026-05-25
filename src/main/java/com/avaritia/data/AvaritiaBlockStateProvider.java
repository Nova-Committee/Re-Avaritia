package com.avaritia.data;

import com.avaritia.Avaritia;
import com.avaritia.init.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Avaritia 方块状态与方块模型数据生成器。
 * <p>
 * 为所有注册方块生成 blockstate JSON 和 block model JSON，
 * 输出到 {@code src/generated/resources/assets/avaritia/blockstates/} 和
 * {@code models/block/}。支持简单方块（cube_all）、水平朝向方块和特殊方块。
 */
public class AvaritiaBlockStateProvider extends BlockStateProvider {

    /**
     * 使用完整参数构造（推荐）。
     * <p>
     * 在 {@link AvaritiaData#gatherData} 中通过
     * {@code event.getExistingFileHelper()} 传入。
     *
     * @param output       数据生成输出
     * @param exFileHelper 已有文件助手
     */
    public AvaritiaBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Avaritia.MOD_ID, exFileHelper);
    }

    /**
     * 使用简化参数构造（不执行纹理验证）。
     *
     * @param output 数据生成输出
     */
    public AvaritiaBlockStateProvider(PackOutput output) {
        this(output, new ExistingFileHelper(
                List.of(),
                Set.of(Avaritia.MOD_ID),
                false,
                null,
                null
        ));
    }

    @Override
    protected void registerStatesAndModels() {
        // ==================== 简单资源方块 (cube_all) ====================
        simpleBlockWithItem(ModBlocks.neutron.get(), cubeAll(ModBlocks.neutron.get()));
        simpleBlockWithItem(ModBlocks.infinity.get(), cubeAll(ModBlocks.infinity.get()));
        simpleBlockWithItem(ModBlocks.crystal_matrix.get(), cubeAll(ModBlocks.crystal_matrix.get()));
        simpleBlockWithItem(ModBlocks.blaze_cube_block.get(), cubeAll(ModBlocks.blaze_cube_block.get()));
        simpleBlockWithItem(ModBlocks.diamond_lattice_block.get(), cubeAll(ModBlocks.diamond_lattice_block.get()));
        simpleBlockWithItem(ModBlocks.star_fuel_block.get(), cubeAll(ModBlocks.star_fuel_block.get()));
        simpleBlockWithItem(ModBlocks.refined_coal_block.get(), cubeAll(ModBlocks.refined_coal_block.get()));

        // ==================== 假方块（无 BlockItem） ====================
        simpleBlock(ModBlocks.fake_bedrock.get(), cubeAll(ModBlocks.fake_bedrock.get()));
        simpleBlock(ModBlocks.fake_end_portal_frame.get(), cubeAll(ModBlocks.fake_end_portal_frame.get()));
        simpleBlock(ModBlocks.fake_end_portal.get(), cubeAll(ModBlocks.fake_end_portal.get()));

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
        simpleBlockWithItem(ModBlocks.compressed_chest.get(), cubeAll(ModBlocks.compressed_chest.get()));
        simpleBlockWithItem(ModBlocks.infinity_chest.get(), cubeAll(ModBlocks.infinity_chest.get()));

        // ==================== 特殊功能方块 ====================
        simpleBlockWithItem(ModBlocks.soul_farmland.get(), cubeAll(ModBlocks.soul_farmland.get()));
        simpleBlockWithItem(ModBlocks.endless_cake.get(), cubeAll(ModBlocks.endless_cake.get()));
    }

    /**
     * 为水平朝向方块生成 blockstate（四方向旋转变体）和物品模型。
     * <p>
     * 要求方块持有 {@code BlockStateProperties.HORIZONTAL_FACING} 属性。
     *
     * @param block 目标方块
     */
    private void horizontalBlockWithItem(Block block) {
        ModelFile model = cubeAll(block);
        horizontalBlock(block, model);
        simpleBlockItem(block, model);
    }
}
