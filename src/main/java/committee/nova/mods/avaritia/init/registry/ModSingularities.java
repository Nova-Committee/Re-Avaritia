package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:37
 * Version: 1.0
 */
public class ModSingularities {
    public static final Singularity COAL = new Singularity(new ResourceLocation(Static.MOD_ID, "coal"), "singularity.avaritia.coal", new int[]{0x363739, 0x261E24}, Ingredient.of(Items.COAL));
    public static final Singularity COPPER = new Singularity(new ResourceLocation(Static.MOD_ID, "copper"), "singularity.avaritia.copper", new int[]{0xFA977C, 0xBC5430}, Ingredient.of(Items.COPPER_INGOT));
    public static final Singularity IRON = new Singularity(new ResourceLocation(Static.MOD_ID, "iron"), "singularity.avaritia.iron", new int[]{0xE1E1E1, 0x6C6C6C}, Ingredient.of(Items.IRON_INGOT));
    public static final Singularity LAPIS_LAZULI = new Singularity(new ResourceLocation(Static.MOD_ID, "lapis_lazuli"), "singularity.avaritia.lapis_lazuli", new int[]{0x678DEA, 0x1B53A7}, Ingredient.of(Items.LAPIS_LAZULI));
    public static final Singularity REDSTONE = new Singularity(new ResourceLocation(Static.MOD_ID, "redstone"), "singularity.avaritia.redstone", new int[]{0xFF0000, 0x8A0901}, Ingredient.of(Items.REDSTONE));
    public static final Singularity GLOWSTONE = new Singularity(new ResourceLocation(Static.MOD_ID, "glowstone"), "singularity.avaritia.glowstone", new int[]{0xFFD38F, 0xA06135}, Ingredient.of(Items.GLOWSTONE_DUST));
    public static final Singularity GOLD = new Singularity(new ResourceLocation(Static.MOD_ID, "gold"), "singularity.avaritia.gold", new int[]{0xFDF55F, 0xD98E04}, Ingredient.of(Items.GOLD_INGOT));
    public static final Singularity DIAMOND = new Singularity(new ResourceLocation(Static.MOD_ID, "diamond"), "singularity.avaritia.diamond", new int[]{0xA6FCE9, 0x1AACA8}, Ingredient.of(Items.DIAMOND));
    public static final Singularity EMERALD = new Singularity(new ResourceLocation(Static.MOD_ID, "emerald"), "singularity.avaritia.emerald", new int[]{0x7DF8AC, 0x008E1A}, Ingredient.of(Items.EMERALD));
    public static final Singularity NETHERITE = new Singularity(new ResourceLocation(Static.MOD_ID, "netherite"), "singularity.avaritia.netherite", new int[]{0x443a3b, 0x1a1616}, Ingredient.of(Items.NETHERITE_INGOT));

    public static final Singularity ALUMINUM = new Singularity(new ResourceLocation(Static.MOD_ID, "aluminum"), "singularity.avaritia.aluminum", new int[]{0xCACCDA, 0x9A9CA6}, "c:aluminum_ingots");
    public static final Singularity TIN = new Singularity(new ResourceLocation(Static.MOD_ID, "tin"), "singularity.avaritia.tin", new int[]{0xA0BEBD, 0x527889}, "c:tin_ingots");
    public static final Singularity BRONZE = new Singularity(new ResourceLocation(Static.MOD_ID, "bronze"), "singularity.avaritia.bronze", new int[]{0xD99F43, 0xBB6B3B}, "c:bronze_ingots");
    public static final Singularity SILVER = new Singularity(new ResourceLocation(Static.MOD_ID, "silver"), "singularity.avaritia.silver", new int[]{0xC0CDD2, 0x5F6E7C}, "c:silver_ingots");
    public static final Singularity LEAD = new Singularity(new ResourceLocation(Static.MOD_ID, "lead"), "singularity.avaritia.lead", new int[]{0x6C7D92, 0x323562}, "c:lead_ingots");
    public static final Singularity STEEL = new Singularity(new ResourceLocation(Static.MOD_ID, "steel"), "singularity.avaritia.steel", new int[]{0x565656, 0x232323}, "c:steel_ingots");
    public static final Singularity NICKEL = new Singularity(new ResourceLocation(Static.MOD_ID, "nickel"), "singularity.avaritia.nickel", new int[]{0xE1D798, 0xB1976C}, "c:nickel_ingots");
    public static final Singularity ELECTRUM = new Singularity(new ResourceLocation(Static.MOD_ID, "electrum"), "singularity.avaritia.electrum", new int[]{0xF5F18E, 0x9E8D3E}, "c:electrum_ingots");
    public static final Singularity INVAR = new Singularity(new ResourceLocation(Static.MOD_ID, "invar"), "singularity.avaritia.invar", new int[]{0xBCC5BB, 0x5D7877}, "c:invar_ingots");
    public static final Singularity PLATINUM = new Singularity(new ResourceLocation(Static.MOD_ID, "platinum"), "singularity.avaritia.platinum", new int[]{0x6FEAEF, 0x57B8BC}, "c:platinum_ingots");

    public static List<Singularity> getDefaults() {
        return List.of(
                COAL,
                IRON,
                LAPIS_LAZULI,
                REDSTONE,
                GLOWSTONE,
                GOLD,
                DIAMOND,
                EMERALD,
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
                PLATINUM
        );
    }
}
