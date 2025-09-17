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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
 * @author: cnlimiter
 */
public class RegUtils {
    public final DeferredRegister<Block> BLOCKS;
    public final DeferredRegister<Item> ITEMS;
    public final DeferredRegister<CreativeModeTab> TABS;
    public final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES;
    public final DeferredRegister<EntityType<?>> ENTITIES;
    public final DeferredRegister<MenuType<?>> MENUS;
    public final DeferredRegister<Enchantment> ENCHANTMENT;
    public final DeferredRegister<RecipeType<?>> RECIPES;
    public final DeferredRegister<RecipeSerializer<?>> SERIALIZERS;
    public final List<RegistryObject<Item>> ACCEPT_ITEM = new ArrayList<>();

    public RegUtils(String modid){
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, modid);
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, modid);
        TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modid);
        BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, modid);
        MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, modid);
        ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, modid);
        ENCHANTMENT = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, modid);
        RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, modid);
        SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, modid);

    }

    public void init() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
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

    public RegistryObject<Item> item(String name) {
        return item(name, true);
    }

    public RegistryObject<Item> item(String name, boolean exist) {
        return item(name, (e) -> new BaseItem(), exist);
    }

    public RegistryObject<Item> item(String name, Function<String, Item> item) {
        return item(name, item, true);
    }

    public RegistryObject<Item> item(String name, Function<String, Item> item, boolean exist) {
        return item(name, () -> item.apply(name), exist);
    }

    public RegistryObject<Item> item(String name, Supplier<Item> item) {
        return item(name, item, true);
    }

    public RegistryObject<Item> item(String name, Supplier<Item> item, boolean exist) {
        var regItem = ITEMS.register(name, item);
        if (exist) ACCEPT_ITEM.add(regItem);
        return regItem;
    }

    private RegistryObject<Block> baseBlock(String name, Supplier<Block> block) {
        return BLOCKS.register(name, block);
    }

    public RegistryObject<Block> itemBlock(String name, Supplier<Block> block) {
        return itemBlock(name, block, true);
    }

    public RegistryObject<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem) {
        return itemBlock(name, block, hasItem, true, new Item.Properties());
    }

    public RegistryObject<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem, boolean exist) {
        return itemBlock(name, block, hasItem, exist, new Item.Properties());
    }

    public RegistryObject<Block> itemBlock(String name, Supplier<Block> block, Rarity rarity) {
        return itemBlock(name, block, true, true, new Item.Properties().rarity(rarity));
    }

    public RegistryObject<Block> itemBlock(String name, Supplier<Block> block,  boolean hasItem, Item.Properties properties) {
        return itemBlock(name, block, hasItem, true, properties);
    }

    public RegistryObject<Block> itemBlock(String name, Supplier<Block> block, boolean hasItem, boolean exist, Item.Properties properties) {
        var reg = BLOCKS.register(name, block);
        if (hasItem) item(name, () -> new BlockItem(reg.get(), properties), exist);
        return reg;
    }

    public RegistryObject<Block> itemBurnBlock(String name, Supplier<Block> block, boolean hasItem, Item.Properties properties, int burnTime) {
        var reg = BLOCKS.register(name, block);
        if (hasItem) item(name, () -> new BlockItem(reg.get(), properties){
            @Override
            public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                return burnTime;
            }
        });
        return reg;
    }

    public <T extends BlockEntity> RegistryObject<BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(tile, blocks.get()).build(null));
    }

    public <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> menu(String name, IContainerFactory<T> container) {
        return MENUS.register(name, () -> IForgeMenuType.create(container));
    }

    public RegistryObject<Enchantment> enchant(String name, Supplier<Enchantment> enchantment) {
        return ENCHANTMENT.register(name, enchantment);
    }

    public <T extends Recipe<Container>> RegistryObject<RecipeType<T>> recipe(String name, Supplier<RecipeType<T>> type) {
        return RECIPES.register(name, type);
    }

    public RegistryObject<RecipeSerializer<?>> serializer(String name, Supplier<RecipeSerializer<?>> serializer) {
        return SERIALIZERS.register(name, serializer);
    }
}
