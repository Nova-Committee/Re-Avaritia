package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

import java.util.List;

/**
 * @author cnlimiter
 * @version 1.0
 */
public class ModSingularities {

    public static final Singularity OBSIDIAN = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "obsidian"), "singularity.avaritia.obsidian", new int[]{0x3B2754, 0x100C1C}, Ingredient.of(Items.OBSIDIAN));
    public static final Singularity BLUE_ICE = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "blue_ice"), "singularity.avaritia.blue_ice", new int[]{0x8EB8FE, 0x6B9DFB}, Ingredient.of(Items.BLUE_ICE));
    public static final Singularity COAL = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "coal"), "singularity.avaritia.coal", new int[]{0x363739, 0x261E24}, Ingredient.of(Items.COAL));
    public static final Singularity COPPER = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "copper"), "singularity.avaritia.copper", new int[]{0xFA977C, 0xBC5430}, Ingredient.of(Items.COPPER_INGOT));
    public static final Singularity IRON = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "iron"), "singularity.avaritia.iron", new int[]{0xE1E1E1, 0x6C6C6C}, Ingredient.of(Items.IRON_INGOT));
    public static final Singularity LAPIS_LAZULI = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "lapis_lazuli"), "singularity.avaritia.lapis_lazuli", new int[]{0x678DEA, 0x1B53A7}, Ingredient.of(Items.LAPIS_LAZULI));
    public static final Singularity REDSTONE = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "redstone"), "singularity.avaritia.redstone", new int[]{0xFF0000, 0x8A0901}, Ingredient.of(Items.REDSTONE));
    public static final Singularity GLOWSTONE = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "glowstone"), "singularity.avaritia.glowstone", new int[]{0xFFD38F, 0xA06135}, Ingredient.of(Items.GLOWSTONE_DUST));
    public static final Singularity GOLD = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "gold"), "singularity.avaritia.gold", new int[]{0xFDF55F, 0xD98E04}, Ingredient.of(Items.GOLD_INGOT));
    public static final Singularity DIAMOND = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "diamond"), "singularity.avaritia.diamond", new int[]{0xA6FCE9, 0x1AACA8}, Ingredient.of(Items.DIAMOND));
    public static final Singularity EMERALD = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "emerald"), "singularity.avaritia.emerald", new int[]{0x7DF8AC, 0x008E1A}, Ingredient.of(Items.EMERALD));
    public static final Singularity QUARTZ = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "quartz"), "singularity.avaritia.quartz", new int[]{0xEAE5DE, 0xB6A48E}, Ingredient.of(Items.QUARTZ));
    public static final Singularity AMETHYST_SHARD = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "amethyst_shard"), "singularity.avaritia.amethyst_shard", new int[]{0xCFA0F3, 0x8D6ACC}, Ingredient.of(Items.AMETHYST_SHARD));
    public static final Singularity NETHERITE = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "netherite"), "singularity.avaritia.netherite", new int[]{0x443a3b, 0x1a1616}, Ingredient.of(Items.NETHERITE_INGOT));

    public static final Singularity ALUMINUM = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "aluminum"), "singularity.avaritia.aluminum", new int[]{0xCACCDA, 0x9A9CA6},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/aluminum"))), new NotCondition(new TagEmptyCondition("forge:ingots/aluminum")));
    public static final Singularity TIN = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "tin"), "singularity.avaritia.tin", new int[]{0xA0BEBD, 0x527889},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/tin"))), new NotCondition(new TagEmptyCondition("forge:ingots/tin")));
    public static final Singularity BRONZE = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "bronze"), "singularity.avaritia.bronze", new int[]{0xD99F43, 0xBB6B3B},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/bronze"))), new NotCondition(new TagEmptyCondition("forge:ingots/bronze")));
    public static final Singularity SILVER = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "silver"), "singularity.avaritia.silver", new int[]{0xC0CDD2, 0x5F6E7C},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/silver"))), new NotCondition(new TagEmptyCondition("forge:ingots/silver")));
    public static final Singularity LEAD = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "lead"), "singularity.avaritia.lead", new int[]{0x6C7D92, 0x323562},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/lead"))), new NotCondition(new TagEmptyCondition("forge:ingots/lead")));
    public static final Singularity STEEL = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "steel"), "singularity.avaritia.steel", new int[]{0x565656, 0x232323},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/steel"))), new NotCondition(new TagEmptyCondition("forge:ingots/steel")));
    public static final Singularity NICKEL = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "nickel"), "singularity.avaritia.nickel", new int[]{0xE1D798, 0xB1976C},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/nickel"))), new NotCondition(new TagEmptyCondition("forge:ingots/nickel")));
    public static final Singularity ELECTRUM = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "electrum"), "singularity.avaritia.electrum", new int[]{0xF5F18E, 0x9E8D3E},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/electrum"))), new NotCondition(new TagEmptyCondition("forge:ingots/electrum")));
    public static final Singularity INVAR = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "invar"), "singularity.avaritia.invar", new int[]{0xBCC5BB, 0x5D7877},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/invar"))), new NotCondition(new TagEmptyCondition("forge:ingots/invar")));
    public static final Singularity PLATINUM = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "platinum"), "singularity.avaritia.platinum", new int[]{0x6FEAEF, 0x57B8BC},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/platinum"))), new NotCondition(new TagEmptyCondition("forge:ingots/platinum")));
    public static final Singularity URANIUM = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "uranium"), "singularity.avaritia.uranium", new int[]{0xd2f9d1, 0xa6c5a4},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/uranium"))), new NotCondition(new TagEmptyCondition("forge:ingots/uranium")));
    public static final Singularity OSMIUM = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "osmium"), "singularity.avaritia.osmium", new int[]{0xe6eef7, 0xc0c4cd},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/osmium"))), new NotCondition(new TagEmptyCondition("forge:ingots/osmium")));
    public static final Singularity REFINED_OBSIDIAN = Singularity.create(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "refined_obsidian"), "singularity.avaritia.refined_obsidian", new int[]{0xa9a1b8, 0x8d78b7},
            Ingredient.of(ItemTags.create(ResourceLocation.parse("forge:ingots/refined_obsidian"))), new NotCondition(new TagEmptyCondition("forge:ingots/refined_obsidian")));

    public static List<Singularity> getDefaults() {
        return List.of(
                OBSIDIAN,
                BLUE_ICE,
                COAL,
                IRON,
                LAPIS_LAZULI,
                REDSTONE,
                GLOWSTONE,
                GOLD,
                DIAMOND,
                EMERALD,
                QUARTZ,
                AMETHYST_SHARD,
                NETHERITE,

                ALUMINUM,
                COPPER,
                TIN,
                BRONZE,
                SILVER,
                LEAD,
                STEEL,
                NICKEL,
                ELECTRUM,
                INVAR,
                PLATINUM,
                URANIUM,
                OSMIUM,
                REFINED_OBSIDIAN
        );
    }
}
