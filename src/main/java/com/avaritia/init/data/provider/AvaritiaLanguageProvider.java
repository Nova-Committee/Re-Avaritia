package com.avaritia.init.data.provider;

import com.avaritia.Const;

import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModEntityTypes;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModMobEffects;
import com.avaritia.init.registry.enums.ModLang;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Avaritia 英文本地化数据生成器。
 * <p>
 * 负责通过 datagen 生成 {@code assets/avaritia/lang/en_us.json}，避免手写语言 JSON。
 * </p>
 */
public class AvaritiaLanguageProvider extends LanguageProvider {

    /**
     * 创建语言文件提供器。
     *
     * @param output 数据生成输出目录
     * @param locale 语言区域代码，例如 {@code en_us}
     */
    public AvaritiaLanguageProvider(PackOutput output, String locale) {
        super(output, Const.MOD_ID, locale);
    }

    /**
     * 添加全部英文本地化条目。
     */
    @Override
    protected void addTranslations() {
        addItems();
        addBlocks();
        addCreativeTabs();
        addEntities();
        addEffectsAndEnchantments();
        addSingularities();
        addToolModes();
        addTooltips();
        addContainersAndScreens();
        addButtons();
        addDeathMessages();
        addRecipeViewerTexts();
        addConfigTexts();
        addAdvancements();
        addMiscTexts();
    }

    /**
     * 添加物品名称。
     */
    private void addItems() {
        add(ModItems.neutron_ring.get(), "Neutronium Ring");
        add(ModItems.infinity_totem.get(), "Infinity Totem");
        add(ModItems.infinity_ring.get(), "Infinity Ring (WIP)");
        add(ModItems.infinity_umbrella.get(), "Infinity Umbrella");
        add(ModItems.infinity_clock.get(), "Infinity Clock");
        add(ModItems.side_config_card.get(), "Side Configuration Card");

        add(ModItems.infinity_sword.get(), "Sword of the Cosmos");
        add(ModItems.infinity_hoe.get(), "Eternal Fertility");
        add(ModItems.infinity_pickaxe.get(), "World Breaker");
        add(ModItems.infinity_shovel.get(), "Planet Eater");
        add(ModItems.infinity_axe.get(), "Nature's Ruin");
        add(ModItems.infinity_bucket.get(), "Infinity Bucket");
        add(ModItems.infinity_bow.get(), "Longbow of the Heavens");
        add(ModItems.infinity_crossbow.get(), "Crossbow of the Inferno");
        add(ModItems.infinity_shield.get(), "Shield of the Earth's Core");
        add(ModItems.infinity_trident.get(), "Trident of the Sea Abyss");
        add(ModItems.infinity_mace.get(), "Infinity Mace");

        add(ModItems.crystal_sword.get(), "Crystal Sword");
        add(ModItems.crystal_hoe.get(), "Crystal Hoe");
        add(ModItems.crystal_pickaxe.get(), "Crystal Pickaxe");
        add(ModItems.crystal_shovel.get(), "Crystal Shovel");
        add(ModItems.crystal_axe.get(), "Crystal Axe");
        add(ModItems.crystal_bow.get(), "Crystal Bow");

        add(ModItems.blaze_sword.get(), "Blaze Skull Sword");
        add(ModItems.blaze_hoe.get(), "Blaze Soul Hoe");
        add(ModItems.blaze_pickaxe.get(), "Blaze Lava Pickaxe");
        add(ModItems.blaze_shovel.get(), "Blaze Fire Shovel");
        add(ModItems.blaze_axe.get(), "Blaze Bush Axe");
        add(ModItems.blaze_bow.get(), "Blaze Shining Bow");

        add(ModItems.infinity_helmet.get(), "Infinity Helmet");
        add(ModItems.infinity_chestplate.get(), "Infinity Chestplate");
        add(ModItems.infinity_pants.get(), "Infinity Pants");
        add(ModItems.infinity_boots.get(), "Infinity Boots");
        add(ModItems.neutron_horse_armor.get(), "Neutronium Horse Armor");
        add(ModItems.infinity_elytra.get(), "Infinity Elytra");

        add(ModItems.blaze_cube.get(), "Blaze Cube");
        add(ModItems.diamond_lattice.get(), "Diamond Lattice");
        add(ModItems.crystal_matrix_ingot.get(), "Crystal Matrix Ingot");
        add(ModItems.neutron_pile.get(), "Pile of Neutrons");
        add(ModItems.neutron_nugget.get(), "Neutronium Nugget");
        add(ModItems.neutron_ingot.get(), "Neutronium Ingot");
        add(ModItems.neutron_gear.get(), "Neutronium Gear");
        add(ModItems.infinity_nugget.get(), "Infinity Nugget");
        add(ModItems.infinity_catalyst.get(), "Infinity Catalyst");
        add(ModItems.infinity_ingot.get(), "Infinity Ingot");
        add(ModItems.singularity.get(), "%s Singularity");
        add(ModItems.eternal_singularity.get(), "Eternal Singularity");
        add(ModItems.record_fragment.get(), "Record Fragment");
        add(ModItems.star_fuel.get(), "Star Fuel");
        add(ModItems.refined_coal.get(), "Refined Coal");
        add(ModItems.endest_pearl.get(), "Endest Pearl");
        add(ModItems.matter_cluster.get(), "Matter Cluster");
        add(ModItems.full_matter_cluster.get(), "Full Matter Cluster");
        add(ModItems.enhancement_core.get(), "Enhancement Core");
        add(ModItems.upgrade_smithing_template.get(), "Upgrade Smithing Template");
        add(ModItems.infinity_upgrade.get(), "Infinity Upgrade");
        add(ModItems.ultimate_stew.get(), "Ultimate Stew");
        add(ModItems.cosmic_meatballs.get(), "Cosmic Meatballs");
        add(ModItems.forge_energy.get(), "Forge Energy");
    }

