package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.tile.BaseInventoryTileEntity;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import committee.nova.mods.avaritia.common.menu.TierCraftMenu;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import committee.nova.mods.avaritia.init.registry.enums.ModCraftTier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 8:44
 * Version: 1.0
 */
public class TierCraftTile extends BaseInventoryTileEntity {

    private final ItemStackWrapper inventory;
    public ModCraftTier tier;

    public TierCraftTile(BlockPos pos, BlockState blockState) {
        super(ModTileEntities.mod_craft_tile.get(), pos, blockState);
        if (blockState.is(ModBlocks.sculk_crafting_table.get())) {
            tier = ModCraftTier.SCULK;
        } else if (blockState.is(ModBlocks.nether_crafting_table.get())) {
            tier = ModCraftTier.NETHER;
        } else if (blockState.is(ModBlocks.end_crafting_table.get())) {
            tier = ModCraftTier.END;
        } else if (blockState.is(ModBlocks.extreme_crafting_table.get())) {
            tier = ModCraftTier.EXTREME;
        }
        this.inventory = new ItemStackWrapper(tier.size * tier.size, Integer.MAX_VALUE, this::setChangedAndDispatch);
    }

    @Override
    public @NotNull ItemStackWrapper getInventory() {
        return inventory;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("container." + tier.name).build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pInventory) {
        switch (tier) {
            case SCULK -> {
                return TierCraftMenu.sculk(pContainerId, pInventory, this.getBlockPos());
            }
            case END -> {
                return TierCraftMenu.end(pContainerId, pInventory, this.getBlockPos());
            }
            case NETHER -> {
                return TierCraftMenu.nether(pContainerId, pInventory, this.getBlockPos());
            }
            case EXTREME -> {
                return TierCraftMenu.extreme(pContainerId, pInventory, this.getBlockPos());
            }
        }
        return TierCraftMenu.extreme(pContainerId, pInventory, this.getBlockPos());
    }

}
