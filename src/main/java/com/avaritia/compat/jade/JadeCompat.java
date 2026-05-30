package com.avaritia.compat.jade;

import com.avaritia.Avaritia;
import com.avaritia.Const;
import com.avaritia.api.common.crafting.TierInput;
import com.avaritia.common.block.collector.NeutronCollectorBlock;
import com.avaritia.common.block.compressor.NeutronCompressorBlock;
import com.avaritia.common.block.craft.TierCraftTableBlock;
import com.avaritia.common.block.extreme.ExtremeSmithingTableBlock;
import com.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import com.avaritia.common.tile.NeutronCollectorTile;
import com.avaritia.common.tile.NeutronCompressorTile;
import com.avaritia.common.tile.TierCraftTile;
import com.avaritia.init.registry.ModRecipeTypes;
import com.avaritia.init.registry.ModTooltips;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.text.DecimalFormat;

/**
 * Jade compatibility providers for Avaritia machines.
 */
@WailaPlugin
public class JadeCompat implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CollectorComponentProvider.INSTANCE, NeutronCollectorBlock.class);
        registration.registerBlockComponent(CompressorComponentProvider.INSTANCE, NeutronCompressorBlock.class);
        registration.registerBlockComponent(CraftingComponentProvider.INSTANCE, TierCraftTableBlock.class);
        registration.registerBlockComponent(ExtremeSmithingComponentProvider.INSTANCE, ExtremeSmithingTableBlock.class);
    }

    public enum CollectorComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof NeutronCollectorTile collector)) {
                return;
            }

            int progress = collector.data.get(0);
            int productionTicks = collector.getProductionTicks();
            if (progress > 0 && productionTicks > 0) {
                double fraction = (double) Mth.clamp(progress, 0, productionTicks) / productionTicks;
                tooltip.add(ModTooltips.PROGRESS.args(formatFraction(fraction)).build());
            }

            tooltip.add(Component.translatable("tooltip.avaritia.jade.collector_output", collector.getProduction().getHoverName()));
        }

        @Override
        public Identifier getUid() {
            return Const.rl("neutron_collector");
        }
    }

    public enum CompressorComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof NeutronCompressorTile compressor)) {
                return;
            }

            var recipe = compressor.getActiveRecipe();
            if (recipe != null) {
                var level = accessor.getLevel();
                var output = recipe.getResultItem(level.registryAccess());
                tooltip.add(ModTooltips.COMPRESS.args(output.getCount(), output.getHoverName()).build());
            }
        }

        @Override
        public Identifier getUid() {
            return Const.rl("compressor");
        }
    }

    public enum CraftingComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var level = Minecraft.getInstance().level;
            if (level == null || !(accessor.getBlockEntity() instanceof TierCraftTile craftTile)) {
                return;
            }

            var recipe = level.getRecipeManager()
                    .getRecipeFor(ModRecipeTypes.CRAFTING_TABLE_RECIPE.get(),
                            TierInput.of(craftTile.tier.size, craftTile.tier.size, craftTile.getInventory().getStacks(), craftTile.tier.ordinal()), level)
                    .map(RecipeHolder::value)
                    .orElse(null);

            if (recipe != null) {
                var output = recipe.getResultItem(level.registryAccess());
                tooltip.add(ModTooltips.CRAFTING.args(Component.translatable("jei.category.avaritia." + craftTile.tier.name).getString(), output.getCount(), output.getHoverName()).build());
            }
        }

        @Override
        public Identifier getUid() {
            return Const.rl("crafting_table");
        }
    }

    public enum ExtremeSmithingComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }

            var recipes = level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.EXTREME_SMITHING_RECIPE.get());
            if (!recipes.isEmpty()) {
                ExtremeSmithingRecipe recipe = recipes.getFirst().value();
                var output = recipe.getResultItem(level.registryAccess());
                tooltip.add(ModTooltips.SMITHING.args(output.getCount(), output.getHoverName()).build());
            }
        }

        @Override
        public Identifier getUid() {
            return Const.rl("extreme_smithing");
        }
    }

    private static String formatFraction(double value) {
        return new DecimalFormat("0.00%").format(value);
    }
}
