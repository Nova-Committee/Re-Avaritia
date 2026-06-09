package committee.nova.mods.avaritia.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class RainProEntity extends ThrowableItemProjectile {
    public RainProEntity(EntityType<? extends RainProEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide()) {
            if (result instanceof BlockHitResult blockHit) {
                BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
                this.level().setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
            } else if (result instanceof EntityHitResult entityHit) {
                BlockPos pos = entityHit.getEntity().blockPosition();
                this.level().setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
            }
            this.discard();
        }
    }
}
