package committee.nova.mods.avaritia.init.data;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.data.provider.*;
import net.minecraft.DetectedVersion;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Avaritia 数据生成入口类。
 * <p>
 * 订阅 {@link GatherDataEvent.Client} 事件，在运行 {@code runData} 任务时
 * 注册所有数据提供程序（Provider），自动生成语言文件、模型、配方、战利品表、
 * 标签和方块状态等资源文件。
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class AvaritiaData {

    /**
     * 数据生成事件处理方法。
     * <p>
     * 26.3 起配方、战利品表与进度均为数据包注册表对象，
     * 通过 {@link AvaritiaRegistriesProvider} 以 RegistrySetBuilder 形式生成；
     * 其余文件型 Provider 依次注册：
     * <ol>
     *   <li>{@link AvaritiaLanguageProvider} — 语言文件（本地化）</li>
     *   <li>{@link AvaritiaModelProvider} — 物品与方块模型</li>
     *   <li>{@link AvaritiaCompatRecipeProvider} — 兼容配方</li>
     *   <li>{@link AvaritiaTagProvider} — 物品与方块标签</li>
     *   <li>{@link AvaritiaBlockStateProvider} — 方块状态</li>
     * </ol>
     *
     * @param event 客户端数据生成事件
     */
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        AvaritiaRegistriesProvider.register(event);
        // 动态注册表先写入事件 lookup，后续标签 Provider 才能解析 avaritia:infinity。
        var lookupProvider = event.getReloadableLookupProvider();

        // 1. 语言文件提供程序
        generator.addProvider(true, new AvaritiaLanguageProvider(packOutput));

        // 2. 物品与方块模型提供程序
        generator.addProvider(true, new AvaritiaModelProvider(packOutput));

        // 3. 合成配方提供程序（核心配方与战利品表/进度见 AvaritiaRegistriesProvider）
        generator.addProvider(true, new AvaritiaCompatRecipeProvider(packOutput));

        // 4. 物品与方块标签提供程序
        generator.addProvider(true, new AvaritiaTagProvider(packOutput, lookupProvider));

        // 5. 方块状态提供程序
        generator.addProvider(true, new AvaritiaBlockStateProvider(packOutput));
        generator.addProvider(true, new AvaritiaSpriteSourceProvider(packOutput, lookupProvider));
        generator.addProvider(true, new AvaritiaSoundDefinitionsProvider(packOutput));
        generator.addProvider(true, new AvaritiaEquipmentAssetProvider(packOutput));
        generator.addProvider(true, new AvaritiaEntityTypeTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new AvaritiaDamageTypeTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new AvaritiaSingularityProvider(packOutput, lookupProvider));
        var packFormat = DetectedVersion.BUILT_IN.packVersion(PackType.CLIENT_RESOURCES);
        PackMetadataGenerator metadataProvider = new PackMetadataGenerator(packOutput).add(PackMetadataSection.CLIENT_TYPE, new PackMetadataSection(
                Component.literal("Re:Avaritia 26 Generated Resources"),
                packFormat.minorRange()
        ));
        generator.addProvider(true, metadataProvider);
    }
}
