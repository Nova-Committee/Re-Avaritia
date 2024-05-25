package committee.nova.mods.avaritia.util.registry;

import committee.nova.mods.avaritia.Static;
import io.github.fabricators_of_create.porting_lib.item.api.itemgroup.PortingLibCreativeTab;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupBuilderImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * FabricRegistry
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/23 上午11:52
 */
public class FabricRegistry {
    public static FabricRegistry INSTANCE = new FabricRegistry();
    public RegistryHolder<Item> createItemRegistryHolder() {
        return new FabricRegistryHolder<>(BuiltInRegistries.ITEM, Static.MOD_ID);
    }

    public RegistryHolder<Block> createBlockRegistryHolder() {
        return new FabricRegistryHolder<>(BuiltInRegistries.BLOCK, Static.MOD_ID);
    }

    public RegistryHolder<BlockEntityType<?>> createBlockEntityRegistryHolder() {
        return new FabricRegistryHolder<>(BuiltInRegistries.BLOCK_ENTITY_TYPE, Static.MOD_ID);
    }

    public RegistryHolder<CreativeModeTab> createCreativeTabRegistryHolder() {
        return new FabricRegistryHolder<>(BuiltInRegistries.CREATIVE_MODE_TAB, Static.MOD_ID);
    }

    public RegistryHolder<MenuType<?>> createMenusRegistryHolder() {
        return new FabricRegistryHolder<>(BuiltInRegistries.MENU, Static.MOD_ID);
    }

    public RegistryHolder<RecipeSerializer<?>> createRecipeSerializerRegistryHolder() {
        return new FabricRegistryHolder<>(BuiltInRegistries.RECIPE_SERIALIZER, Static.MOD_ID);
    }

    public RegistryHolder<RecipeType<?>> createRecipeTypeRegistryHolder() {
        return new FabricRegistryHolder<>(BuiltInRegistries.RECIPE_TYPE, Static.MOD_ID);
    }

    public RegistryHolder<EntityType<?>> createEntitiesRegistryHolder() {
        return new FabricRegistryHolder<>(BuiltInRegistries.ENTITY_TYPE, Static.MOD_ID);
    }

    public CreativeModeTab.Builder createTabBuilder() {
        return new PortingLibCreativeTab.PortingLibCreativeTabBuilder();
    }
}
