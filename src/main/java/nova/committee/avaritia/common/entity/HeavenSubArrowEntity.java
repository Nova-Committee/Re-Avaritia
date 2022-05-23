package nova.committee.avaritia.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import nova.committee.avaritia.init.config.ModConfig;
import nova.committee.avaritia.init.registry.ModEntities;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/20 17:43
 * Version: 1.0
 */
public class HeavenSubArrowEntity extends Arrow {

    public HeavenSubArrowEntity(EntityType<? extends Arrow> p_36858_, Level p_36859_) {
        super(p_36858_, p_36859_);
    }

    public static HeavenSubArrowEntity create(Level level, double x, double y, double z) {
        HeavenSubArrowEntity entity = new HeavenSubArrowEntity(ModEntities.HeavenSubArrow.get(), level);
        entity.setPos(x, y, z);
        return entity;
    }

    @Override
    public void tick() {
        super.tick();

        if (inGround && inGroundTime >= 20) {
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }


    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setBaseDamage(compound.getDouble("damage"));

    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putDouble("damage", ModConfig.SERVER.subArrowDamage.get());

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }
}
