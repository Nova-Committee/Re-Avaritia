package com.avaritia.init.data.provider;

import com.avaritia.Const;
import com.avaritia.init.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class AvaritiaZhCnLanguageProvider extends LanguageProvider {

    public AvaritiaZhCnLanguageProvider(PackOutput output) {
        super(output, Const.MOD_ID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        add(ModBlocks.compressed_crafting_table.get(), "压缩工作台");
        add(ModBlocks.double_compressed_crafting_table.get(), "二重压缩工作台");
        add(ModBlocks.neutron.get(), "中子态素块");
        add(ModBlocks.infinity.get(), "无尽之块");
        add(ModBlocks.crystal_matrix.get(), "水晶矩阵");
        add(ModBlocks.blaze_cube_block.get(), "炽骨立方块");
        add(ModBlocks.compressed_chest.get(), "压缩箱子");
        add(ModBlocks.infinity_chest.get(), "无尽之箱");
        add(ModBlocks.soul_farmland.get(), "灵魂耕地");
        add(ModBlocks.diamond_lattice_block.get(), "钻石晶格块");
        add(ModBlocks.star_fuel_block.get(), "恒星燃料块");
        add(ModBlocks.refined_coal_block.get(), "精炼煤炭块");
        add(ModBlocks.sculk_crafting_table.get(), "幽匿工作台");
        add(ModBlocks.nether_crafting_table.get(), "炼狱工作台");
        add(ModBlocks.end_crafting_table.get(), "终末工作台");
        add(ModBlocks.extreme_crafting_table.get(), "终焉工作台");
        add(ModBlocks.neutron_collector.get(), "基础中子态素收集器");
        add(ModBlocks.dense_neutron_collector.get(), "致密中子态素收集器");
        add(ModBlocks.denser_neutron_collector.get(), "精英中子态素收集器");
        add(ModBlocks.densest_neutron_collector.get(), "极限中子态素收集器");
        add(ModBlocks.neutron_compressor.get(), "基础中子态素压缩机");
        add(ModBlocks.dense_neutron_compressor.get(), "致密中子态素压缩机");
        add(ModBlocks.denser_neutron_compressor.get(), "精英中子态素压缩机");
        add(ModBlocks.densest_neutron_compressor.get(), "极限中子态素压缩机");
        add(ModBlocks.extreme_smithing_table.get(), "终焉锻造台");
        add(ModBlocks.extreme_anvil.get(), "终焉之砧");
        add(ModBlocks.endless_cake.get(), "贪婪蛋糕");
        add(ModBlocks.fake_bedrock.get(), "伪基岩");
        add(ModBlocks.fake_end_portal_frame.get(), "伪末地传送门框架");
        add(ModBlocks.fake_end_portal.get(), "伪末地传送门");

        add("container.sculk_crafting_table", "幽匿工作台");
        add("container.nether_crafting_table", "炼狱工作台");
        add("container.end_crafting_table", "终末工作台");
        add("container.extreme_crafting_table", "终焉工作台");
        add("container.extreme_smithing", "终焉锻造台");
        add("container.infinity_chest", "§6数量: %s / %s");
        add("item.avaritia.infinity_clock", "无尽时钟");
        add("item.avaritia.neutron_ring", "中子态素戒指");
        add("screen.avaritia.side_config.title", "输入输出配置");
        add("title.avaritia.config.title", "Re:Avaritia");
        add("title.avaritia.item_filter", "无尽工具过滤器");
        add("title.avaritia.item_select", "选择物品");

        add("attribute.name.generic.flying_speed", "飞行速度");
        add("attribute.name.generic.movement_speed", "移动速度");
        add("attribute.name.generic.walking_speed", "行走速度");
        add("tooltip.armor.desc", "护甲");
        add("tooltip.armor_toughness.desc", "盔甲韧性");
        add("tooltip.infinity", "无尽");
        add("tooltip.infinity.desc", "攻击伤害");
    }
}