    /**
     * 添加方块名称。
     */
    private void addBlocks() {
        add(ModBlocks.compressed_crafting_table.get(), "Compressed Crafting Table");
        add(ModBlocks.double_compressed_crafting_table.get(), "Double Compressed Crafting Table");
        add(ModBlocks.neutron.get(), "Neutronium Block");
        add(ModBlocks.infinity.get(), "Infinity Block");
        add(ModBlocks.crystal_matrix.get(), "Crystal Matrix");
        add(ModBlocks.blaze_cube_block.get(), "Blaze Cube Block");
        add(ModBlocks.compressed_chest.get(), "Compressed Chest");
        add(ModBlocks.infinity_chest.get(), "Infinity Chest");
        add(ModBlocks.soul_farmland.get(), "Soul Farmland");
        add(ModBlocks.diamond_lattice_block.get(), "Diamond Lattice Block");
        add(ModBlocks.star_fuel_block.get(), "Star Fuel Block");
        add(ModBlocks.refined_coal_block.get(), "Refined Coal Block");
        add(ModBlocks.sculk_crafting_table.get(), "Sculk Crafting Table");
        add(ModBlocks.nether_crafting_table.get(), "Nether Crafting Table");
        add(ModBlocks.end_crafting_table.get(), "End Crafting Table");
        add(ModBlocks.extreme_crafting_table.get(), "Extreme Crafting Table");
        add(ModBlocks.neutron_collector.get(), "Neutronium Collector");
        add(ModBlocks.dense_neutron_collector.get(), "Dense Neutronium Collector");
        add(ModBlocks.denser_neutron_collector.get(), "Denser Neutronium Collector");
        add(ModBlocks.densest_neutron_collector.get(), "Densest Neutronium Collector");
        add(ModBlocks.neutron_compressor.get(), "Neutronium Compressor");
        add(ModBlocks.dense_neutron_compressor.get(), "Dense Neutronium Compressor");
        add(ModBlocks.denser_neutron_compressor.get(), "Denser Neutronium Compressor");
        add(ModBlocks.densest_neutron_compressor.get(), "Densest Neutronium Compressor");
        add(ModBlocks.extreme_smithing_table.get(), "Extreme Smithing Table");
        add(ModBlocks.extreme_anvil.get(), "Extreme Anvil");
        add(ModBlocks.endless_cake.get(), "Endless Cake");
        add(ModBlocks.fake_bedrock.get(), "Fake Bedrock");
        add(ModBlocks.fake_end_portal_frame.get(), "Fake End Portal Frame");
        add(ModBlocks.fake_end_portal.get(), "Fake End Portal");
    }

    /**
     * 添加创造模式物品栏名称。
     */
    private void addCreativeTabs() {
        add("itemGroup.tab.Infinity", "Re:Avaritia");
        add("itemGroup.tab.Singularity", "Avaritia: Singularity");
    }

    /**
     * 添加实体名称。
     */
    private void addEntities() {
        add(ModEntityTypes.IMMORTAL.get(), "Immortal Item");
        add(ModEntityTypes.ENDER_PEARL.get(), "Endest Pearl");
        add(ModEntityTypes.GAPING_VOID.get(), "Gaping Void");
        add(ModEntityTypes.HEAVEN_ARROW.get(), "Heaven Arrow");
        add(ModEntityTypes.NEUTRON_ARROW.get(), "Neutron Arrow");
        add(ModEntityTypes.HEAVEN_SUB_ARROW.get(), "Heaven Sub Arrow");
        add(ModEntityTypes.EXPLOSIONS_ARROW.get(), "Explosive Arrow");
        add(ModEntityTypes.BURNING_ARROW.get(), "Burning Arrow");
        add(ModEntityTypes.TRACE_ARROW.get(), "Trace Arrow");
        add(ModEntityTypes.FIRE_BALL.get(), "Fire Ball");
        add(ModEntityTypes.BURNING_BALL.get(), "Burning Ball");
        add(ModEntityTypes.BLADE_SLASH.get(), "Blade Slash");
        add(ModEntityTypes.SUN_PRO.get(), "Sun Projectile");
        add(ModEntityTypes.RAIN_PRO.get(), "Rain Projectile");
        add(ModEntityTypes.STORM_PRO.get(), "Storm Projectile");
        add(ModEntityTypes.ACCELERATOR_DISPLAY.get(), "Accelerator Display");
        add(ModEntityTypes.TNT_PRO.get(), "TNT Projectile");
        add(ModEntityTypes.INFINITY_THROWN_TRIDENT.get(), "Infinity Thrown Trident");
    }

    /**
     * 添加状态效果与附魔名称。
     */
    private void addEffectsAndEnchantments() {
        add(ModMobEffects.BURNING.get(), "Burning");
        add("effect.avaritia.burning.description", "Deals fire damage equal to 5% of maximum health every 20 ticks (1 second). \n\nThis debuff forces the creature to lose health and can only be alleviated in water or powder snow.\n\nThe health loss effect is similar to wither or poison.");
        add("enchantment.minecraft.frost_walker", "Frost Walker");
    }

    /**
     * 添加奇点名称。
     */
    private void addSingularities() {
        add("singularity.avaritia.obsidian", "Obsidian");
        add("singularity.avaritia.blue_ice", "Blue Ice");
        add("singularity.avaritia.coal", "Coal");
        add("singularity.avaritia.copper", "Copper");
        add("singularity.avaritia.iron", "Iron");
        add("singularity.avaritia.lapis_lazuli", "Lapis Lazuli");
        add("singularity.avaritia.redstone", "Redstone");
        add("singularity.avaritia.glowstone", "Glowstone");
        add("singularity.avaritia.gold", "Gold");
        add("singularity.avaritia.diamond", "Diamond");
        add("singularity.avaritia.emerald", "Emerald");
        add("singularity.avaritia.quartz", "Quartz");
        add("singularity.avaritia.amethyst_shard", "Amethyst Shard");
        add("singularity.avaritia.netherite", "Netherite");
        add("singularity.avaritia.aluminum", "Aluminum");
        add("singularity.avaritia.tin", "Tin");
        add("singularity.avaritia.bronze", "Bronze");
        add("singularity.avaritia.silver", "Silver");
        add("singularity.avaritia.lead", "Lead");
        add("singularity.avaritia.steel", "Steel");
        add("singularity.avaritia.nickel", "Nickel");
        add("singularity.avaritia.electrum", "Electrum");
        add("singularity.avaritia.invar", "Invar");
        add("singularity.avaritia.platinum", "Platinum");
        add("singularity.avaritia.uranium", "Uranium");
        add("singularity.avaritia.osmium", "Osmium");
        add("singularity.avaritia.refined_obsidian", "Refined Obsidian");
    }

