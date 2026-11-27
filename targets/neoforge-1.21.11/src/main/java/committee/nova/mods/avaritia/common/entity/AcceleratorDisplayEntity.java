package committee.nova.mods.avaritia.common.entity;

import committee.nova.mods.avaritia.common.item.misc.InfinityClockItem;
import committee.nova.mods.avaritia.init.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class AcceleratorDisplayEntity extends Entity {

    private static final EntityDataAccessor<Integer> SPEED_MULTIPLIER =
            SynchedEntityData.defineId(AcceleratorDisplayEntity.class, EntityDataSerializers.INT);
    // 同步面信息
    private static final EntityDataAccessor<Integer> FACE =
            SynchedEntityData.defineId(AcceleratorDisplayEntity.class, EntityDataSerializers.INT);
    // 关联的方块位置
    private BlockPos targetPos;
    private int particleTimer = 0;

    public AcceleratorDisplayEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true; // 不需要物理碰撞
        this.setInvisible(true); // 实体本身不可见（只显示文字）
    }

    public AcceleratorDisplayEntity(Level level, BlockPos targetPos, int speed, Direction face) {
        this(ModEntityTypes.ACCELERATOR_DISPLAY.get(), level);
        this.targetPos = targetPos;
        this.entityData.set(SPEED_MULTIPLIER, speed);
        this.entityData.set(FACE, face.get3DDataValue()); // 保存面信息
        // 根据面信息设置实体位置
        setPositionByFace(targetPos, face);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPEED_MULTIPLIER, 1);
        builder.define(FACE, Direction.NORTH.get3DDataValue());
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
        if (!level().isClientSide() && (targetPos == null || !isTargetStillAccelerated())) {
            this.remove(RemovalReason.KILLED);
        }

        // 客户端粒子效果
        if (level().isClientSide() && targetPos != null) {
            renderEffects();
        }
    }

    private boolean isTargetStillAccelerated() {
        return InfinityClockItem.acceleratedBlocks
                .getOrDefault(level().dimension(), Map.of())
                .containsKey(targetPos);
    }

    private void renderEffects() {
        if (targetPos == null) return;

        particleTimer++;
        Level level = this.level();
        long gameTime = level.getGameTime();
        if (particleTimer % 2 == 0) {
            for (int i = 0; i < 10; i++) {
                double hAngle = (gameTime * 0.5 + i * 40) % 360;
                double hRadius = 0.6;
                double hX = targetPos.getX() + 0.5 + Math.cos(Math.toRadians(hAngle)) * hRadius;
                double hZ = targetPos.getZ() + 0.5 + Math.sin(Math.toRadians(hAngle)) * hRadius;
                double hY = targetPos.getY() + 0.5 + (i % 3 - 1) * 0.2;

                level.addParticle(
                        ParticleTypes.ENCHANT,
                        hX, hY, hZ,
                        0, 0, 0
                );

                double vAngle = (gameTime * 0.7 + i * 60) % 360;
                double vRadius = 0.6;
                double vX = targetPos.getX() + 0.5 + Math.cos(Math.toRadians(vAngle)) * vRadius;
                double vY = targetPos.getY() + 0.5 + Math.sin(Math.toRadians(vAngle)) * vRadius;
                double vZ = targetPos.getZ() + 0.5 + (i % 2 - 0.5) * 0.2;

                level.addParticle(
                        ParticleTypes.ENCHANT,
                        vX, vY, vZ,
                        0, 0, 0
                );
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        targetPos = BlockPos.of(input.getLongOr("TargetPos", 0L));
        entityData.set(SPEED_MULTIPLIER, input.getIntOr("Speed", 1));
        entityData.set(FACE, input.getIntOr("Face", Direction.NORTH.get3DDataValue()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putLong("TargetPos", targetPos.asLong());
        output.putInt("Speed", getSpeedMultiplier());
        output.putInt("Face", getFace().get3DDataValue());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }
}
