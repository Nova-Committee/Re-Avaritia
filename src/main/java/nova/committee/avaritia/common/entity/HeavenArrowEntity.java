package nova.committee.avaritia.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import nova.committee.avaritia.init.registry.ModEntities;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/20 17:37
 * Version: 1.0
 */
public class HeavenArrowEntity extends Arrow {

    private Entity shooter;

    public HeavenArrowEntity(EntityType<? extends Arrow> p_36858_, Level p_36859_) {
        super(p_36858_, p_36859_);
        this.setBaseDamage(Float.POSITIVE_INFINITY);

    }

    public static HeavenArrowEntity create(Level level, Entity shooter) {
        HeavenArrowEntity entity = new HeavenArrowEntity(ModEntities.HeavenArrow.get(), level);
        entity.setShooter(shooter);
        return entity;
    }

    public void setShooter(Entity shooter) {
        this.shooter = shooter;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        var pos = result.getLocation();
        var randy = getLevel().random;
        for (int i = 0; i < 30; i++) {
            double angle = randy.nextDouble() * 2 * Math.PI;
            double dist = randy.nextGaussian() * 0.5;

            double x = Math.sin(angle) * dist + pos.x();
            double z = Math.cos(angle) * dist + pos.z();
            double y = pos.y() + 25.0;

            double dangle = randy.nextDouble() * 2 * Math.PI;
            double ddist = randy.nextDouble() * 0.35;
            double dx = Math.sin(dangle) * ddist;
            double dz = Math.cos(dangle) * ddist;

            HeavenSubArrowEntity subArrow = HeavenSubArrowEntity.create(level, x, y, z);
            if (shooter != null) subArrow.setOwner(shooter);
            subArrow.piercedAndKilledEntities = piercedAndKilledEntities;
            subArrow.push(dx, -(randy.nextDouble() * 1.85 + 0.15), dz);
            subArrow.setCritArrow(true);
            subArrow.pickup = pickup;

            level.addFreshEntity(subArrow);
        }
        remove(RemovalReason.KILLED);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putDouble("damage", Float.POSITIVE_INFINITY);
    }


    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setBaseDamage(compound.getDouble("damage"));
    }


    @Override
    public @NotNull Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
