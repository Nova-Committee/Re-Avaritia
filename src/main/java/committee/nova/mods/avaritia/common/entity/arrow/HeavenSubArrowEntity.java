package committee.nova.mods.avaritia.common.entity.arrow;

import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/20 17:43
 * Version: 1.0
 */
public class HeavenSubArrowEntity extends Arrow {

    private Entity shooter;

    public HeavenSubArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    public static HeavenSubArrowEntity create(Level level, Entity shooter) {
        HeavenSubArrowEntity entity = new HeavenSubArrowEntity(ModEntities.HEAVEN_SUB_ARROW.get(), level);
        entity.setPos(shooter.getX(), shooter.getY() + 1.2, shooter.getZ());
        entity.shooter = shooter;
        return entity;
    }

    public static HeavenSubArrowEntity create(Level level, Entity shooter, double x, double y, double z) {
        HeavenSubArrowEntity entity = new HeavenSubArrowEntity(ModEntities.HEAVEN_SUB_ARROW.get(), level);
        entity.setPos(x, y, z);
        entity.shooter = shooter;
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
    public @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        Entity entity = result.getEntity();
        // if (shooter != null) entity.hurt(ModDamageTypes.causeRandomDamage(shooter), 2000F);
        final float HEAVEN_ARROW_DAMAGE = 200f;
        if (shooter != null && shooter != entity) {
            entity.hurt(ModDamageTypes.causeRandomDamage(this.shooter), HEAVEN_ARROW_DAMAGE);
        } else {
            //   entity.hurt(ModDamageTypes.causeRandomDamage(entity), Float.MAX_VALUE); // 使用被击中的实体作为默认值
            System.out.println("无尽弓 - SubArrow: 射手为空或射到了自己, 无伤害。");
        }
    }
}
