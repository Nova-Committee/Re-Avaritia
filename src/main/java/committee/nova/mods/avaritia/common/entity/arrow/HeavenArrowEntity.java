package committee.nova.mods.avaritia.common.entity.arrow;

import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HeavenArrowEntity extends Arrow {

    public HeavenArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    public HeavenArrowEntity(Level world, Entity pShooter, double xPos, double yPos, double zPos) {
        this(ModEntities.HEAVEN_ARROW.get(), world);
        this.setOwner(pShooter);
        this.setPos(xPos, yPos, zPos);
    }

    public HeavenArrowEntity(Level world, Entity pShooter) {
        this(world, pShooter, pShooter.getX(), pShooter.getEyeY() - (double) 0.1F, pShooter.getZ());
        if (pShooter instanceof Player) {
            this.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
    }

    public HeavenArrowEntity(Entity pShooter) {
        this(pShooter.level(), pShooter);
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        var pos = result.getBlockPos();
        var randy = level().getRandom();
        if (getOwner() != null) {
            ToolUtils.arrowBarrage(this.getOwner(), level(), List.of(), pickup, randy, pos);
        }
        this.remove(RemovalReason.KILLED);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        Entity entity = result.getEntity();
        final float HEAVEN_ARROW_DAMAGE = 500f;
        if (getOwner() != null) {
            if (entity == getOwner()) {
                return;
            }
            if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                entity.hurtServer(serverLevel, ModDamageTypes.causeRandomDamage(level(), this.getOwner()), HEAVEN_ARROW_DAMAGE);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putDouble("damage", Float.POSITIVE_INFINITY);
    }

    @Override
    public void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setBaseDamage(input.getDoubleOr("damage", Float.POSITIVE_INFINITY));
    }

    @Override
    public @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
