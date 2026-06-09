package committee.nova.mods.avaritia.common.entity.arrow;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class NeutronArrowEntity extends Arrow {
    public NeutronArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }
    @Override
    public @NotNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        this.discard();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (this.getOwner() != null && entity.equals(this.getOwner())) {
            return;
        }

        if (this.getOwner() != null) {
            if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                entity.hurtServer(serverLevel, this.damageSources().fellOutOfWorld(), 32.0F);
            } else {
                entity.hurtOrSimulate(this.damageSources().fellOutOfWorld(), 32.0F);
            }
        }
    }
}
