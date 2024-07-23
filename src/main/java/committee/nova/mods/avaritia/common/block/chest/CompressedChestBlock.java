package committee.nova.mods.avaritia.common.block.chest;

import committee.nova.mods.avaritia.common.tile.CompressedChestTile;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

/**
 * Project: Avaritia
 * Author: cnlimiter
 * CreateTime: 2024/7/13 下午12:38
 * Description:
 */
public final class CompressedChestBlock extends ChestBlock {
    public CompressedChestBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava(), () -> ModTileEntities.compressed_chest_tile.get());
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new CompressedChestTile(pPos, pState);
    }
}
