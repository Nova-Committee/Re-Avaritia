package committee.nova.mods.avaritia.api.init.registry;

import committee.nova.mods.avaritia.api.common.item.BaseItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author cnlimiter
 */
public class RegUtils {
    public static DeferredRegister<Block> BLOCKS;
    public static DeferredRegister<Item> ITEMS;
    public static DeferredRegister<CreativeModeTab> TABS;
    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES;
    public static DeferredRegister<EntityType<?>> ENTITIES;
    public static DeferredRegister<MenuType<?>> MENUS;
    public static DeferredRegister<Enchantment> ENCHANTMENT;
    public static DeferredRegister<RecipeType<?>> RECIPES;
    public static DeferredRegister<RecipeSerializer<?>> SERIALIZERS;
    public static final List<RegistryObject<Item>> ACCEPT_ITEM = new ArrayList<>();

    public static void init(String modid, IEventBus bus) {
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, modid);
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, modid);
        TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modid);
        BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, modid);
        MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, modid);
        ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, modid);
        ENCHANTMENT = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, modid);
        RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, modid);
        SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, modid);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TABS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        ENTITIES.register(bus);
        ENCHANTMENT.register(bus);
        RECIPES.register(bus);
        SERIALIZERS.register(bus);
    }

    public static RegistryObject<Item> item(String name) {
        return item(name, true);
    }

    public static RegistryObject<Item> item(String name, boolean exist) {
        return item(name, (e) -> new BaseItem(), exist);
    }

    public static RegistryObject<Item> item(String name, Function<String, Item> item) {
        return item(name, item, true);
    }

    public static RegistryObject<Item> item(String name, Function<String, Item> item, boolean exist) {
        return item(name, () -> item.apply(name), exist);
    }

    public static RegistryObject<Item> item(String name, Supplier<Item> item) {
        return item(name, item, true);
    }

    public static RegistryObject<Item> item(String name, Supplier<Item> item, boolean exist) {
        var regItem = ITEMS.register(name, item);
        if (exist) ACCEPT_ITEM.add(regItem);
        return regItem;
    }

    private static RegistryObject<Block> baseBlock(String name, Supplier<Block> block) {
        return BLOCKS.register(name, block);
    }

    public static RegistryObject<Block> itemBlock(String name, Supplier<Block> block) {
        return itemBlock(name, block, true);
    }

    public static RegistryObject<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem) {
        return itemBlock(name, block, hasItem, true, new Item.Properties());
    }

    public static RegistryObject<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem, boolean exist) {
        return itemBlock(name, block, hasItem, exist, new Item.Properties());
    }

    public static RegistryObject<Block> itemBlock(String name, Supplier<Block> block, Rarity rarity) {
        return itemBlock(name, block, true, true, new Item.Properties().rarity(rarity));
    }

    public static RegistryObject<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem, Item.Properties properties) {
        return itemBlock(name, block, hasItem, true, properties);
    }

    public static RegistryObject<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem, boolean exist, Item.Properties properties) {
        var reg = BLOCKS.register(name, block);
        if (hasItem) item(name, () -> new BlockItem(reg.get(), properties), exist);
        return reg;
    }

    public static RegistryObject<Block> itemBurnBlock(String name, Supplier<Block> block, boolean hasItem, Item.Properties properties, int burnTime) {
        var reg = BLOCKS.register(name, block);
        if (hasItem) item(name, () -> new BlockItem(reg.get(), properties) {
            @Override
            public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                return burnTime;
            }
        });
        return reg;
    }

    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(tile, blocks.get()).build(null));
    }

    public static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> menu(String name, IContainerFactory<T> container) {
        return MENUS.register(name, () -> IForgeMenuType.create(container));
    }

    public static RegistryObject<Enchantment> enchant(String name, Supplier<Enchantment> enchantment) {
        return ENCHANTMENT.register(name, enchantment);
    }

    public static <T extends Recipe<Container>> RegistryObject<RecipeType<T>> recipe(String name, Supplier<RecipeType<T>> type) {
        return RECIPES.register(name, type);
    }

    public static RegistryObject<RecipeSerializer<?>> serializer(String name, Supplier<RecipeSerializer<?>> serializer) {
        return SERIALIZERS.register(name, serializer);
    }
}
