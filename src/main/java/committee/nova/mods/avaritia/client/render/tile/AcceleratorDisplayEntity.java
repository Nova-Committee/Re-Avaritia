// committee/nova/mods/avaritia/common/entity/AcceleratorDisplayEntity.java
package committee.nova.mods.avaritia.client.render.tile;

import committee.nova.mods.avaritia.common.item.misc.InfinityClockItem;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashMap;

public class AcceleratorDisplayEntity extends Entity {
    // 同步加速倍数（客户端需要这个数据来渲染）
    private static final EntityDataAccessor<Integer> SPEED_MULTIPLIER =
            SynchedEntityData.defineId(AcceleratorDisplayEntity.class, EntityDataSerializers.INT);
    // 关联的方块位置
    private BlockPos targetPos;

    public AcceleratorDisplayEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true; // 不需要物理碰撞
        this.setInvisible(true); // 实体本身不可见（只显示文字）
    }

    public AcceleratorDisplayEntity(Level level, BlockPos targetPos, int speed) {
        this(ModEntities.acceleratorDisplayEntity.get(), level);
        this.targetPos = targetPos;
        this.entityData.set(SPEED_MULTIPLIER, speed);
        // 实体位置设置在方块中心
        this.setPos(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SPEED_MULTIPLIER, 1);
    }

    // 获取加速倍数
    public int getSpeedMultiplier() {
        return entityData.get(SPEED_MULTIPLIER);
    }

    // 更新加速倍数
    public void setSpeedMultiplier(int speed) {
        entityData.set(SPEED_MULTIPLIER, speed);
    }

    @Override
    public void tick() {
        super.tick();
        // 如果方块被移除或不在加速列表中，删除实体
        if (!level().isClientSide && (targetPos == null ||
                !InfinityClockItem.acceleratedBlocks.getOrDefault(level().dimension(), new HashMap<>()).containsKey(targetPos))) {
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        targetPos = BlockPos.of(nbt.getLong("TargetPos"));
        entityData.set(SPEED_MULTIPLIER, nbt.getInt("Speed"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putLong("TargetPos", targetPos.asLong());
        nbt.putInt("Speed", getSpeedMultiplier());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}