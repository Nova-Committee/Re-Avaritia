package committee.nova.mods.avaritia.common.entity.arrow;

import committee.nova.mods.avaritia.init.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/20 17:37
 * Version: 1.0
 */
public class HeavenArrowEntity extends Arrow {

    private LivingEntity shooter;

    public HeavenArrowEntity(EntityType<? extends Arrow> p_36858_, Level p_36859_) {
        super(p_36858_, p_36859_);

    }

    public static HeavenArrowEntity create(Level level, LivingEntity shooter) {
        HeavenArrowEntity entity = new HeavenArrowEntity(ModEntities.HEAVEN_ARROW.get(), level);
        entity.setShooter(shooter);
        return entity;
    }

    public void setShooter(LivingEntity shooter) {
        this.shooter = shooter;
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        var randy = level().random;
        
        if (entity instanceof LivingEntity living) {
            var pos = living.getOnPos();
            barrage(randy, pos);
            this.remove(RemovalReason.KILLED);
        }
    }

    private void barrage(RandomSource randy, BlockPos pos) {
        for (int i = 0; i < 30; i++) {
            double angle = randy.nextDouble() * 2 * Math.PI;
            double dist = randy.nextGaussian() * 0.5;

            double x = Math.sin(angle) * dist + pos.getX();
            double z = Math.cos(angle) * dist + pos.getZ();
            double y = pos.getY() + 25.0;

            double dangle = randy.nextDouble() * 2 * Math.PI;
            double ddist = randy.nextDouble() * 0.35;
            double dx = Math.sin(dangle) * ddist;
            double dz = Math.cos(dangle) * ddist;

            HeavenSubArrowEntity subArrow = HeavenSubArrowEntity.create(level(), x, y, z);
            if (shooter != null) subArrow.setOwner(shooter);
            subArrow.piercedAndKilledEntities = piercedAndKilledEntities;
            subArrow.push(dx, -(randy.nextDouble() * 1.85 + 0.15), dz);
            subArrow.setCritArrow(true);
            subArrow.pickup = pickup;

            level().addFreshEntity(subArrow);
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        var pos = result.getBlockPos();
        var randy = level().random;
        barrage(randy, pos);
        remove(RemovalReason.KILLED);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putDouble("damage", Float.POSITIVE_INFINITY);
    }


    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setBaseDamage(compound.getDouble("damage"));
    }


    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }


    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
