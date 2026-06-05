package com.avaritia.init.data.provider;

import com.avaritia.Const;
import com.avaritia.client.model.loader.base.AvaritiaItemModels;
import com.avaritia.init.registry.ModItems;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
    private static final Map<String, String> ITEM_TEXTURES = Map.ofEntries(
            Map.entry("neutron_ring", "item/misc/neutron_ring"),
            Map.entry("infinity_totem", "item/misc/infinity_totem"),
            Map.entry("infinity_ring", "item/misc/infinity_ring"),
            Map.entry("infinity_umbrella", "item/misc/infinity_umbrella"),
            Map.entry("infinity_clock", "item/misc/infinity_clock"),
            Map.entry("side_config_card", "item/misc/side_config_card"),
            Map.entry("infinity_sword", "item/tools/infinity_sword/layer_0"),
            Map.entry("infinity_hoe", "item/tools/infinity_hoe/layer_0"),
            Map.entry("infinity_pickaxe", "item/tools/infinity_pickaxe/layer_0"),
            Map.entry("infinity_shovel", "item/tools/infinity_shovel/layer_0"),
            Map.entry("infinity_axe", "item/tools/infinity_axe/layer_0"),
            Map.entry("infinity_bucket", "item/misc/infinity_bucket"),
            Map.entry("infinity_bow", "item/tools/infinity_bow/idle"),
            Map.entry("infinity_crossbow", "item/tools/infinity_crossbow/idle"),
            Map.entry("infinity_shield", "item/tools/infinity_shield/layer_0"),
            Map.entry("infinity_trident", "item/tools/infinity_trident/layer_0"),
            Map.entry("infinity_mace", "item/tools/infinity_mace/layer_0"),
            Map.entry("crystal_sword", "item/tools/crystal_sword/layer_0"),
            Map.entry("crystal_hoe", "item/tools/crystal_hoe/layer_0"),
            Map.entry("crystal_pickaxe", "item/tools/crystal_pickaxe/layer_0"),
            Map.entry("crystal_shovel", "item/tools/crystal_shovel/layer_0"),
            Map.entry("crystal_axe", "item/tools/crystal_axe/layer_0"),
            Map.entry("crystal_bow", "item/tools/crystal_bow/crystal_bow"),
            Map.entry("blaze_sword", "item/tools/blaze_sword/layer_0"),
            Map.entry("blaze_hoe", "item/tools/blaze_hoe/layer_0"),
            Map.entry("blaze_pickaxe", "item/tools/blaze_pickaxe/layer_0"),
            Map.entry("blaze_shovel", "item/tools/blaze_shovel/layer_0"),
            Map.entry("blaze_axe", "item/tools/blaze_axe/layer_0"),
            Map.entry("blaze_bow", "item/tools/blaze_bow/blaze_bow"),
            Map.entry("infinity_helmet", "item/armor/helmet/layer_0"),
            Map.entry("infinity_chestplate", "item/armor/chestplate/layer_0"),
            Map.entry("infinity_pants", "item/armor/legs/layer_0"),
            Map.entry("infinity_boots", "item/armor/boots/layer_0"),
            Map.entry("neutron_horse_armor", "item/armor/horse/neutron_horse_armor_item"),
            Map.entry("infinity_elytra", "item/armor/elytra/infinity_elytra"),
            Map.entry("blaze_cube", "item/resource/blaze/blaze_cube"),
            Map.entry("diamond_lattice", "item/resource/crystal/diamond_lattice"),
            Map.entry("crystal_matrix_ingot", "item/resource/crystal/crystal_matrix_ingot"),
            Map.entry("neutron_pile", "item/resource/neutron/neutron_pile"),
            Map.entry("neutron_nugget", "item/resource/neutron/neutron_nugget"),
            Map.entry("neutron_ingot", "item/resource/neutron/neutron_ingot"),
            Map.entry("neutron_gear", "item/resource/neutron/neutron_gear"),
            Map.entry("infinity_nugget", "item/resource/infinity/infinity_nugget"),
            Map.entry("infinity_catalyst", "item/resource/infinity/infinity_catalyst"),
            Map.entry("infinity_ingot", "item/resource/infinity/infinity_ingot"),
            Map.entry("singularity", "item/resource/singularity/singularity"),
            Map.entry("eternal_singularity", "item/resource/singularity/eternal_singularity"),
            Map.entry("record_fragment", "item/resource/record_fragment"),
            Map.entry("star_fuel", "item/resource/fuel/star_fuel"),
            Map.entry("refined_coal", "item/resource/fuel/refined_coal"),
            Map.entry("endest_pearl", "item/misc/endest_pearl/layer_0"),
            Map.entry("matter_cluster", "item/misc/matter_cluster/empty"),
            Map.entry("full_matter_cluster", "item/misc/matter_cluster/full_matter_cluster"),
            Map.entry("enhancement_core", "item/misc/enhancement_core"),
            Map.entry("upgrade_smithing_template", "item/misc/upgrade_smithing_template"),
            Map.entry("infinity_upgrade", "item/misc/infinity_upgrade"),
            Map.entry("ultimate_stew", "item/foods/ultimate_stew/layer_0"),
            Map.entry("cosmic_meatballs", "item/foods/cosmic_meatballs/layer_0"),
            Map.entry("forge_energy", "item/misc/forge_energy")
    );

    private final PackOutput.PathProvider itemInfoPathProvider;
    private final PackOutput.PathProvider modelPathProvider;
    private final Map<Identifier, ClientItem> generatedClientItems = new LinkedHashMap<>();
    private final Map<Identifier, ModelInstance> generatedModels = new LinkedHashMap<>();

    public AvaritiaModelProvider(PackOutput output) {
        this.itemInfoPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        registerModels();
        return CompletableFuture.allOf(
                DataProvider.saveAll(output, ModelInstance::get, this.modelPathProvider::json, this.generatedModels),
                DataProvider.saveAll(output, ClientItem.CODEC, this.itemInfoPathProvider, this.generatedClientItems)
        );
    }

    @Override
    public String getName() {
        return "Avaritia Item Models";
    }

    protected void registerModels() {
        ModItems.ITEMS.getEntries().forEach(this::item);
    }

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

    private void basicItem(Item item, Identifier id) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(modelLocation(item), layer0(id), this.generatedModels::put);
        clientItem(item, clientItemModel(id, model));
    }

    private void handheldItem(Item item, Identifier id) {
        Identifier model = ModelTemplates.FLAT_HANDHELD_ITEM.create(modelLocation(item), layer0(id), this.generatedModels::put);
        clientItem(item, clientItemModel(id, model));
    }

    private ItemModel.Unbaked clientItemModel(Identifier id, Identifier model) {
        return switch (id.getPath()) {
            case "infinity_sword" -> new AvaritiaItemModels.Cosmic(model, List.of(mask("infinity_sword_mask")));
            case "infinity_helmet" -> new AvaritiaItemModels.Cosmic(model, List.of(mask("infinity_helmet_mask")));
            case "infinity_chestplate" -> new AvaritiaItemModels.Cosmic(model, List.of(mask("infinity_chestplate_mask")));
            case "infinity_pants" -> new AvaritiaItemModels.Cosmic(model, List.of(mask("infinity_pants_mask")));
            case "infinity_boots" -> new AvaritiaItemModels.Cosmic(model, List.of(mask("infinity_boots_mask")));
            case "infinity_trident" -> new AvaritiaItemModels.CosmicArc(model, List.of(mask("infinity_trident_mask")));
            case "eternal_singularity" -> new AvaritiaItemModels.HaloCosmic(model, List.of(mask("eternal_singularity_mask")), halo(), -16777216, 6, false);
            case "infinity_ingot", "infinity_nugget", "infinity_catalyst", "infinity_totem", "infinity_ring",
                 "infinity_bucket", "infinity_elytra", "infinity_upgrade", "enhancement_core", "endest_pearl" ->
                    new AvaritiaItemModels.Halo(model, halo(), -16777216, 10, true);
            default -> ItemModelUtils.plainModel(model);
        };
    }

    private void clientItem(Item item, ItemModel.Unbaked model) {
        this.generatedClientItems.put(BuiltInRegistries.ITEM.getKey(item), new ClientItem(model, ClientItem.Properties.DEFAULT));
    }

    private Identifier modelLocation(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).withPrefix("item/");
    }

    private TextureMapping layer0(Identifier id) {
        return new TextureMapping().put(TextureSlot.LAYER0, texture(itemTexture(id)));
    }

    private Identifier itemTexture(Identifier id) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), ITEM_TEXTURES.getOrDefault(id.getPath(), "item/" + id.getPath()));
    }

    private Identifier mask(String path) {
        return Identifier.fromNamespaceAndPath(Const.MOD_ID, "mask/item/" + path);
    }

    private Identifier halo() {
        return Identifier.fromNamespaceAndPath(Const.MOD_ID, "misc/halo");
    }

    private Material texture(Identifier id) {
        return new Material(id);
    }
}