    /**
     * 添加工具模式语言键。
     */
    private void addToolModes() {
        add(ModLang.CURRENT_MODE.getTranslationKey(), "Current Mode: %s");
        add(ModLang.DEFAULT_MODE.getTranslationKey(), "Default");
        add(ModLang.ADVANCE_MODE.getTranslationKey(), "Advanced");
        add(ModLang.RANGE_MODE.getTranslationKey(), "Range");
        add(ModLang.MODE_SWITCH.getTranslationKey(), "Switched to %s");
    }

    /**
     * 添加 Tooltip 文本。
     */
    private void addTooltips() {
        add("tooltip.avaritia.added_by", "Added By: %s");
        add("tooltip.avaritia.singularity_id", "Singularity ID: %s");
        add("tooltip.avaritia.active", "%s mode active!");
        add("tooltip.avaritia.inactive", "%s mode inactive!");
        add("tooltip.avaritia.switch", "Switch to %s");
        add("tooltip.avaritia.empty", "Empty");
        add("tooltip.avaritia.num_items", "Need %s Items");
        add("tooltip.avaritia.crafting", "%s: %s x %s");
        add("tooltip.avaritia.smithing", "Smithing: %s x %s");
        add("tooltip.avaritia.compress", "Compress: %s x %s");
        add("tooltip.avaritia.time_consume", "Time Required: %s");
        add("tooltip.avaritia.progress", "Progress: %s");
        add("tooltip.avaritia.init_enchant", "Always has at least %s");
        add("tooltip.avaritia.durability", "Durability %s");

        add("tooltip.avaritia.blaze_axe.desc", "Burn the forest, and the wood turns into charcoal.");
        add("tooltip.avaritia.blaze_cube.desc", "Blaze Storm.");
        add("tooltip.avaritia.blaze_hoe.desc", "Power of soul, all things grow.");
        add("tooltip.avaritia.blaze_pickaxe.desc", "Molten mountain hot smelted.");
        add("tooltip.avaritia.blaze_shovel.desc", "Spell born from Nether.");
        add("tooltip.avaritia.blaze_sword.desc", "Beheads skeletons and scorches them black.");
        add("tooltip.avaritia.crystal_axe.desc", "Breaks rocks, shatters shields.");
        add("tooltip.avaritia.crystal_hoe.desc", "One swing, life blooms; another, time flows.");
        add("tooltip.avaritia.crystal_matrix_ingot.desc", "This isn't even its final form.");
        add("tooltip.avaritia.crystal_pickaxe.desc", "Luck brings treasures; precision finds gems.");
        add("tooltip.avaritia.crystal_shovel.desc", "Rises like wind, falls to slow.");
        add("tooltip.avaritia.crystal_sword.desc", "Blades cut through, shadows vanquish foes.");
        add("tooltip.avaritia.diamond_lattice.desc", "Dense material...");
        add("tooltip.avaritia.endless_cake.desc", "Gluttony?");
        add("tooltip.avaritia.enhancement_core.desc", "The road to completion!");
        add("tooltip.avaritia.infinity_bucket.desc", "Devour everything...");
        add("tooltip.avaritia.infinity_bucket.message", "Current Fluid: %s | Amount: %smL");
        add("tooltip.avaritia.infinity_catalyst.desc", "One is all and all is one.");
        add("tooltip.avaritia.infinity_clock.desc", "Control of your time...");
        add("tooltip.avaritia.infinity_ingot.desc", "The fury of the universe in the palm of your hand.");
        add("tooltip.avaritia.infinity_nugget.desc", "Angel's tears...");
        add("tooltip.avaritia.infinity_ring.desc", "...");
        add("tooltip.avaritia.infinity_totem.desc", "Eternal Guardian...");
        add("tooltip.avaritia.infinity_umbrella.desc", "Instant eternity...");
        add("tooltip.avaritia.infinity_upgrade.desc", "Accelerate upgrade!");
        add("tooltip.avaritia.items_required", "Items Required:");
        add("tooltip.avaritia.limited_input", "Limited Input");
        add("tooltip.avaritia.matter_cluster.counter", "%s / %s items");
        add("tooltip.avaritia.matter_cluster.desc", "Use to deconstruct.");
        add("tooltip.avaritia.matter_cluster.desc2", "Hold SHIFT for contents.");
        add("tooltip.avaritia.mode", "Mode: %s");
        add("tooltip.avaritia.more", "and %s more...");
        add("tooltip.avaritia.neutron_gear.desc", "The secret of the tech renaissance...");
        add("tooltip.avaritia.neutron_horse_armor.desc", "Full power...");
        add("tooltip.avaritia.neutron_ingot.desc", "The dense heart of a star in convenient ingot form.");
        add("tooltip.avaritia.neutron_nugget.desc", "About 35.6 million metric tons.");
        add("tooltip.avaritia.neutron_pile.desc", "Try not to think about it.");
        add("tooltip.avaritia.record_fragment.desc", "One likes to believe in the freedom of music~");
        add("tooltip.avaritia.refined_coal.desc", "Heat explosion!");
        add("tooltip.avaritia.seconds", "%s Seconds");
        add("tooltip.avaritia.selected", "Selected");
        add("tooltip.avaritia.star_fuel.desc", "A fuel for the stars.");
        add("tooltip.avaritia.sword_kill_mode.active", "Kill all you can see.");
        add("tooltip.avaritia.ticks", "%s Ticks");
        add("tooltip.avaritia.tier", "Tier: %s");
        add("tooltip.avaritia.totem_break", "The Infinity Totem has been depleted");
        add("tooltip.avaritia.type", "Type: %s");
        add("tooltip.avaritia.unlimited_input", "Unlimited Input");

        add("tooltip.avaritia.side.click_to_cycle", "Click To Cycle");
        add("tooltip.avaritia.side.mode.active_input", "Active Input");
        add("tooltip.avaritia.side.mode.active_mixin", "Active Mixin");
        add("tooltip.avaritia.side.mode.active_output", "Active Output");
        add("tooltip.avaritia.side.mode.off", "OFF");
        add("tooltip.avaritia.side.mode.passive_input", "Passive Input");
        add("tooltip.avaritia.side.mode.passive_mixin", "Passive Mixin");
        add("tooltip.avaritia.side.mode.passive_output", "Passive Output");
        add("tooltip.avaritia.side_config_card.already_empty", "Configuration is already empty");
        add("tooltip.avaritia.side_config_card.apply_success", "Configuration applied to machine");
        add("tooltip.avaritia.side_config_card.cleared", "Configuration cleared");
        add("tooltip.avaritia.side_config_card.has_config", "§7Configuration saved");
        add("tooltip.avaritia.side_config_card.instruction_right_click", "§7Right-click on machine to apply");
        add("tooltip.avaritia.side_config_card.instruction_shift_air", "§7Shift+right-click other block to clear");
        add("tooltip.avaritia.side_config_card.instruction_shift_right_click", "§7Shift+right-click machine to save");
        add("tooltip.avaritia.side_config_card.no_config", "§7Empty configuration");
        add("tooltip.avaritia.side_config_card.no_config_to_apply", "No configuration to apply");
        add("tooltip.avaritia.side_config_card.read_success", "Configuration saved to card");

        add("tooltip.avaritia.tool.fire_ball", "Fire Ball");
        add("tooltip.avaritia.tool.blade_slash", "Blade Slash");
        add("tooltip.avaritia.tool.blaze_bow_burning", "Area Burning");
        add("tooltip.avaritia.tool.blaze_shovel_trans", "Material conversion");
        add("tooltip.avaritia.tool.crystal_pickaxe.enchant_1", "Set to Fortune III");
        add("tooltip.avaritia.tool.crystal_pickaxe.enchant_2", "Set to Silk Touch");
        add("tooltip.avaritia.tool.infinity_axe_range", "Range Break");
        add("tooltip.avaritia.tool.infinity_bow_tracer", "Trace Attack");
        add("tooltip.avaritia.tool.infinity_clock.overclock_disabled", "Set to SpeedUp Mode");
        add("tooltip.avaritia.tool.infinity_clock.overclock_enabled", "Set to TimeUp Mode");
        add("tooltip.avaritia.tool.infinity_clock_up", "Speed Up");
        add("tooltip.avaritia.tool.infinity_crossbow_multi", "MultiShoot");
        add("tooltip.avaritia.tool.infinity_hoe_sow", "Range");
        add("tooltip.avaritia.tool.infinity_pickaxe_hammer", "Range Dig");
        add("tooltip.avaritia.tool.infinity_shovel_destroyer", "Range/Endest Pearl Attack");
        add("tooltip.avaritia.tool.infinity_sword_kill", "Slaughter");
        add("tooltip.avaritia.tool.infinity_trident_loyalty", "Loyalty");
        add("tooltip.avaritia.tool.infinity_trident_normal", "Normal");
        add("tooltip.avaritia.tool.infinity_trident_riptide", "Riptide");
        add("tooltip.avaritia.tool.infinity_umbrella_normal", "Base");
        add("tooltip.avaritia.tool.infinity_umbrella_rain", "Rain");
        add("tooltip.avaritia.tool.infinity_umbrella_storm", "Storm");
        add("tooltip.avaritia.tool.infinity_umbrella_sun", "Clear");
        add("tooltip.avaritia.tool.smelt", "Smelt");

        add("tooltip.armor.desc", "Armor");
        add("tooltip.armor_toughness.desc", "Armor Toughness");
        add("tooltip.crystal_pickaxe.enchant_1", "Set to Fortune III");
        add("tooltip.crystal_pickaxe.enchant_2", "Set to Silk Touch");
        add("tooltip.infinity_pickaxe.enchant_1", "Set to Fortune X");
        add("tooltip.infinity_pickaxe.enchant_2", "Set to Silk Touch");
        add("tooltip.infinity", "Infinity");
        add("tooltip.infinity.desc", "Attack Damage");
        add("tooltip.avaritia.compressor_eject.message_1", "§c[Neutron Compressor] §fUnlock the recipe first");
        add("tooltip.avaritia.compressor_eject.message_2", "§a[Neutron Compressor] §fThe material has been ejected and packed into a matter cluster.");
        add("tooltip.avaritia.compressor_eject.message_3", "§c[Neutron Compressor] §fNo materials available to pop out");
        add("tooltip.avaritia.compressor_lock.message_1", "§a[Neutron Compressor] §fRecipe is locked");
        add("tooltip.avaritia.compressor_lock.message_2", "§e[Neutron Compressor] §fRecipe unlocked");
        add("tooltip.avaritia.compressor_lock.message_3", "§c[Neutron Compressor] §fPlease add a recipe ingredient first");
    }

