package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.registry.ModSingularities;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * 奇点数据提供者 - 生成标准数据包格式的奇点定义
 *
 * 生成位于 data/avaritia/singularities/ 目录下的奇点定义文件
 * 支持条件加载、标签引用、物品引用等标准数据包功能
 *
 * @author cnlimiter
 * @version 1.0
 */
public class ModSingularityProvider implements DataProvider {

    private final DataGenerator generator;
    private final PackOutput.PathProvider pathProvider;
    private final ExistingFileHelper fileHelper;
    private final Map<ResourceLocation, Singularity> singularities = new TreeMap<>();

    public ModSingularityProvider(DataGenerator generator, ExistingFileHelper fileHelper) {
        this.generator = generator;
        this.pathProvider = generator.getPackOutput().createPathProvider(PackOutput.Target.DATA_PACK, "singularities");
        this.fileHelper = fileHelper;
    }


    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        List<CompletableFuture<?>> list = new ArrayList<>();
        this.singularities.clear();

        // 收集所有默认奇点
        this.collectDefaultSingularities();

        // 生成JSON文件
        for (var entry : this.singularities.entrySet()) {
            var id = entry.getKey();
            var singularity = entry.getValue();

            var json = SingularityUtils.writeToJson(singularity);

            var path = this.pathProvider.json(id);

            list.add(DataProvider.saveStable(output, json, path));
        }

        Const.LOGGER.info("Generated {} singularity data files", this.singularities.size());
        return CompletableFuture.allOf(list.toArray((p_253393_) -> {
            return new CompletableFuture[p_253393_];
        }));
    }

    /**
     * 收集默认奇点数据
     */
    private void collectDefaultSingularities() {
        for (var singularity : ModSingularities.getDefaults()) {
            this.singularities.put(singularity.getId(), singularity);
        }

        Const.LOGGER.debug("Collected {} default singularities for data generation",
                         this.singularities.size());
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Singularities Data Provider";
    }
}