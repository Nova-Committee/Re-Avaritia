package nova.committee.avaritia.init.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.common.tile.CompressorTileEntity;
import nova.committee.avaritia.common.tile.ExtremeCraftingTile;
import nova.committee.avaritia.common.tile.NeutronCollectorTile;
import nova.committee.avaritia.util.RegistryUtil;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:48
 * Version: 1.0
 */
@Mod.EventBusSubscriber(modid = Static.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModTileEntities {

    public static BlockEntityType<ExtremeCraftingTile> extreme_crafting_tile;
    public static BlockEntityType<NeutronCollectorTile> neutron_collector_tile;
    public static BlockEntityType<CompressorTileEntity> compressor_tile;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<BlockEntityType<?>> event) {
        final IForgeRegistry<BlockEntityType<?>> registry = event.getRegistry();

        registry.registerAll(
                extreme_crafting_tile = RegistryUtil.build(ExtremeCraftingTile::new, "extreme_crafting_tile", ModBlocks.extreme_crafting_table),
                neutron_collector_tile = RegistryUtil.build(NeutronCollectorTile::new, "neutron_collector_tile", ModBlocks.neutron_collector),
                compressor_tile = RegistryUtil.build(CompressorTileEntity::new, "compressor_tile", ModBlocks.compressor)


        );

    }
}