    /**
     * 添加容器与屏幕文本。
     */
    private void addContainersAndScreens() {
        add("container.sculk_crafting_table", "Sculk Crafting Table");
        add("container.nether_crafting_table", "Nether Crafting Table");
        add("container.end_crafting_table", "End Crafting Table");
        add("container.extreme_crafting_table", "Extreme Crafting Table");
        add("container.extreme_smithing", "Extreme Smithing Table");
        add("container.infinity_chest", "§6Amount: %s / %s");
        add("screen.avaritia.side_config.title", "IO Config");
        add("title.avaritia.config.title", "Re:Avaritia");
        add("title.avaritia.resourcepack", "Avaritia Old Resourcepack");
    }

    /**
     * 添加按钮文本。
     */
    private void addButtons() {
        add("button.avaritia.side_config_button_1", "Configure Input and Output");
        add("button.avaritia.side_config_button_2", "Click to open the six-sided configuration interface");
        add("button.avaritia.lock_button_1", "Recipe Locked");
        add("button.avaritia.lock_button_2", "Click to unlock recipe");
        add("button.avaritia.lock_button_3", "Unlocked Recipe");
        add("button.avaritia.lock_button_4", "Can click lock when there is a recipe");
        add("button.avaritia.eject_button_1", "Pop-up material");
        add("button.avaritia.eject_button_2", "Click to pop up all materials into the inventory");
    }

