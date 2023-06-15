package nova.committee.avaritia.init.handler;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import nova.committee.avaritia.api.client.render.CustomRenderedItemModel;
import nova.committee.avaritia.api.client.render.CustomRenderedItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/6/4 12:36
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelsHandler {
    //protected CustomBlockModels customBlockModels = new CustomBlockModels();
    //protected CustomItemModels customItemModels = new CustomItemModels();
    protected static CustomRenderedItems customRenderedItems = new CustomRenderedItems();

//    public CustomBlockModels getCustomBlockModels() {
//        return customBlockModels;
//    }
//
//    public CustomItemModels getCustomItemModels() {
//        return customItemModels;
//    }

    public static CustomRenderedItems getCustomRenderedItems() {
        return customRenderedItems;
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelEvent event) {
        customRenderedItems.forEach((item, modelFunc) -> modelFunc.apply(null)
                .getModelLocations()
                .forEach(ForgeModelBakery::addSpecialModel));
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModelRegistry();

        //customBlockModels.forEach((block, modelFunc) -> swapModels(modelRegistry, getAllBlockStateModelLocations(block), modelFunc));
        //customItemModels.forEach((item, modelFunc) -> swapModels(modelRegistry, getItemModelLocation(item), modelFunc));
        customRenderedItems.forEach((item, modelFunc) -> {
            swapModels(modelRegistry, getItemModelLocation(item), m -> {
                CustomRenderedItemModel swapped = modelFunc.apply(m);
                swapped.loadPartials(event);
                return swapped;
            });
        });
    }


    public static <T extends BakedModel> void swapModels(Map<ResourceLocation, BakedModel> modelRegistry,
                                                         List<ModelResourceLocation> locations, Function<BakedModel, T> factory) {
        locations.forEach(location -> {
            swapModels(modelRegistry, location, factory);
        });
    }

    public static <T extends BakedModel> void swapModels(Map<ResourceLocation, BakedModel> modelRegistry,
                                                         ModelResourceLocation location, Function<BakedModel, T> factory) {
        modelRegistry.put(location, factory.apply(modelRegistry.get(location)));
    }

    public static List<ModelResourceLocation> getAllBlockStateModelLocations(Block block) {
        List<ModelResourceLocation> models = new ArrayList<>();
        ResourceLocation blockRl = ForgeRegistries.BLOCKS.getKey(block);
        block.getStateDefinition()
                .getPossibleStates()
                .forEach(state -> {
                    models.add(BlockModelShaper.stateToModelLocation(blockRl, state));
                });
        return models;
    }

    public static ModelResourceLocation getItemModelLocation(Item item) {
        return new ModelResourceLocation(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)), "inventory");
    }
}
