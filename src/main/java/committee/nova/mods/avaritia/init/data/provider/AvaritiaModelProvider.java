package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.client.model.loader.AvaritiaItemModelLoaders;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.iface.IColored;
import committee.nova.mods.avaritia.client.render.item.InfinityShieldRender;
import committee.nova.mods.avaritia.client.tint.RainbowTintSource;
import committee.nova.mods.avaritia.init.handler.ItemOverrideHandler;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.color.item.ItemTintSource;
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
            ModItems.infinity_umbrella,
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
            Map.entry("infinity_crossbow", "item/tools/infinity_crossbow/standby"),
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
        Identifier model = switch (id.getPath()) {
            case "singularity" -> twoLayerItem(item, id, "singularity_overlay");
            case "eternal_singularity" -> twoLayerItem(item, id, "eternal_singularity2");
            case "infinity_trident" -> tridentModel("infinity_trident", "item/tools/infinity_trident/layer_0");
            default -> ModelTemplates.FLAT_ITEM.create(modelLocation(item), layer0(id), this.generatedModels::put);
        };
        clientItem(item, clientItemModel(id, model));
    }

    private void handheldItem(Item item, Identifier id) {
        Identifier model = switch (id.getPath()) {
            case "infinity_sword" -> layeredHandheldModel("infinity_sword",
                    "item/tools/infinity_sword/layer_0",
                    "item/tools/infinity_sword/layer_1");
            case "infinity_crossbow" -> crossbowModel("infinity_crossbow", "item/tools/infinity_crossbow/standby");
            case "infinity_shield" -> shieldModel("infinity_shield", false);
            case "blaze_bow", "crystal_bow" -> bowModel(id.getPath(), itemTexture(id).getPath());
            default -> ModelTemplates.FLAT_HANDHELD_ITEM.create(modelLocation(item), layer0(id), this.generatedModels::put);
        };
        clientItem(item, clientItemModel(id, model));
    }

    private ItemModel.Unbaked clientItemModel(Identifier id, Identifier model) {
        // 这里决定每个物品最终写入 items/*.json 的运行时模型；星空/halo 都走 AvaritiaItemModelLoaders。
        return switch (id.getPath()) {
            case "infinity_sword" -> infinitySwordModel(model);
            case "infinity_bow" -> infinityBowModel();
            case "infinity_crossbow" -> infinityCrossbowModel(model);
            case "blaze_bow", "crystal_bow" -> simpleBowModel(id.getPath(), model);
            case "infinity_pickaxe" -> modeModel("infinity_pickaxe_hammer", handheldModel("infinity_pickaxe/hammer", "item/tools/infinity_pickaxe/hammer"), model);
            case "infinity_shovel" -> modeModel("infinity_shovel_destroyer", handheldModel("infinity_shovel/destroyer", "item/tools/infinity_shovel/destroyer"), model);
            case "infinity_shield" -> infinityShieldModel(model);
            case "infinity_umbrella" -> infinityUmbrellaModel(model);
            case "infinity_clock" -> infinityClockModel(model);
            case "infinity_trident" -> new AvaritiaItemModelLoaders.CosmicArc(model, List.of(mask("infinity_trident_mask")));
            case "infinity_helmet", "infinity_chestplate", "infinity_pants", "infinity_boots" ->
                    new AvaritiaItemModelLoaders.Cosmic(model, List.of(mask(id.getPath() + "_mask")));
            case "singularity" -> new AvaritiaItemModelLoaders.Halo(model, halo(), -16777216, 4, false, singularityTints());
            case "eternal_singularity" -> new AvaritiaItemModelLoaders.HaloCosmic(model, List.of(mask("eternal_singularity_mask")), halo(), -16777216, 6, false, rainbowTints());
            case "matter_cluster" -> ItemModelUtils.conditional(new ItemOverrideHandler.MatterClusterFull(),
                    matterClusterFullModel(), new AvaritiaItemModelLoaders.Cosmic(model, List.of(mask("matter_cluster_empty_mask"))));
            case "full_matter_cluster" -> new AvaritiaItemModelLoaders.HaloCosmic(model, List.of(mask("matter_cluster_full_mask")), halo(), -16777216, 10, false);
            case "infinity_ingot", "infinity_nugget" -> haloModel(model, 10, true);
            case "infinity_catalyst", "enhancement_core" -> haloModel(model, 8, true);
            case "infinity_totem", "infinity_ring", "infinity_bucket", "infinity_elytra", "neutron_ring", "star_fuel" ->
                    haloModel(model, 6, false);
            case "infinity_upgrade" -> haloModel(model, 4, false);
            case "endest_pearl" -> haloModel(model, 4, true);
            case "neutron_pile" -> haloNoiseModel(model, 872415231);
            case "neutron_nugget" -> haloNoiseModel(model, 1308622847);
            case "neutron_ingot", "neutron_gear", "neutron_horse_armor", "upgrade_smithing_template" ->
                    haloNoiseModel(model, -1711276033);
            default -> ItemModelUtils.plainModel(model);
        };
    }

    private ItemModel.Unbaked infinitySwordModel(Identifier model) {
        Identifier killModel = layeredHandheldModel("infinity_sword/kill",
                "item/tools/infinity_sword/layer_0",
                "item/tools/infinity_sword/layer_1");
        return ItemModelUtils.conditional(new ItemOverrideHandler.ModeFlag("infinity_sword_kill"),
                new AvaritiaItemModelLoaders.Hell(killModel, List.of(mask("infinity_sword_mask"))),
                new AvaritiaItemModelLoaders.Cosmic(model, List.of(mask("infinity_sword_mask"))));
    }

    private ItemModel.Unbaked infinityBowModel() {
        ItemModel.Unbaked idle = cosmicHandheld("infinity_bow/idle", "item/tools/infinity_bow/idle", "infinity_bow/idle_mask");
        ItemModel.Unbaked pull0 = cosmicHandheld("infinity_bow/pull_0", "item/tools/infinity_bow/pull_0", "infinity_bow/pull_0_mask");
        ItemModel.Unbaked pull1 = cosmicHandheld("infinity_bow/pull_1", "item/tools/infinity_bow/pull_1", "infinity_bow/pull_1_mask");
        ItemModel.Unbaked pull2 = cosmicHandheld("infinity_bow/pull_2", "item/tools/infinity_bow/pull_2", "infinity_bow/pull_2_mask");
        ItemModel.Unbaked normal = ItemModelUtils.rangeSelect(new ItemOverrideHandler.BowPull(), idle,
                ItemModelUtils.override(pull0, 0.05F),
                ItemModelUtils.override(pull1, 0.65F),
                ItemModelUtils.override(pull2, 0.9F));

        ItemModel.Unbaked tracerIdle = hellHandheld("infinity_bow/tracer_idle", "item/tools/infinity_bow/tracer/idle", "infinity_bow/idle_mask");
        ItemModel.Unbaked tracerPull0 = hellHandheld("infinity_bow/tracer_pull_0", "item/tools/infinity_bow/tracer/pull_0", "infinity_bow/pull_0_mask");
        ItemModel.Unbaked tracerPull1 = hellHandheld("infinity_bow/tracer_pull_1", "item/tools/infinity_bow/tracer/pull_1", "infinity_bow/pull_1_mask");
        ItemModel.Unbaked tracerPull2 = hellHandheld("infinity_bow/tracer_pull_2", "item/tools/infinity_bow/tracer/pull_2", "infinity_bow/pull_2_mask");
        ItemModel.Unbaked tracer = ItemModelUtils.rangeSelect(new ItemOverrideHandler.BowPull(), tracerIdle,
                ItemModelUtils.override(tracerPull0, 0.05F),
                ItemModelUtils.override(tracerPull1, 0.65F),
                ItemModelUtils.override(tracerPull2, 0.9F));

        return ItemModelUtils.conditional(new ItemOverrideHandler.ModeFlag("infinity_bow_tracer"), tracer, normal);
    }

    private ItemModel.Unbaked infinityCrossbowModel(Identifier idleModel) {
        ItemModel.Unbaked idle = new AvaritiaItemModelLoaders.Cosmic(idleModel, List.of(mask("infinity_crossbow/standby_mask")));
        ItemModel.Unbaked pull0 = cosmicCrossbow("infinity_crossbow/pull_0", "item/tools/infinity_crossbow/pull_0", "infinity_crossbow/pull_0_mask");
        ItemModel.Unbaked pull1 = cosmicCrossbow("infinity_crossbow/pull_1", "item/tools/infinity_crossbow/pull_1", "infinity_crossbow/pull_1_mask");
        ItemModel.Unbaked pull2 = cosmicCrossbow("infinity_crossbow/pull_2", "item/tools/infinity_crossbow/pull_2", "infinity_crossbow/pull_2_mask");
        ItemModel.Unbaked pulling = ItemModelUtils.rangeSelect(new ItemOverrideHandler.InfinityCrossbowPull(), idle,
                ItemModelUtils.override(pull0, 0.05F),
                ItemModelUtils.override(pull1, 0.58F),
                ItemModelUtils.override(pull2, 1.0F));
        ItemModel.Unbaked charged = cosmicCrossbow("infinity_crossbow/standby", "item/tools/infinity_crossbow/standby", "infinity_crossbow/standby_mask");
        return ItemModelUtils.conditional(new ItemOverrideHandler.InfinityCrossbowCharged(), charged, pulling);
    }

    private ItemModel.Unbaked simpleBowModel(String itemName, Identifier idleModel) {
        ItemModel.Unbaked pull0 = ItemModelUtils.plainModel(bowModel(itemName + "_pulling_0", "item/tools/" + itemName + "/" + itemName + "_pulling_0"));
        ItemModel.Unbaked pull1 = ItemModelUtils.plainModel(bowModel(itemName + "_pulling_1", "item/tools/" + itemName + "/" + itemName + "_pulling_1"));
        ItemModel.Unbaked pull2 = ItemModelUtils.plainModel(bowModel(itemName + "_pulling_2", "item/tools/" + itemName + "/" + itemName + "_pulling_2"));
        ItemModel.Unbaked pulling = ItemModelUtils.rangeSelect(new ItemOverrideHandler.BowPull(), pull0,
                ItemModelUtils.override(pull1, 0.65F),
                ItemModelUtils.override(pull2, 0.9F));
        return ItemModelUtils.conditional(new ItemOverrideHandler.UsingStack(), pulling, ItemModelUtils.plainModel(idleModel));
    }

    private ItemModel.Unbaked infinityClockModel(Identifier model) {
        Identifier upModel = flatModel("infinity_clock_up", "item/misc/infinity_clock_up", false);
        return ItemModelUtils.conditional(new ItemOverrideHandler.ModeFlag("infinity_clock_up"),
                new AvaritiaItemModelLoaders.Halo(upModel, halo(), -16777216, 6, true),
                ItemModelUtils.plainModel(model));
    }

    private ItemModel.Unbaked infinityUmbrellaModel(Identifier model) {
        ItemModel.Unbaked normal = new AvaritiaItemModelLoaders.Halo(model, halo(), -16777216, 6, false);
        ItemModel.Unbaked sun = new AvaritiaItemModelLoaders.Halo(
                handheldModel("infinity_umbrella_sun", "item/misc/infinity_umbrella_sun"),
                halo(), -16777216, 6, false);
        ItemModel.Unbaked rain = new AvaritiaItemModelLoaders.Halo(
                handheldModel("infinity_umbrella_rain", "item/misc/infinity_umbrella_rain"),
                halo(), -16777216, 6, false);
        ItemModel.Unbaked storm = new AvaritiaItemModelLoaders.Halo(
                handheldModel("infinity_umbrella_storm", "item/misc/infinity_umbrella_storm"),
                halo(), -16777216, 6, false);
        return ItemModelUtils.rangeSelect(new ItemOverrideHandler.UmbrellaMode(), normal,
                ItemModelUtils.override(sun, 1.0F),
                ItemModelUtils.override(rain, 2.0F),
                ItemModelUtils.override(storm, 3.0F));
    }

    private ItemModel.Unbaked infinityShieldModel(Identifier model) {
        Identifier blockingModel = shieldModel("infinity_shield_blocking", true);
        return ItemModelUtils.conditional(ItemModelUtils.isUsingItem(),
                ItemModelUtils.specialModel(blockingModel, new InfinityShieldRender.Unbaked()),
                ItemModelUtils.specialModel(model, new InfinityShieldRender.Unbaked()));
    }

    private ItemModel.Unbaked matterClusterFullModel() {
        Identifier model = flatModel("matter_cluster/full", "item/misc/matter_cluster/full_matter_cluster", false);
        return new AvaritiaItemModelLoaders.HaloCosmic(model, List.of(mask("matter_cluster_full_mask")), halo(), -16777216, 10, false);
    }

    private ItemModel.Unbaked haloModel(Identifier model, int size, boolean pulse) {
        return new AvaritiaItemModelLoaders.Halo(model, halo(), -16777216, size, pulse);
    }

    private ItemModel.Unbaked haloNoiseModel(Identifier model, int color) {
        return new AvaritiaItemModelLoaders.Halo(model, haloNoise(), color, 6, false);
    }

    private ItemModel.Unbaked modeModel(String modeKey, Identifier activeModel, Identifier fallbackModel) {
        return ItemModelUtils.conditional(new ItemOverrideHandler.ModeFlag(modeKey),
                ItemModelUtils.plainModel(activeModel),
                ItemModelUtils.plainModel(fallbackModel));
    }

    private ItemModel.Unbaked cosmicHandheld(String modelPath, String texturePath, String maskPath) {
        return new AvaritiaItemModelLoaders.Cosmic(flatModel(modelPath, texturePath, true), List.of(mask(maskPath)));
    }

    private ItemModel.Unbaked cosmicCrossbow(String modelPath, String texturePath, String maskPath) {
        return new AvaritiaItemModelLoaders.Cosmic(crossbowModel(modelPath, texturePath), List.of(mask(maskPath)));
    }

    private ItemModel.Unbaked hellHandheld(String modelPath, String texturePath, String maskPath) {
        return new AvaritiaItemModelLoaders.Hell(flatModel(modelPath, texturePath, true), List.of(mask(maskPath)));
    }

    private Identifier handheldModel(String modelPath, String texturePath) {
        return flatModel(modelPath, texturePath, true);
    }

    private Identifier layeredHandheldModel(String modelPath, String layer0, String layer1) {
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.LAYER0, texture(Identifier.fromNamespaceAndPath(Const.MOD_ID, layer0)))
                .put(TextureSlot.LAYER1, texture(Identifier.fromNamespaceAndPath(Const.MOD_ID, layer1)));
        Identifier model = Identifier.fromNamespaceAndPath(Const.MOD_ID, "item/" + modelPath);
        return ModelTemplates.createItem("handheld", TextureSlot.LAYER0, TextureSlot.LAYER1)
                .create(model, textures, this.generatedModels::put);
    }

    private Identifier flatModel(String modelPath, String texturePath, boolean handheld) {
        TextureMapping textures = new TextureMapping().put(TextureSlot.LAYER0, texture(Identifier.fromNamespaceAndPath(Const.MOD_ID, texturePath)));
        Identifier model = Identifier.fromNamespaceAndPath(Const.MOD_ID, "item/" + modelPath);
        return (handheld ? ModelTemplates.FLAT_HANDHELD_ITEM : ModelTemplates.FLAT_ITEM).create(model, textures, this.generatedModels::put);
    }

    private Identifier bowModel(String modelPath, String texturePath) {
        TextureMapping textures = new TextureMapping().put(TextureSlot.LAYER0, texture(Identifier.fromNamespaceAndPath(Const.MOD_ID, texturePath)));
        Identifier model = Identifier.fromNamespaceAndPath(Const.MOD_ID, "item/" + modelPath);
        return ModelTemplates.BOW.create(model, textures, this.generatedModels::put);
    }

    private Identifier crossbowModel(String modelPath, String texturePath) {
        Identifier model = Identifier.fromNamespaceAndPath(Const.MOD_ID, "item/" + modelPath);
        String parent = "infinity_crossbow".equals(modelPath) ? "minecraft:item/generated" : Const.MOD_ID + ":item/infinity_crossbow";
        this.generatedModels.put(model, () -> crossbowModelJson(parent, texturePath));
        return model;
    }

    private JsonObject crossbowModelJson(String parent, String texturePath) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", parent);

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", Const.MOD_ID + ":" + texturePath);
        root.add("textures", textures);

        if ("minecraft:item/generated".equals(parent)) {
            JsonObject display = new JsonObject();
            display.add("thirdperson_righthand", transform(new double[]{-90, 0, -60}, new double[]{2, 0.1, -3}, new double[]{0.9, 0.9, 0.9}));
            display.add("thirdperson_lefthand", transform(new double[]{-90, 0, 30}, new double[]{2, 0.1, -3}, new double[]{0.9, 0.9, 0.9}));
            display.add("firstperson_righthand", transform(new double[]{-90, 0, -55}, new double[]{1.13, 3.2, 1.13}, new double[]{0.68, 0.68, 0.68}));
            display.add("firstperson_lefthand", transform(new double[]{-90, 0, 35}, new double[]{1.13, 3.2, 1.13}, new double[]{0.68, 0.68, 0.68}));
            root.add("display", display);
        }
        return root;
    }

    private Identifier tridentModel(String modelPath, String texturePath) {
        Identifier model = Identifier.fromNamespaceAndPath(Const.MOD_ID, "item/" + modelPath);
        this.generatedModels.put(model, () -> tridentModelJson(texturePath));
        return model;
    }

    private JsonObject tridentModelJson(String texturePath) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", Const.MOD_ID + ":" + texturePath);
        root.add("textures", textures);

        JsonObject display = new JsonObject();
        display.add("ground", transform(new double[]{0, 0, 0}, new double[]{4, 4, 2}, new double[]{0.5, 0.5, 0.5}));
        display.add("fixed", transform(new double[]{0, 180, 0}, new double[]{0, 0, 0}, new double[]{1, 1, 1}));
        display.add("gui", transform(new double[]{0, 0, 0}, new double[]{0, 0, 0}, new double[]{1, 1, 1}));
        display.add("thirdperson_righthand", transform(new double[]{0, 60, 0}, new double[]{11, 17, -2}, new double[]{1, 1, 1}));
        display.add("thirdperson_lefthand", transform(new double[]{0, 60, 0}, new double[]{-11, 17, 4}, new double[]{1, 1, 1}));
        display.add("firstperson_righthand", transform(new double[]{0, -90, 25}, new double[]{-3, 17, 1}, new double[]{1, 1, 1}));
        display.add("firstperson_lefthand", transform(new double[]{0, 90, -25}, new double[]{-15, 17, 1}, new double[]{1, 1, 1}));
        root.add("display", display);
        return root;
    }

    private Identifier shieldModel(String modelPath, boolean blocking) {
        Identifier model = Identifier.fromNamespaceAndPath(Const.MOD_ID, "item/" + modelPath);
        this.generatedModels.put(model, () -> shieldModelJson(blocking));
        return model;
    }

    private JsonObject shieldModelJson(boolean blocking) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        root.addProperty("gui_light", "front");

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "avaritia:item/tools/infinity_shield/layer_0");
        textures.addProperty("particle", "avaritia:block/resource/infinity");
        root.add("textures", textures);

        JsonObject display = new JsonObject();
        if (blocking) {
            display.add("thirdperson_righthand", transform(new double[]{45, 135, 0}, new double[]{3.51, 11, -2}, new double[]{1, 1, 1}));
            display.add("thirdperson_lefthand", transform(new double[]{45, 135, 0}, new double[]{13.51, 3, 5}, new double[]{1, 1, 1}));
            display.add("firstperson_righthand", transform(new double[]{0, 180, -5}, new double[]{-15, 5, -11}, new double[]{1.25, 1.25, 1.25}));
            display.add("firstperson_lefthand", transform(new double[]{0, 180, -5}, new double[]{5, 5, -11}, new double[]{1.25, 1.25, 1.25}));
            display.add("gui", transform(new double[]{15, -25, -5}, new double[]{2, 3, 0}, new double[]{0.65, 0.65, 0.65}));
        } else {
            display.add("thirdperson_righthand", transform(new double[]{0, 90, 0}, new double[]{10, 6, -4}, new double[]{1, 1, 1}));
            display.add("thirdperson_lefthand", transform(new double[]{0, 90, 0}, new double[]{10, 6, 12}, new double[]{1, 1, 1}));
            display.add("firstperson_righthand", transform(new double[]{0, 180, 5}, new double[]{-10, 2, -10}, new double[]{1.25, 1.25, 1.25}));
            display.add("firstperson_lefthand", transform(new double[]{0, 180, 5}, new double[]{10, 0, -10}, new double[]{1.25, 1.25, 1.25}));
            display.add("gui", transform(new double[]{15, -25, -5}, new double[]{2, 3, 0}, new double[]{0.65, 0.65, 0.65}));
            display.add("fixed", transform(new double[]{0, 180, 0}, new double[]{-4.5, 4.5, -5}, new double[]{0.55, 0.55, 0.55}));
            display.add("ground", transform(new double[]{0, 0, 0}, new double[]{2, 4, 2}, new double[]{0.25, 0.25, 0.25}));
        }
        root.add("display", display);
        return root;
    }

    private JsonObject transform(double[] rotation, double[] translation, double[] scale) {
        JsonObject transform = new JsonObject();
        transform.add("rotation", array(rotation));
        transform.add("translation", array(translation));
        transform.add("scale", array(scale));
        return transform;
    }

    private JsonArray array(double[] values) {
        JsonArray array = new JsonArray();
        for (double value : values) {
            array.add(value);
        }
        return array;
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

    private Identifier twoLayerItem(Item item, Identifier id, String overlayPath) {
        TextureMapping textures = layer0(id).put(TextureSlot.LAYER1, texture(itemTexture(id, overlayPath)));
        return ModelTemplates.TWO_LAYERED_ITEM.create(modelLocation(item), textures, this.generatedModels::put);
    }

    private Identifier itemTexture(Identifier id) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), ITEM_TEXTURES.getOrDefault(id.getPath(), "item/" + id.getPath()));
    }

    private Identifier itemTexture(Identifier id, String path) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), "item/resource/singularity/" + path);
    }

    private Identifier mask(String path) {
        return Identifier.fromNamespaceAndPath(Const.MOD_ID, "mask/item/" + path);
    }

    private Identifier halo() {
        return Identifier.fromNamespaceAndPath(Const.MOD_ID, "misc/halo");
    }

    private Identifier haloNoise() {
        return Identifier.fromNamespaceAndPath(Const.MOD_ID, "misc/halo_noise");
    }

    private List<ItemTintSource> singularityTints() {
        return List.of(new IColored.ItemColors(0), new IColored.ItemColors(1));
    }

    private List<ItemTintSource> rainbowTints() {
        return List.of(new RainbowTintSource(), new RainbowTintSource());
    }

    private Material texture(Identifier id) {
        return new Material(id);
    }
}
