package committee.nova.mods.avaritia.init.data.loot;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Name: Avaritia-forge / ModLootTables
 * Author: cnlimiter
 * CreateTime: 2023/8/24 13:38
 * Description:
 */

public class ModLootTables extends LootTableProvider {
    public ModLootTables(DataGenerator output) {
        super(output);
    }

    @Override
    public @NotNull List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(
                Pair.of(ModBlockLootTables::new, LootContextParamSets.BLOCK)
//                new SubProviderEntry(ModChestLootTables::new, LootContextParamSets.CHEST),
//                new SubProviderEntry(ModEntityLootTables::new, LootContextParamSets.ENTITY),
//                new SubProviderEntry(ModGiftLootTables::new, LootContextParamSets.GIFT)
        );
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, @NotNull ValidationContext validationContext) {
        map.forEach((name, loo) -> {
            loo.validate(validationContext.setParams(loo.getParamSet()).enterTable("{" + name + "}", name));
        });
    }
}
