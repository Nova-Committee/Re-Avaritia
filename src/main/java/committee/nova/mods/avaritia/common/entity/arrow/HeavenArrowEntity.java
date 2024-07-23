package committee.nova.mods.avaritia.common.entity.arrow;

import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/20 17:37
 * Version: 1.0
 */
public final class HeavenArrowEntity extends Arrow {
    private LivingEntity shooter;

    public HeavenArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
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
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        var pos = result.getBlockPos();
        var randy = level().random;
        ToolUtils.arrowBarrage(shooter, level(), piercedAndKilledEntities, pickup, randy, pos);
        this.remove(RemovalReason.KILLED);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        ToolUtils.infinityArrowDamage(result, this);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putDouble("damage", Float.POSITIVE_INFINITY);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setBaseDamage(compound.contains("damage") ? compound.getDouble("damage") : Float.POSITIVE_INFINITY);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
