package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.model.bake.InfinitySwordBakeModel;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Objects;

/**
 * ModelBakeReplaceHandler
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/9 下午2:01
 */
@Mod.EventBusSubscriber(modid = Static.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModelBakeReplaceHandler {
    @SubscribeEvent
    public static void onModelBaked(ModelEvent.ModifyBakingResult event){
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModels();
        ModelResourceLocation location = new ModelResourceLocation(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(ModItems.infinity_sword.get())), "inventory");
        BakedModel existingModel = modelRegistry.get(location);
        if (existingModel == null) {
            throw new RuntimeException("Did not find Obsidian Hidden in registry");
        }else if (existingModel instanceof InfinitySwordBakeModel) {
            throw new RuntimeException("Tried to replaceObsidian Hidden twice");
        } else {
            InfinitySwordBakeModel obsidianWrenchBakedModel = new InfinitySwordBakeModel(existingModel);
            event.getModels().replace(location, obsidianWrenchBakedModel);
        }
    }
}
