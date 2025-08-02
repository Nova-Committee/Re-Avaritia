package committee.nova.mods.avaritia.init.compat;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.block.compressor.CompressorBlock;
import committee.nova.mods.avaritia.common.block.craft.TierCraftTableBlock;
import committee.nova.mods.avaritia.common.block.extreme.ExtremeSmithingTableBlock;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import committee.nova.mods.avaritia.common.tile.TierCraftTile;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 23:21
 * Version: 1.0
 */
@WailaPlugin
public class JadeCompat implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CompressorComponentProvider.INSTANCE, CompressorBlock.class);
        registration.registerBlockComponent(CraftingComponentProvider.INSTANCE, TierCraftTableBlock.class);
        registration.registerBlockComponent(ExtremeSmithingComponentProvider.INSTANCE, ExtremeSmithingTableBlock.class);
    }

    public enum CompressorComponentProvider implements IBlockComponentProvider {

        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var level = Minecraft.getInstance().level;
            assert level != null;
            var compressor = (NeutronCompressorTile) accessor.getBlockEntity();
            var recipe = compressor.getActiveRecipe();

            if (recipe != null) {
                var output = recipe.getResultItem(level.registryAccess());
                tooltip.add(ModTooltips.COMPRESS.args(output.getCount(), output.getHoverName()).build());
            }
        }

        @Override
        public ResourceLocation getUid() {
            return new ResourceLocation(Const.MOD_ID, "compressor");
        }
    }

    public enum CraftingComponentProvider implements IBlockComponentProvider {

        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var level = Minecraft.getInstance().level;
            assert level != null;
            var craftTile = (TierCraftTile) accessor.getBlockEntity();
            var recipe = level.getRecipeManager().getRecipeFor(ModRecipeTypes.CRAFTING_TABLE_RECIPE.get(), craftTile.getInventory().toIInventory(), level);

            if (recipe.isPresent()) {
                var output = recipe.get().getResultItem(level.registryAccess());
                tooltip.add(ModTooltips.CRAFTING.args(I18n.get("jei.category.avaritia." + craftTile.tier.name), output.getCount(), output.getHoverName()).build());
            }
        }

        @Override
        public ResourceLocation getUid() {
            return new ResourceLocation(Const.MOD_ID, "crafting_table");
        }
    }

    public enum ExtremeSmithingComponentProvider implements IBlockComponentProvider {

        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var level = Minecraft.getInstance().level;
            assert level != null;
            var recipes = level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.EXTREME_SMITHING_RECIPE.get());
            if (!recipes.isEmpty()) {
                ExtremeSmithingRecipe recipe = recipes.get(0);
                var output = recipe.getResultItem(level.registryAccess());
                tooltip.add(ModTooltips.SMITHING.args(output.getCount(), output.getHoverName()).build());
            }
        }

        @Override
        public ResourceLocation getUid() {
            return new ResourceLocation(Const.MOD_ID, "extreme_smithing");
        }
    }
}