    /**
     * 添加伤害死亡消息。
     */
    private void addDeathMessages() {
        add("death.attack.infinity", "%1$s was obliterated by %2$s");
        add("death.attack.infinity.0", "%1$s was sliced to ribbons");
        add("death.attack.infinity.1", "%1$s was sliced to ribbons");
        add("death.attack.infinity.2", "%1$s was excised from existence");
        add("death.attack.infinity.3", "%1$s was overkilled");
        add("death.attack.infinity.4", "%1$s was annihilated");
        add("death.attack.infinity.item", "%1$s was obliterated by %2$s");
        add("death.attack.infinity.player.0", "%1$s was sliced to ribbons by %2$s");
        add("death.attack.infinity.player.1", "%1$s was sliced to ribbons by %2$s");
        add("death.attack.infinity.player.2", "%1$s was excised from existence by %2$s");
        add("death.attack.infinity.player.3", "%1$s was overkilled by %2$s");
        add("death.attack.infinity.player.4", "%1$s was annihilated by %2$s");
    }

    /**
     * 添加配方查看器与 Jade 文本。
     */
    private void addRecipeViewerTexts() {
        add("jei.category.avaritia.compressor", "Neutronium Compressor");
        add("jei.category.avaritia.sculk_crafting_table", "Sculk Craft");
        add("jei.category.avaritia.nether_crafting_table", "Nether Craft");
        add("jei.category.avaritia.end_crafting_table", "End Craft");
        add("jei.category.avaritia.extreme_crafting_table", "Extreme Craft");
        add("jei.category.avaritia.extreme_smithing_table", "Extreme Smithing");
        add("jei.tooltip.avaritia.bedrock", "Obtain using Crystal Pickaxe or World Breaker (Infinity Pickaxe)");
        add("jei.tooltip.avaritia.crystal_pickaxe", "This tool can mine Bedrock");
        add("jei.tooltip.avaritia.end_portal_frame", "Obtain using Crystal Pickaxe or World Breaker (Infinity Pickaxe)");
        add("jei.tooltip.avaritia.full_matter_cluster", "Use an internal Matter Cluster with 4096 items to craft");
        add("jei.tooltip.avaritia.neutron_collector", "Collect Pile of Neutrons");
        add("jei.tooltip.avaritia.neutron_pile", "With a neutron collector, you can get a pile of neutrons after a while");
        add("jei.tooltip.avaritia.refined_coal", "Use Blaze Bush Axe to chop logs and obtain");
        add("jei.tooltip.shapeless.recipe", "Shapeless Recipe");
        add("config.jade.plugin_avaritia.neutron_collector", "Neutronium Collector");
        add("config.jade.plugin_avaritia.compressor", "Compressor");
        add("config.jade.plugin_avaritia.crafting_table", "Avaritia Crafting");
        add("config.jade.plugin_avaritia.extreme_smithing", "Extreme Smithing");
    }

