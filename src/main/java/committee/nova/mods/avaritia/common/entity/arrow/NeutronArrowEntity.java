package committee.nova.mods.avaritia.common.entity.arrow;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class NeutronArrowEntity extends Arrow {
    private int life = 0;
    public NeutronArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }
    @Override
    public @NotNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();
        ++this.life;
        if (this.life >= 600) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        if (this.getOwner() != null && entity.equals(this.getOwner())) {
            return;
        }

        if (this.getOwner() != null) {
            entity.hurt(this.damageSources().fellOutOfWorld(), 32.0F);
        }
    }
}
