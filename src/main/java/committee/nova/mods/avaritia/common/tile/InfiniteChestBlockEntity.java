package committee.nova.mods.avaritia.common.tile;

import committee.nova.mods.avaritia.common.menu.InfiniteChestMenu2;
import committee.nova.mods.avaritia.common.wrappers.InfiniteItemHandler;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author: cnlimiter
 */
public class InfiniteChestBlockEntity extends BlockEntity implements MenuProvider {
    private final InfiniteItemHandler itemHandler = new InfiniteItemHandler();
    private final LazyOptional<IItemHandler> itemHandlerLazy = LazyOptional.of(() -> itemHandler);

    // 权限系统
    private final Map<UUID, AccessLevel> accessLevels = new HashMap<>();
    private UUID owner;

    public enum AccessLevel {
        OWNER, FULL_ACCESS, VIEW_ONLY, NO_ACCESS
    }

    public InfiniteChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.infinity_chest_block_entity.get(), pos, state);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Infinite Chest");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new InfiniteChestMenu2(containerId, inventory, this.getBlockPos());
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerLazy.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());

        // 保存权限信息
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }

        CompoundTag accessTag = new CompoundTag();
        for (Map.Entry<UUID, AccessLevel> entry : accessLevels.entrySet()) {
            accessTag.putInt(entry.getKey().toString(), entry.getValue().ordinal());
        }
        tag.put("AccessLevels", accessTag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));

        // 加载权限信息
        if (tag.hasUUID("Owner")) {
            owner = tag.getUUID("Owner");
        }

        CompoundTag accessTag = tag.getCompound("AccessLevels");
        for (String key : accessTag.getAllKeys()) {
            UUID playerId = UUID.fromString(key);
            int levelOrdinal = accessTag.getInt(key);
            if (levelOrdinal >= 0 && levelOrdinal < AccessLevel.values().length) {
                accessLevels.put(playerId, AccessLevel.values()[levelOrdinal]);
            }
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public InfiniteItemHandler getItemHandler() {
        return itemHandler;
    }

    // 权限管理方法
    public void setOwner(UUID playerId) {
        this.owner = playerId;
        setChanged();
    }

    public UUID getOwner() {
        return owner;
    }

    public void setAccessLevel(UUID playerId, AccessLevel level) {
        accessLevels.put(playerId, level);
        setChanged();
    }

    public AccessLevel getAccessLevel(UUID playerId) {
        if (playerId.equals(owner)) {
            return AccessLevel.OWNER;
        }
        return accessLevels.getOrDefault(playerId, AccessLevel.NO_ACCESS);
    }

    public boolean canAccess(Player player, boolean modify) {
        AccessLevel level = getAccessLevel(player.getUUID());
        if (level == AccessLevel.OWNER) {
            return true;
        }
        if (modify) {
            return level == AccessLevel.FULL_ACCESS;
        } else {
            return level == AccessLevel.FULL_ACCESS || level == AccessLevel.VIEW_ONLY;
        }
    }

}