    /**
     * 添加配置界面文本。
     */
    private void addConfigTexts() {
        add("config.avaritia.category.channel", "Channel");
        add("config.avaritia.category.emc", "EMC");
        add("config.avaritia.category.misc", "Misc");
        add("config.avaritia.category.storage", "Storage");
        add("config.avaritia.category.tools", "Tools");
        add("config.avaritia.axe_chain_count", "Axe Chain Count");
        add("config.avaritia.axe_chain_count.tooltip", "Chain number of Infinity Axe cutting trees");
        add("config.avaritia.blade_slash_damage", "Blade Slash Damage");
        add("config.avaritia.blade_slash_damage.tooltip", "Damage of Blade Slash for Crystal Sword");
        add("config.avaritia.blade_slash_radius", "Blade Slash Radius");
        add("config.avaritia.blade_slash_radius.tooltip", "Radius of Blade Slash for Crystal Sword");
        add("config.avaritia.boot_speed_backward_multiplier", "Boots Backward Multiplier");
        add("config.avaritia.boot_speed_backward_multiplier.tooltip", "Infinity Boots speed multiplier when moving backward");
        add("config.avaritia.boot_speed_base", "Boots Base Speed");
        add("config.avaritia.boot_speed_base.tooltip", "Base movement speed for Infinity Boots");
        add("config.avaritia.boot_speed_flying_multiplier", "Boots Flying Multiplier");
        add("config.avaritia.boot_speed_flying_multiplier.tooltip", "Infinity Boots speed multiplier when flying");
        add("config.avaritia.boot_speed_sneaking_multiplier", "Boots Sneaking Multiplier");
        add("config.avaritia.boot_speed_sneaking_multiplier.tooltip", "Infinity Boots speed multiplier when sneaking");
        add("config.avaritia.boot_speed_sprinting_multiplier", "Boots Sprinting Multiplier");
        add("config.avaritia.boot_speed_sprinting_multiplier.tooltip", "Infinity Boots additional speed when sprinting");
        add("config.avaritia.boot_speed_strafing_multiplier", "Boots Strafing Multiplier");
        add("config.avaritia.boot_speed_strafing_multiplier.tooltip", "Infinity Boots speed multiplier when strafing");
        add("config.avaritia.boot_speed_swimming_multiplier", "Boots Swimming Multiplier");
        add("config.avaritia.boot_speed_swimming_multiplier.tooltip", "Infinity Boots speed multiplier when swimming");
        add("config.avaritia.channel_fast_update_rate", "Fast Update Rate");
        add("config.avaritia.channel_fast_update_rate.tooltip", "Fast update rate for channels");
        add("config.avaritia.channel_full_update_rate", "Full Update Rate");
        add("config.avaritia.channel_full_update_rate.tooltip", "Full update rate for channels");
        add("config.avaritia.chest_max_item_size", "Chest Max Item Size");
        add("config.avaritia.chest_max_item_size.tooltip", "Define the maximum number of item types that can be stored in an Infinity Chest");
        add("config.avaritia.endless_item_entity_range", "Endless Item Range");
        add("config.avaritia.endless_item_entity_range.tooltip", "Tracking endless item range");
        add("config.avaritia.endless_item_entity_speed", "Endless Item Speed");
        add("config.avaritia.endless_item_entity_speed.tooltip", "Tracking endless item speed");
        add("config.avaritia.food_time", "Food Time");
        add("config.avaritia.food_time.tooltip", "Food effect time scaling factor");
        add("config.avaritia.growth_soul_farmland", "Growth Soul Farmland Rate");
        add("config.avaritia.growth_soul_farmland.tooltip", "Growth soul farmland rate");
        add("config.avaritia.infinity_elytra_flying_damage_range", "Elytra Flying Damage Range");
        add("config.avaritia.infinity_elytra_flying_damage_range.tooltip", "The damage range of Infinity Elytra flight collision");
        add("config.avaritia.infinity_elytra_flying_speed", "Infinity Elytra Flying Speed");
        add("config.avaritia.infinity_elytra_flying_speed.tooltip", "Speed of Infinity Elytra");
        add("config.avaritia.infinity_helmet_night_vision", "Infinity Helmet Night Vision");
        add("config.avaritia.infinity_helmet_night_vision.tooltip", "Whether wearing the Infinity Helmet activates night vision");
        add("config.avaritia.internal_infinity_catalyst_craft", "Internal Infinity Catalyst Craft");
        add("config.avaritia.internal_infinity_catalyst_craft.tooltip", "Whether Infinity Catalyst craft uses all singularities");
        add("config.avaritia.inventory_rows", "Inventory Rows");
        add("config.avaritia.inventory_rows.tooltip", "Inventory rows for multi-page mode");
        add("config.avaritia.is_keep_stone", "Keep Stone");
        add("config.avaritia.is_keep_stone.tooltip", "Whether the super mode of Infinity tools retains stone and soil");
        add("config.avaritia.is_merge_matter_cluster", "Merge Matter Cluster");
        add("config.avaritia.is_merge_matter_cluster.tooltip", "Whether to merge matter clusters");
        add("config.avaritia.is_sword_attack_endless", "Sword Causes Infinity Damage");
        add("config.avaritia.is_sword_attack_endless.tooltip", "Whether right-click causes infinity damage");
        add("config.avaritia.is_sword_attack_item_entity", "Sword Damages Item Entity");
        add("config.avaritia.is_sword_attack_item_entity.tooltip", "Whether kill mode kills item entities");
        add("config.avaritia.is_sword_attack_projectile", "Sword Damages Projectile");
        add("config.avaritia.is_sword_attack_projectile.tooltip", "Whether kill mode kills projectiles");
        add("config.avaritia.is_sword_attack_lightning", "Sword Causes Lightning");
        add("config.avaritia.is_sword_attack_lightning.tooltip", "Whether right-click spawns lightning in attack range");
        add("config.avaritia.max_channels_pre_player", "Max Player Channels");
        add("config.avaritia.max_channels_pre_player.tooltip", "Maximum channels per player");
        add("config.avaritia.max_page_limit", "Max Page Limit");
        add("config.avaritia.max_page_limit.tooltip", "Maximum page limit");
        add("config.avaritia.max_public_channels", "Max Public Channels");
        add("config.avaritia.max_public_channels.tooltip", "Maximum public channels");
        add("config.avaritia.max_size_pre_channel", "Channel Size");
        add("config.avaritia.max_size_pre_channel.tooltip", "Maximum size per channel");
        add("config.avaritia.neutron_collector_product_tick", "Neutron Collector Product Tick");
        add("config.avaritia.neutron_collector_product_tick.tooltip", "The product tick of Neutron Collector");
        add("config.avaritia.neutron_horse_speed", "Horse Armor Speed");
        add("config.avaritia.neutron_horse_speed.tooltip", "The speed of Neutronium Horse Armor when worn by a horse");
        add("config.avaritia.pickaxe_break_range", "Pickaxe Break Range");
        add("config.avaritia.pickaxe_break_range.tooltip", "The range that Infinity Pickaxe can break");
        add("config.avaritia.reset_max_page", "Reset Max Page");
        add("config.avaritia.reset_max_page.tooltip", "Recovery option: reset max page to 0");
        add("config.avaritia.shovel_break_range", "Shovel Break Range");
        add("config.avaritia.shovel_break_range.tooltip", "The range that Infinity Shovel can break");
        add("config.avaritia.singularity_time_required", "Singularity Time Required");
        add("config.avaritia.singularity_time_required.tooltip", "Singularity default time required");
        add("config.avaritia.slot_stack_limit", "Slot Stack Limit");
        add("config.avaritia.slot_stack_limit.tooltip", "Stack size limit of slot");
        add("config.avaritia.sub_arrow_damage", "Sub Arrow Damage");
        add("config.avaritia.sub_arrow_damage.tooltip", "Infinity Bow scattering light arrow damage");
        add("config.avaritia.sword_attack_range", "Sword Attack Range");
        add("config.avaritia.sword_attack_range.tooltip", "Infinity Sword right-click attack range");
        add("config.avaritia.sword_range_damage", "Sword Range Damage");
        add("config.avaritia.sword_range_damage.tooltip", "Range damage value of right-click Infinity Sword");
        add("config.avaritia.use_advance_tooltips", "Use Advanced Tooltips");
        add("config.avaritia.use_advance_tooltips.tooltip", "For development purposes");
        add("config.avaritia.use_single_page_mode", "Use Single Page Mode");
        add("config.avaritia.use_single_page_mode.tooltip", "Use single page mode");
        add("config.avaritia.blaze_cube_emc", "Blaze Cube EMC");
        add("config.avaritia.blaze_cube_emc.tooltip", "EMC of Blaze Cube");
        add("config.avaritia.neutron_pile_emc", "Pile of Neutrons EMC");
        add("config.avaritia.neutron_pile_emc.tooltip", "EMC of Pile of Neutrons");
        add("config.avaritia.vanilla_totem_emc", "Vanilla Totem EMC");
        add("config.avaritia.vanilla_totem_emc.tooltip", "EMC of Totem of Undying");
        add("config.avaritia.bedrock_emc", "Bedrock EMC");
        add("config.avaritia.bedrock_emc.tooltip", "EMC of Bedrock");
    }

