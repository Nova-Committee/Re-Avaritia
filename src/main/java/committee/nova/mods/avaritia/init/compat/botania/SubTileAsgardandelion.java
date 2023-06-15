//package nova.committee.avaritia.init.compat.botania;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.block.state.BlockState;
//import nova.committee.avaritia.init.config.CommonConfig;
//import nova.committee.avaritia.init.registry.ModSubtiles;
//import org.jetbrains.annotations.Nullable;
//import vazkii.botania.api.subtile.RadiusDescriptor;
//import vazkii.botania.api.subtile.TileEntityGeneratingFlower;
//
///**
// * Description:
// * Author: cnlimiter
// * Date: 2022/5/17 16:07
// * Version: 1.0
// */
//public class SubTileAsgardandelion extends TileEntityGeneratingFlower {
//    public SubTileAsgardandelion(BlockPos pos, BlockState state) {
//        super(ModSubtiles.asgardandelion, pos, state);
//    }
//
//    @Override
//    public int getMaxMana() {
//        return CommonConfig.COMMON.asgardandelionMaxMana.get();
//    }
//
//    @Override
//    public int getColor() {
//        return 0x11FF00;
//    }
//
//    @Nullable
//    @Override
//    public RadiusDescriptor getRadius() {
//        return RadiusDescriptor.Rectangle.square(this.worldPosition, 10000);
//    }
//
//    @Override
//    public int getModulatedDelay() {
//        return CommonConfig.COMMON.generationCycleDelay.get();
//    }
//
//
//}
