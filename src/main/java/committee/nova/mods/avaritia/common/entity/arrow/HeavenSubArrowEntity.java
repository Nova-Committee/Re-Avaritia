package committee.nova.mods.avaritia.common.entity.arrow;

import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
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

    public HeavenSubArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    public static HeavenSubArrowEntity create(Level level, double x, double y, double z) {
        HeavenSubArrowEntity entity = new HeavenSubArrowEntity(ModEntities.HEAVEN_SUB_ARROW.get(), level);
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
        ToolUtils.infinityArrowDamage(result, this);
    }
}
