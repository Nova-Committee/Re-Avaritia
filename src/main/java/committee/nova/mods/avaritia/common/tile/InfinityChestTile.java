package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.api.common.tile.BaseTileEntity;
import committee.nova.mods.avaritia.api.utils.lang.Localizable;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.core.chest.ServerChestHandler;
import committee.nova.mods.avaritia.core.chest.ServerChestManager;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/1/31 15:28
 * @Description:
 */
public class InfinityChestTile extends BaseTileEntity implements LidBlockEntity {
    @Getter
    private UUID owner;
    @Getter
    private boolean locked = false;
    @Getter
    private String filter = "";
    @Getter
    private byte sortType = 4;
    @Getter
    private UUID channelID = UUID.randomUUID();
    @Getter
    private ServerChestHandler channel = new ServerChestHandler();

    public InfinityChestTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.infinity_chest_tile.get(), pos, state);
    }


    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("block.avaritia.infinity_chest").build();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new InfinityChestMenu(containerId, player, this);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag pTag, HolderLookup.@NotNull Provider registries) {
        if (pTag.contains("owner")) {
            owner = pTag.getUUID("owner");
            locked = pTag.getBoolean("locked");
        }
        if (pTag.contains("filter")) filter = pTag.getString("filter");
        if (pTag.contains("sortType")) sortType = pTag.getByte("sortType");
        if (pTag.contains("channelID")) channelID = pTag.getUUID("channelID");
        channel = ServerChestManager.getInstance().getChest(owner, channelID);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag pTag, HolderLookup.@NotNull Provider registries) {
        if (owner != null) {
            pTag.putUUID("owner", owner);
            pTag.putBoolean("locked", locked);
        }
        pTag.putString("filter", filter);
        pTag.putByte("sortType", sortType);
        pTag.putUUID("channelID", channelID);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        this.setChanged();
    }

    public void setFilter(String filter) {
        this.filter = filter;
        this.setChanged();
    }

    public void setSortType(byte sortType) {
        this.sortType = sortType;
        this.setChanged();
    }

    public void setChannelId(UUID id) {
        this.channelID = id;
        this.setChanged();
    }

    private final ChestLidController chestLidController = new ChestLidController();
    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, InfinityChestTile blockEntity) {
        blockEntity.chestLidController.tickLid();
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.chestLidController.shouldBeOpen(type > 0);
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return this.chestLidController.getOpenness(partialTicks);
    }

    public static void playSound(Level pLevel, BlockPos pPos, SoundEvent pSound) {
        double d0 = (double) pPos.getX() + 0.5;
        double d1 = (double) pPos.getY() + 0.5;
        double d2 = (double) pPos.getZ() + 0.5;
        pLevel.playSound(null, d0, d1, d2, pSound, SoundSource.BLOCKS, 0.5F, pLevel.random.nextFloat() * 0.1F + 0.9F);
    }
}