    /**
     * 添加进度文本。
     */
    private void addAdvancements() {
        add("advancements.avaritia.start.title", "Welcome to Re:Avaritia!");
        add("advancements.avaritia.start.desc", "");
        add("advancements.avaritia.compressed_crafting_table.title", "First Contact");
        add("advancements.avaritia.compressed_crafting_table.desc", "Craft Compressed Crafting Table");
        add("advancements.avaritia.sculk_crafting_table.title", "Sound of Ancient");
        add("advancements.avaritia.sculk_crafting_table.desc", "Craft Sculk Crafting Table");
        add("advancements.avaritia.diamond_lattice.title", "Brilliant New Life");
        add("advancements.avaritia.diamond_lattice.desc", "Craft Diamond Lattice");
        add("advancements.avaritia.crystal_matrix_ingot.title", "Crystal Energy Emergence");
        add("advancements.avaritia.crystal_matrix_ingot.desc", "Craft Crystal Matrix Ingot");
        add("advancements.avaritia.crystal_pickaxe.title", "Bedrock Breaker");
        add("advancements.avaritia.crystal_pickaxe.desc", "Craft Crystal Pickaxe");
        add("advancements.avaritia.nether_crafting_table.title", "Realm of Fire");
        add("advancements.avaritia.nether_crafting_table.desc", "Craft Nether Crafting Table");
        add("advancements.avaritia.blaze_cube.title", "Scorching Bones");
        add("advancements.avaritia.blaze_cube.desc", "Craft Blaze Cube");
        add("advancements.avaritia.blaze_sword.title", "Skeleton's Nightmare");
        add("advancements.avaritia.blaze_sword.desc", "Craft Blaze Skull Sword");
        add("advancements.avaritia.blaze_axe.title", "Blaze Annihilates Forest");
        add("advancements.avaritia.blaze_axe.desc", "Craft Blaze Bush Axe");
        add("advancements.avaritia.refined_coal.title", "High Efficiency Energy");
        add("advancements.avaritia.refined_coal.desc", "Craft Refined Coal");
        add("advancements.avaritia.end_crafting_table.title", "End and Beginning");
        add("advancements.avaritia.end_crafting_table.desc", "Craft End Crafting Table");
        add("advancements.avaritia.neutron_collector.title", "Long Wait");
        add("advancements.avaritia.neutron_collector.desc", "Craft Neutronium Collector");
        add("advancements.avaritia.eternal_singularity.title", "Zero to Whole - Twice");
        add("advancements.avaritia.eternal_singularity.desc", "Craft Eternal Singularity");
        add("advancements.avaritia.infinity_umbrella.title", "Zeus");
        add("advancements.avaritia.infinity_umbrella.desc", "Craft Infinity Umbrella");
        add("advancements.avaritia.infinity_clock.title", "Chronos");
        add("advancements.avaritia.infinity_clock.desc", "Craft Infinity Clock");
        add("advancements.avaritia.upgrade_smithing_template.title", "Wandering the Ends of the Earth");
        add("advancements.avaritia.upgrade_smithing_template.desc", "Craft Sublimation Template");
        add("advancements.avaritia.enhancement_core.title", "Road to Completion");
        add("advancements.avaritia.enhancement_core.desc", "Craft Flawless Core");
        add("advancements.avaritia.extreme_smithing_table.title", "Start of Nightmare - Again");
        add("advancements.avaritia.extreme_smithing_table.desc", "Craft Ultimate Smithing Table");
        add("advancements.avaritia.endest_pearl.title", "Small black hole");
        add("advancements.avaritia.endest_pearl.desc", "Use Endest Pearl");
        add("advancements.avaritia.extreme_crafting_table.title", "The Beginning of a Nightmare");
        add("advancements.avaritia.extreme_crafting_table.desc", "Craft Extreme Crafting Table");
        add("advancements.avaritia.infinity_armor.title", "INFINITY");
        add("advancements.avaritia.infinity_armor.desc", "Get all Infinity Armor");
        add("advancements.avaritia.infinity_axe.title", "Desolate nature!");
        add("advancements.avaritia.infinity_axe.desc", "Craft Infinity Axe");
        add("advancements.avaritia.infinity_bow.title", "Heaven falls");
        add("advancements.avaritia.infinity_bow.desc", "Craft Infinity Bow");
        add("advancements.avaritia.infinity_catalyst.title", "-Part is whole, whole is Part-");
        add("advancements.avaritia.infinity_catalyst.desc", "Craft Infinity Catalyst");
        add("advancements.avaritia.infinity_food.title", "Clean and hygienic");
        add("advancements.avaritia.infinity_food.desc", "Eat!");
        add("advancements.avaritia.infinity_hoe.title", "Earth Resurrection!");
        add("advancements.avaritia.infinity_hoe.desc", "Craft Infinity Hoe");
        add("advancements.avaritia.infinity_ingot.title", "Sweet are the uses of adversity?");
        add("advancements.avaritia.infinity_ingot.desc", "Get Infinity Ingot");
        add("advancements.avaritia.infinity_pickaxe.title", "World falling apart!");
        add("advancements.avaritia.infinity_pickaxe.desc", "Craft Infinity Pickaxe");
        add("advancements.avaritia.infinity_shovel.title", "Planet Devour!");
        add("advancements.avaritia.infinity_shovel.desc", "Craft Infinity Shovel");
        add("advancements.avaritia.infinity_sword.title", "World domination!");
        add("advancements.avaritia.infinity_sword.desc", "Craft Infinity Sword");
        add("advancements.avaritia.matter_cluster.title", "Cotton artifact");
        add("advancements.avaritia.matter_cluster.desc", "Get Matter Cluster");
        add("advancements.avaritia.neutron_compressor.title", "Zero to whole");
        add("advancements.avaritia.neutron_compressor.desc", "Craft Neutronium Compressor");
        add("advancements.avaritia.neutron_ingot.title", "Very hard to get");
        add("advancements.avaritia.neutron_ingot.desc", "Craft Neutronium Ingot");
        add("advancements.avaritia.neutron_pile.title", "Endless hang up");
        add("advancements.avaritia.neutron_pile.desc", "Get a pile of neutrons!");
        add("advancements.avaritia.singularity.title", "Running low on stock");
        add("advancements.avaritia.singularity.desc", "Get a singularity");
        add("advancements.avaritia.star_fuel.title", "Stellar Energy");
        add("advancements.avaritia.star_fuel.desc", "Get Star Fuel");
    }

