package committee.nova.mods.avaritia.common.entity.arrow;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class NeutronArrowEntity extends Arrow {
    private static final int MAX_LIFETIME_TICKS = 1200;

    public NeutronArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount >= MAX_LIFETIME_TICKS) {
            this.discard();
        }
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
        Entity entity = result.getEntity();

        if (this.getOwner() != null && entity.equals(this.getOwner())) {
            return;
        }

        super.onHitEntity(result);

        if (this.getOwner() != null) {
            entity.hurt(this.damageSources().fellOutOfWorld(), 32.0F);
        }
    }
}
