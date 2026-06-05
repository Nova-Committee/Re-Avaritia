package com.avaritia.init.data.provider;

import com.avaritia.core.singularity.Singularity;
import com.avaritia.init.registry.ModSingularities;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AvaritiaSingularityProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public AvaritiaSingularityProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "singularities");
        this.registries = registries;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        return this.registries.thenCompose(provider -> CompletableFuture.allOf(ModSingularities.getDefaults()
                .stream()
                .map(singularity -> DataProvider.saveStable(
                        output,
                        provider,
                        Singularity.CONDITIONAL_CODEC,
                        Optional.of(new WithConditions<>(singularity.getConditions(), singularity)),
                        this.pathProvider.json(singularity.getRegistryName())))
                .toArray(CompletableFuture[]::new)));
    }

    @Override
    public @NotNull String getName() {
        return "Avaritia Singularities";
    }
}
