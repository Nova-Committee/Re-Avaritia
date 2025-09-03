// committee/nova/mods/avaritia/common/entity/AcceleratorDisplayEntity.java
package committee.nova.mods.avaritia.common.entity;

import committee.nova.mods.avaritia.common.item.misc.InfinityClockItem;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.HashMap;

public class AcceleratorDisplayEntity extends Entity {

    private static final EntityDataAccessor<Integer> SPEED_MULTIPLIER =
            SynchedEntityData.defineId(AcceleratorDisplayEntity.class, EntityDataSerializers.INT);
    // 同步面信息
    private static final EntityDataAccessor<Integer> FACE =
            SynchedEntityData.defineId(AcceleratorDisplayEntity.class, EntityDataSerializers.INT);
    // 关联的方块位置
    private BlockPos targetPos;

    public AcceleratorDisplayEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true; // 不需要物理碰撞
        this.setInvisible(true); // 实体本身不可见（只显示文字）
    }

    public AcceleratorDisplayEntity(Level level, BlockPos targetPos, int speed, Direction face) {
        this(ModEntities.acceleratorDisplayEntity.get(), level);
        this.targetPos = targetPos;
        this.entityData.set(SPEED_MULTIPLIER, speed);
        this.entityData.set(FACE, face.get3DDataValue()); // 保存面信息
        // 根据面信息设置实体位置
        setPositionByFace(targetPos, face);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SPEED_MULTIPLIER, 1);
        entityData.define(FACE, Direction.NORTH.get3DDataValue());
    }

    // 获取加速倍数
    public int getSpeedMultiplier() {
        return entityData.get(SPEED_MULTIPLIER);
    }

    // 更新加速倍数
    public void setSpeedMultiplier(int speed) {
        entityData.set(SPEED_MULTIPLIER, speed);
    }

    // 获取面信息
    public Direction getFace() {
        return Direction.from3DDataValue(entityData.get(FACE));
    }

    // 设置面信息
    public void setFace(Direction face) {
        entityData.set(FACE, face.get3DDataValue());
    }

    // 根据面信息设置实体位置
    private void setPositionByFace(BlockPos pos, Direction face) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // 根据面偏移位置，确保文字在方块表面外侧
        double offset = 0.51; // 略大于0.5以确保在方块外
        switch (face) {
            case UP -> y = pos.getY() + offset;
            case DOWN -> y = pos.getY() - offset + 1.0;
            case NORTH -> z = pos.getZ() - offset + 1.0;
            case SOUTH -> z = pos.getZ() + offset;
            case WEST -> x = pos.getX() - offset + 1.0;
            case EAST -> x = pos.getX() + offset;
        }

        this.setPos(x, y, z);
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
        entityData.set(FACE, nbt.getInt("Face"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putLong("TargetPos", targetPos.asLong());
        nbt.putInt("Speed", getSpeedMultiplier());
        nbt.putInt("Face", getFace().get3DDataValue());
    }

//    @Override
//    public Packet<ClientGamePacketListener> getAddEntityPacket() {
//        return NetworkHooks.getEntitySpawningPacket(this);
//    }
}
