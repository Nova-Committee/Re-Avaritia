package committee.nova.mods.avaritia.common.entity.arrow;

import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
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

public class HeavenArrowEntity extends Arrow {

    private Entity shooter;

    public HeavenArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
        this.shooter = null; // 显式初始化为 null
    }

    public static HeavenArrowEntity create(Level level, LivingEntity shooter) {
        HeavenArrowEntity entity = new HeavenArrowEntity(ModEntities.HEAVEN_ARROW.get(), level);
        entity.shooter = shooter;
        return entity;
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        var pos = result.getBlockPos();
        var randy = level().random;
        if (shooter != null) {
            ToolUtils.arrowBarrage(this.shooter, level(), piercedAndKilledEntities, pickup, randy, pos);
        } else {
            // 记录日志
            System.out.println("HeavenArrowEntity: shooter is null!");
        }
        this.remove(RemovalReason.KILLED);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        Entity entity = result.getEntity();
        final float HEAVEN_ARROW_DAMAGE = 200f;
        if (shooter != null && shooter != entity) {
            entity.hurt(ModDamageTypes.causeRandomDamage(this.shooter), HEAVEN_ARROW_DAMAGE);
        } else {
            // 处理 shooter 为 null 的情况
         //   entity.hurt(ModDamageTypes.causeRandomDamage(entity), Float.MAX_VALUE); // 使用被击中的实体作为默认值
            System.out.println("无尽弓: 射手为空或射到了自己, 无伤害。");
        }
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