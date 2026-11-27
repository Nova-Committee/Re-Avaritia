package committee.nova.mods.avaritia.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class StormProEntity extends ThrowableItemProjectile {
    public StormProEntity(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            BlockPos pos = null;


            if (result instanceof BlockHitResult blockHit) {
                pos = blockHit.getBlockPos().relative(blockHit.getDirection());
            } else if (result instanceof EntityHitResult entityHit) {
                pos = entityHit.getEntity().blockPosition();
            }

            if (pos != null) {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level());
                if (lightning != null) {
                    lightning.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);


                    if (this.getOwner() instanceof ServerPlayer player) {
                        lightning.setCause(player);
                    }

                    this.level().addFreshEntity(lightning);
                }
            }

            this.discard();
        }
    }

}