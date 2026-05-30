package com.avaritia.init.data.provider;

import com.avaritia.init.registry.ModBlocks;
import com.avaritia.init.registry.ModItems;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Avaritia 物品与方块模型提供程序。
 * <p>
 * 负责在数据生成阶段输出 {@code assets/avaritia/models/} 下的模型文件：
 * 普通资源物品使用 {@code item/generated}，工具使用 {@code item/handheld}，
 * 方块物品则直接引用对应的方块模型。
 */
public class AvaritiaModelProvider implements DataProvider {
    private static final Set<DeferredItem<Item>> HANDHELD_ITEMS = Set.of(
            ModItems.infinity_sword,
            ModItems.infinity_hoe,
            ModItems.infinity_pickaxe,
            ModItems.infinity_shovel,
            ModItems.infinity_axe,
            ModItems.infinity_bucket,
            ModItems.infinity_bow,
            ModItems.infinity_crossbow,
            ModItems.infinity_shield,
            ModItems.infinity_trident,
            ModItems.infinity_mace,
            ModItems.crystal_sword,
            ModItems.crystal_hoe,
            ModItems.crystal_pickaxe,
            ModItems.crystal_shovel,
            ModItems.crystal_axe,
            ModItems.crystal_bow,
            ModItems.blaze_sword,
            ModItems.blaze_hoe,
            ModItems.blaze_pickaxe,
            ModItems.blaze_shovel,
            ModItems.blaze_axe,
            ModItems.blaze_bow
    );

    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider itemInfoPathProvider;
    private final PackOutput.PathProvider modelPathProvider;
    private final Map<Identifier, BlockStateModelDispatcher> generatedBlockStates = new LinkedHashMap<>();
    private final Map<Identifier, ClientItem> generatedClientItems = new LinkedHashMap<>();
    private final Map<Identifier, ModelInstance> generatedModels = new LinkedHashMap<>();

    /**
     * 创建 Avaritia 物品与方块模型提供程序（26.1.2 原版 API 版）。
     *
     * @param output 数据生成输出目录
     */
    public AvaritiaModelProvider(PackOutput output) {
        this.blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.itemInfoPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    /**
     * 生成并写出物品模型、方块模型、方块状态和 26.1.2 客户端物品定义。
     *
     * @param output 缓存输出
     * @return 异步写出任务
     */
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        registerModels();
        return CompletableFuture.allOf(
                DataProvider.saveAll(output, BlockStateModelDispatcher.CODEC, this.blockStatePathProvider, this.generatedBlockStates),
                DataProvider.saveAll(output, ModelInstance::get, this.modelPathProvider::json, this.generatedModels),
                DataProvider.saveAll(output, ClientItem.CODEC, this.itemInfoPathProvider, this.generatedClientItems)
        );
    }

    @Override
    public String getName() {
        return "Avaritia Models";
    }

    /**
     * 注册全部已注册物品和方块的模型。
     * <p>
     * 先为所有方块生成方块模型和方块物品模型，再遍历 {@link ModItems#ITEMS}
     * 生成剩余普通物品和工具模型，确保后续新增注册项不会漏掉模型。
     */
    protected void registerModels() {
        ModBlocks.BLOCKS.getEntries().forEach(this::block);
        ModItems.ITEMS.getEntries().forEach(this::item);
    }

    /**
     * 为方块生成简单 cube_all 方块模型，并为方块物品绑定对应方块模型。
     *
     * @param block 延迟注册方块引用
     */
    private void block(DeferredHolder<Block, ? extends Block> block) {
        Identifier id = block.getId();
        Identifier model = ModelTemplates.CUBE_ALL.create(id.withPrefix("block/"), new TextureMapping().put(TextureSlot.ALL, texture(id, "block")), this.generatedModels::put);
        this.generatedBlockStates.put(id, MultiVariantGenerator.dispatch(block.get(), new net.minecraft.client.data.models.MultiVariant(WeightedList.of(new Variant(model)))).create());

        if (ModItems.BLOCK_ITEMS.containsKey(id.getPath())) {
            clientItem(block.get().asItem(), net.minecraft.client.data.models.model.ItemModelUtils.plainModel(model));
        }
    }

    /**
     * 为普通注册物品生成模型，方块物品已由 {@link #block(DeferredHolder)} 处理。
     *
     * @param item 延迟注册物品引用
     */
    private void item(DeferredHolder<Item, ? extends Item> item) {
        Identifier id = item.getId();
        if (ModItems.BLOCK_ITEMS.containsKey(id.getPath())) {
            return;
        }

        if (HANDHELD_ITEMS.contains(item)) {
            handheldItem(item.get(), id);
        } else {
            basicItem(item.get(), id);
        }
    }

    /**
     * 为简单物品生成 {@code item/generated} 父模型。
     *
     * @param item 物品实例
     * @param id   物品资源定位符
     */
    private void basicItem(Item item, Identifier id) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(modelLocation(item), layer0(id), this.generatedModels::put);
        clientItem(item, net.minecraft.client.data.models.model.ItemModelUtils.plainModel(model));
    }

    /**
     * 为手持工具生成 {@code item/handheld} 父模型。
     *
     * @param item 物品实例
     * @param id   物品资源定位符
     */
    private void handheldItem(Item item, Identifier id) {
        Identifier model = ModelTemplates.FLAT_HANDHELD_ITEM.create(modelLocation(item), layer0(id), this.generatedModels::put);
        clientItem(item, net.minecraft.client.data.models.model.ItemModelUtils.plainModel(model));
    }

    /**
     * 记录 26.1.2 客户端物品定义。
     *
     * @param item  物品实例
     * @param model 未烘焙物品模型
     */
    private void clientItem(Item item, ItemModel.Unbaked model) {
        this.generatedClientItems.put(BuiltInRegistries.ITEM.getKey(item), new ClientItem(model, ClientItem.Properties.DEFAULT));
    }

    /**
     * 获取物品模型输出位置。
     *
     * @param item 物品实例
     * @return {@code <namespace>:item/<path>} 模型定位符
     */
    private Identifier modelLocation(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).withPrefix("item/");
    }

    /**
     * 创建使用单层物品纹理的贴图映射。
     *
     * @param id 物品资源定位符
     * @return 绑定 {@code layer0} 的贴图映射
     */
    private TextureMapping layer0(Identifier id) {
        return new TextureMapping().put(TextureSlot.LAYER0, texture(id, "item"));
    }

    /**
     * 获取模型纹理路径。
     *
     * @param id     注册资源定位符
     * @param folder 纹理目录，如 {@code item} 或 {@code block}
     * @return 指向 {@code avaritia:<folder>/<path>} 的纹理定位符
     */
    private Material texture(Identifier id, String folder) {
        return new Material(Identifier.fromNamespaceAndPath(id.getNamespace(), folder + "/" + id.getPath()));
    }
}