    /**
     * 添加其他界面、键位和提示文本。
     */
    private void addMiscTexts() {
        add("attribute.name.generic.walking_speed", "Walking Speed");
        add("direction.avaritia.down", "down");
        add("direction.avaritia.east", "east");
        add("direction.avaritia.north", "north");
        add("direction.avaritia.south", "south");
        add("direction.avaritia.up", "up");
        add("direction.avaritia.west", "west");
        add("key.avaritia.categories", "Avaritia Key Bindings");
        add("key.avaritia.config", "Open Avaritia Config");
        add("key.avaritia.filter", "Open filter screen");
        add("key.avaritia.neutron_ring", "Open Neutronium Ring");
        add("rarity.avaritia.cosmic", "Cosmic");
        add("rarity.avaritia.legend", "Legend");
        add("rarity.cosmic.name", "Cosmic");
        add("rarity.legend.name", "Legend");

        add("gui.avaritia.addChannel.tip1", "Add \"%d\"");
        add("gui.avaritia.addChannel.tip2", "§aLClick§r : Add to yours");
        add("gui.avaritia.addChannel.tip3", "§dLSHIFT§r + §aLClick§r : Add to public");
        add("gui.avaritia.addChannel.tip4", "§cConsume a Storage Core!");
        add("gui.avaritia.apply", "Apply");
        add("gui.avaritia.back", "back");
        add("gui.avaritia.backChannel.tip1", "Back");
        add("gui.avaritia.cancel", "Cancel");
        add("gui.avaritia.add", "Add");
        add("gui.avaritia.clear", "Clear");
        add("gui.avaritia.capability.tip1", "§aLMB§r : Input 1 stack %d to carried");
        add("gui.avaritia.capability.tip2", "§aRMB§r : Output 1 stack object from carried");
        add("gui.avaritia.capability.tip3", "§dLSHIFT§r : Full it !");
        add("gui.avaritia.channel.tip1", "Channel: \"%d\"");
        add("gui.avaritia.channel.tip2", "Owner: \"%d\"");
        add("gui.avaritia.confirm", "Confirm");
        add("gui.avaritia.craft.channel", "Craft to channel");
        add("gui.avaritia.craft.drop", "Craft and drop");
        add("gui.avaritia.craft.inv", "Craft to inventory");
        add("gui.avaritia.craft.missing", "Missing items (can click)");
        add("gui.avaritia.craft.tip1", "§aLMB§r: Craft 64");
        add("gui.avaritia.craft.tip2", "§aRMB§r: Craft 8");
        add("gui.avaritia.craft.tip3", "§dLSHIFT§r + §aLMB§r: Craft 512");
        add("gui.avaritia.craft.tip4", "§dLSHIFT§r + §aRMB§r: Craft 1");
        add("gui.avaritia.emptyChannel.tip4", "§cThis terminal has not selected a channel");
        add("gui.avaritia.line", "---------------------");
        add("gui.avaritia.name", "name");
        add("gui.avaritia.noPermission.tip3", "§cYou don't have permissions");
        add("gui.avaritia.owner", "Owner: %d");
        add("gui.avaritia.port.down", "Down");
        add("gui.avaritia.port.east", "East");
        add("gui.avaritia.port.input", "Input to channel");
        add("gui.avaritia.port.north", "North");
        add("gui.avaritia.port.output", "Output from channel");
        add("gui.avaritia.port.south", "South");
        add("gui.avaritia.port.tip", "Click toggle enable");
        add("gui.avaritia.port.up", "Up");
        add("gui.avaritia.port.west", "West");
        add("gui.avaritia.public", "Public");
        add("gui.avaritia.rate.tip", "Active port working rate; lower is faster");
        add("gui.avaritia.remove", "Remove");
        add("gui.avaritia.removeChannel.tip1", "Remove: \"%d\"");
        add("gui.avaritia.removeChannel.tip2", "§cChannel must be empty");
        add("gui.avaritia.renameChannel.tip1", "Rename: \"%d\"");
        add("gui.avaritia.renameChannel.tip2", "To: \"%d\"");
        add("gui.avaritia.rule.any_fluid", "Any fluid");
        add("gui.avaritia.rule.any_item", "Any item");
        add("gui.avaritia.rule.fe", "Energy (FE)");
        add("gui.avaritia.rule.fluid", "Fluid: %d");
        add("gui.avaritia.rule.item", "Item: %d");
        add("gui.avaritia.rule.item_tag", "Tag: %d");
        add("gui.avaritia.rule.mod_fluid", "Mod (fluid): %d");
        add("gui.avaritia.rule.mod_item", "Mod (item): %d");
        add("gui.avaritia.rule.tip", "Select with the mouse wheel");
        add("gui.avaritia.save", "Save");
        add("gui.avaritia.search", "search");
        add("gui.avaritia.search.tip1", "Empty prefix searches ID and name");
        add("gui.avaritia.search.tip2", "Prefix \"§a*§r\" searches ID only");
        add("gui.avaritia.search.tip3", "Prefix \"§a$§r\" searches tags");
        add("gui.avaritia.sort.ascending", "Ascending");
        add("gui.avaritia.sort.count", "Sort : §aCount");
        add("gui.avaritia.sort.descending", "Descending");
        add("gui.avaritia.sort.id", "Sort : §aID");
        add("gui.avaritia.sort.mirror_id", "Sort : §aMirror ID");
        add("gui.avaritia.sort.nid", "Sort : §aMOD then ID");
        add("gui.avaritia.sort.tip1", "§aLMB§r : Cycle sort type");
        add("gui.avaritia.sort.tip2", "§dLSHIFT§r + §aLMB§r : Switch ascending");
        add("gui.avaritia.view.all", "§aAll");
        add("gui.avaritia.view.fluid", "§aFluids");
        add("gui.avaritia.view.item", "§aItems");
        add("gui.avaritia.item_filter.count", "Filters: %s");
        add("gui.avaritia.item_select.all", "All");
        add("gui.avaritia.item_select.count", "Items: %s");
        add("gui.avaritia.item_select.inventory", "Inventory");
        add("title.avaritia.item_filter", "Infinity Tool Filter");
        add("title.avaritia.item_select", "Select Item");

        add("info.avaritia.channel.add_success", "Successfully added channel: %s %s %s");
        add("info.avaritia.channel.load_error", "Load Channel Error!");
        add("info.avaritia.channel.load_finish", "Channel load finished!");
        add("info.avaritia.channel.load_success", "Successfully loaded channel: %s %s %s");
        add("info.avaritia.channel.save_success", "Successfully saved channel: %s %s %s");
        add("info.avaritia.infinity_chest.add_success", "Successfully added infinity chest: %s %s %s");
        add("info.avaritia.infinity_chest.load_error", "Load infinity chest Error!");
        add("info.avaritia.infinity_chest.load_finish", "Infinity chest load finished!");
        add("info.avaritia.infinity_chest.load_success", "Successfully loaded infinity chest: %s %s %s");
        add("info.avaritia.infinity_chest.save_success", "Successfully saved infinity chest: %s %s %s");
    }
}
