package committee.nova.mods.avaritia.init.data.provider.base;

import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 奇点数据提供者 - 生成标准数据包格式的奇点定义
 *
 * 生成位于 data/avaritia/singularities/ 目录下的奇点定义文件
 * 支持条件加载、标签引用、物品引用等标准数据包功能
 *
 * @author cnlimiter
 * @version 1.0
 */
public abstract class SingularityProvider implements DataProvider {

    private final DataGenerator generator;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final PackOutput.PathProvider pathProvider;
    private final ExistingFileHelper fileHelper;
    private final Map<ResourceLocation, Singularity> singularities = new TreeMap<>();

    public SingularityProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
        this.generator = generator;
        this.registries = registries;
        this.pathProvider = generator.getPackOutput().createPathProvider(PackOutput.Target.DATA_PACK, "singularities");
        this.fileHelper = fileHelper;
    }


    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        return this.registries.thenCompose((provider) -> {
            List<CompletableFuture<?>> list = new ArrayList<>();
            this.generate(provider, this.fileHelper);
            this.singularities.forEach((res, singularity) -> {
                list.add(DataProvider.saveStable(
                        output,
                        SingularityUtils.writeToJson(singularity),
                        this.pathProvider.json(res)
                ));
            });
            return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
        });
    }

    public abstract void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper);

    public final void addSingularity(List<Singularity> singularities) {
        this.singularities.putAll(singularities.stream()
                .map(Singularity::copy)
                .collect(Collectors.toMap(Singularity::getRegistryName, s -> s)));
    }

    public final void addSingularity(Singularity singularities) {
        Singularity copy = singularities.copy();
        this.singularities.put(copy.getRegistryName(), copy);
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Singularities Data Provider";
    }
}
