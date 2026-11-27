package committee.nova.mods.avaritia.common.entity.arrow;

import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
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

public class HeavenArrowEntity extends Arrow {

    public HeavenArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    public HeavenArrowEntity(Level world, Entity pShooter, double xPos, double yPos, double zPos) {
        this(ModEntityTypes.HEAVEN_ARROW.get(), world);
        this.setOwner(pShooter);
        this.setPos(xPos, yPos, zPos);
    }

    public HeavenArrowEntity(Level world, Entity pShooter) {
        this(world, pShooter, pShooter.getX(), pShooter.getEyeY() - (double)0.1F, pShooter.getZ());
        if (pShooter instanceof Player) {
            this.pickup = AbstractArrow.Pickup.ALLOWED;
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
            arrowBarrage(this.getOwner(), level(), pickup, randy, pos);
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
            damageEntity(entity, ModDamageTypes.source(this.getOwner()), HEAVEN_ARROW_DAMAGE);
        }
    }

    private void damageEntity(Entity entity, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            entity.hurtServer(serverLevel, source, amount);
        } else {
            entity.hurtOrSimulate(source, amount);
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

    private static void arrowBarrage(Entity shooter, Level level, AbstractArrow.Pickup pickup, RandomSource random, BlockPos pos) {
        for (int i = 0; i < 50; i++) {
            double angle = random.nextDouble() * 9 * Math.PI;
            double dist = random.nextGaussian() * 0.8;
            double x = Math.sin(angle) * dist + pos.getX();
            double z = Math.cos(angle) * dist + pos.getZ();
            double y = pos.getY() + 25.0;

            double deltaAngle = random.nextDouble() * 9 * Math.PI;
            double deltaDistance = random.nextDouble() * 0.35;
            double dx = Math.sin(deltaAngle) * deltaDistance;
            double dz = Math.cos(deltaAngle) * deltaDistance;

            HeavenSubArrowEntity subArrow = new HeavenSubArrowEntity(level, shooter, x, y, z);
            subArrow.push(dx, -(random.nextDouble() * 1.85 + 0.15), dz);
            subArrow.setCritArrow(true);
            subArrow.setBaseDamage(500.0D);
            subArrow.pickup = pickup;

            level.addFreshEntity(subArrow);
        }
    }
}
