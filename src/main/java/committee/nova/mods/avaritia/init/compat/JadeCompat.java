package committee.nova.mods.avaritia.init.compat;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.block.compressor.CompressorBlock;
import committee.nova.mods.avaritia.common.block.extreme.ExtremeCraftingTableBlock;
import committee.nova.mods.avaritia.common.tile.CompressorTile;
import committee.nova.mods.avaritia.common.tile.ExtremeCraftingTile;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.client.Minecraft;
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
public final class JadeCompat implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CompressorComponentProvider.INSTANCE, CompressorBlock.class);
        registration.registerBlockComponent(ExtremeComponentProvider.INSTANCE, ExtremeCraftingTableBlock.class);
    }

    public enum CompressorComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var level = Minecraft.getInstance().level;
            assert level != null;
            var compressor = (CompressorTile) accessor.getBlockEntity();
            var recipe = compressor.getActiveRecipe();

            if (recipe != null) {
                var output = recipe.getResultItem(level.registryAccess());
                tooltip.add(ModTooltips.COMPRESS.args(output.getCount(), output.getHoverName()).build());
            }
        }

        @Override
        public ResourceLocation getUid() {
            return new ResourceLocation(Static.MOD_ID, "compressor");
        }
    }

    public enum ExtremeComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var level = Minecraft.getInstance().level;
            assert level != null;
            var compressor = (ExtremeCraftingTile) accessor.getBlockEntity();
            var recipe = level.getRecipeManager().getRecipeFor(ModRecipeTypes.EXTREME_CRAFT_RECIPE.get(), compressor.getInventory().toIInventory(), level);

            if (recipe.isPresent()) {
                var output = recipe.get().getResultItem(level.registryAccess());
                tooltip.add(ModTooltips.CRAFTING.args(output.getCount(), output.getHoverName()).build());
            }
        }

        @Override
        public ResourceLocation getUid() {
            return new ResourceLocation(Static.MOD_ID, "extreme");
        }
    }
}
