package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
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
        addItems();
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

    private void addItems() {
        add("item.avaritia.blaze_axe", "炽灭之焚林斧");
        add("item.avaritia.blaze_bow", "炽阳之辉耀弓");
        add("item.avaritia.blaze_cube", "炽骨立方");
        add("item.avaritia.blaze_hoe", "炽魂之耘灵锄");
        add("item.avaritia.blaze_pickaxe", "炽岩之熔山镐");
        add("item.avaritia.blaze_shovel", "炽咒之狱火锹");
        add("item.avaritia.blaze_sword", "炽焰之啄颅剑");
        add("item.avaritia.cosmic_meatballs", "寰宇肉丸");
        add("item.avaritia.crystal_axe", "晶能裂岩之斧");
        add("item.avaritia.crystal_bow", "耀晶掣空之弓");
        add("item.avaritia.crystal_hoe", "亘古不毁之锄");
        add("item.avaritia.crystal_matrix_ingot", "水晶矩阵锭");
        add("item.avaritia.crystal_pickaxe", "魔能双生之镐");
        add("item.avaritia.crystal_shovel", "晶域疾驰之铲");
        add("item.avaritia.crystal_sword", "双锋裂界之剑");
        add("item.avaritia.diamond_lattice", "钻石晶格");
        add("item.avaritia.endest_pearl", "终望珍珠");
        add("item.avaritia.enhancement_core", "无瑕核心");
        add("item.avaritia.eternal_singularity", "永恒奇点");
        add("item.avaritia.forge_energy", "锻造能量");
        add("item.avaritia.full_matter_cluster", "满载物质团");
        add("item.avaritia.infinity_axe", "自然荒芜之斧");
        add("item.avaritia.infinity_boots", "无尽靴子");
        add("item.avaritia.infinity_bow", "天堂陨落长弓");
        add("item.avaritia.infinity_bucket", "远海鲸吞之桶");
        add("item.avaritia.infinity_catalyst", "无尽催化剂");
        add("item.avaritia.infinity_chestplate", "无尽胸甲");
        add("item.avaritia.infinity_clock", "时序僭越之钟");
        add("item.avaritia.infinity_crossbow", "地狱升华之弩");
        add("item.avaritia.infinity_elytra", "无尽鞘翅");
        add("item.avaritia.infinity_helmet", "无尽头盔");
        add("item.avaritia.infinity_hoe", "地蕴复生之锄");
        add("item.avaritia.infinity_ingot", "无尽之锭");
        add("item.avaritia.infinity_mace", "山崩地裂之锤");
        add("item.avaritia.infinity_nugget", "无尽之泪");
        add("item.avaritia.infinity_pants", "无尽护腿");
        add("item.avaritia.infinity_pickaxe", "世界崩解之镐");
        add("item.avaritia.infinity_ring", "穹宇洞虚之戒（WIP）");
        add("item.avaritia.infinity_shield", "地核磐石之盾");
        add("item.avaritia.infinity_shovel", "星球吞噬之铲");
        add("item.avaritia.infinity_sword", "寰宇支配之剑");
        add("item.avaritia.infinity_totem", "无尽图腾");
        add("item.avaritia.infinity_trident", "海渊裂空之戟");
        add("item.avaritia.infinity_umbrella", "天律统御之伞");
        add("item.avaritia.infinity_upgrade", "无尽升级组件");
        add("item.avaritia.matter_cluster", "物质团");
        add("item.avaritia.neutron_gear", "中子齿轮");
        add("item.avaritia.neutron_horse_armor", "中子战马铠");
        add("item.avaritia.neutron_ingot", "中子锭");
        add("item.avaritia.neutron_nugget", "中子素颗粒");
        add("item.avaritia.neutron_pile", "中子素尘埃");
        add("item.avaritia.neutron_ring", "纳须弥之戒");
        add("item.avaritia.record_fragment", "唱片碎片");
        add("item.avaritia.refined_coal", "精炼煤炭");
        add("item.avaritia.side_config_card", "侧面配置卡");
        add("item.avaritia.singularity", "奇点－%s");
        add("item.avaritia.star_fuel", "恒星燃料");
        add("item.avaritia.ultimate_stew", "超级煲");
        add("item.avaritia.upgrade_smithing_template", "羽化模板");
    }
}
