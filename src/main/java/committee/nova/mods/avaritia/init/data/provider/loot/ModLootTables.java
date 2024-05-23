//package committee.nova.mods.avaritia.init.data.provider.loot;
//
//import com.google.common.collect.ImmutableList;
//import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
//import net.minecraft.data.PackOutput;
//import net.minecraft.data.loot.LootTableProvider;
//import net.minecraft.data.loot.packs.VanillaLootTableProvider;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.level.storage.loot.LootDataId;
//import net.minecraft.world.level.storage.loot.LootDataType;
//import net.minecraft.world.level.storage.loot.LootTable;
//import net.minecraft.world.level.storage.loot.ValidationContext;
//import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.function.BiConsumer;
//
///**
// * Name: Avaritia-forge / ModLootTables
// * Author: cnlimiter
// * CreateTime: 2023/8/24 13:38
// * Description:
// */
//
//public class ModLootTables extends SimpleFabricLootTableProvider {
//    public ModLootTables(PackOutput output) {
//        super(output, Collections.emptySet(), VanillaLootTableProvider.create(output).getTables());
//    }
//
//    @Override
//    public @NotNull List<SubProviderEntry> getTables() {
//        return ImmutableList.of(
//                new SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)
////                new SubProviderEntry(ModChestLootTables::new, LootContextParamSets.CHEST),
////                new SubProviderEntry(ModEntityLootTables::new, LootContextParamSets.ENTITY),
////                new SubProviderEntry(ModGiftLootTables::new, LootContextParamSets.GIFT)
//        );
//    }
//
//    @Override
//    protected void generate(Map<ResourceLocation, LootTable> map, @NotNull ValidationContext validationContext) {
//        map.forEach((name, loo) -> {
//            loo.validate(validationContext.setParams(loo.getParamSet()).enterElement("{" + name + "}", new LootDataId<>(LootDataType.TABLE, name)));
//        });
//    }
//
//    @Override
//    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
//
//    }
//}
