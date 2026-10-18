package committee.nova.mods.avaritia.init.compat;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.block.compressor.NeutronCompressorBlock;
import committee.nova.mods.avaritia.common.block.craft.TierCraftTableBlock;
import committee.nova.mods.avaritia.common.block.extreme.ExtremeSmithingTableBlock;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import committee.nova.mods.avaritia.common.tile.TierCraftTile;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/5/15 23:21
 * Version: 1.0
 */
@WailaPlugin
public class JadeCompat implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CompressorComponentProvider.INSTANCE, NeutronCompressorBlock.class);
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
        public Identifier getUid() {
            return Identifier.fromNamespaceAndPath(Const.MOD_ID, "compressor");
        }
    }

    public enum CraftingComponentProvider implements IBlockComponentProvider {

        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var level = Minecraft.getInstance().level;
            assert level != null;
            var craftTile = (TierCraftTile) accessor.getBlockEntity();
            var container = craftTile.getInventory().toIInventory();
            List<ItemStack> items = new ArrayList<>(container.getContainerSize());
            for (int i = 0; i < container.getContainerSize(); i++) {
                items.add(container.getItem(i));
            }
            CraftingInput input = CraftingInput.of(craftTile.tier.size, craftTile.tier.size, items);
            var recipe = level.recipeAccess().getSynchronizedRecipes().getFirstMatch(ModRecipeTypes.CRAFTING_TABLE_RECIPE.get(), input, level);

            if (recipe.isPresent()) {
                var output = recipe.get().value().getResultItem(level.registryAccess());
                tooltip.add(ModTooltips.CRAFTING.args(I18n.get("jei.category.avaritia." + craftTile.tier.name), output.getCount(), output.getHoverName()).build());
            }
        }

        @Override
        public Identifier getUid() {
            return Identifier.fromNamespaceAndPath(Const.MOD_ID, "crafting_table");
        }
    }

    public enum ExtremeSmithingComponentProvider implements IBlockComponentProvider {

        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var level = Minecraft.getInstance().level;
            assert level != null;
            Collection<RecipeHolder<ExtremeSmithingRecipe>> recipes = level.recipeAccess().getSynchronizedRecipes().getAllOfType(ModRecipeTypes.EXTREME_SMITHING_RECIPE.get());
            if (!recipes.isEmpty()) {
                ExtremeSmithingRecipe recipe = recipes.iterator().next().value();
                var output = recipe.getResultItem(level.registryAccess());
                tooltip.add(ModTooltips.SMITHING.args(output.getCount(), output.getHoverName()).build());
            }
        }

        @Override
        public Identifier getUid() {
            return Identifier.fromNamespaceAndPath(Const.MOD_ID, "extreme_smithing");
        }
    }
}
