package committee.nova.mods.avaritia.common.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2CTotemPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public class S2CTotemPack {
    private final ItemStack stack;
    private final int entityId;

    public S2CTotemPack(FriendlyByteBuf buf) {
        this.stack = buf.readItem();
        this.entityId = buf.readInt();
    }

    public S2CTotemPack(ItemStack stack, int entityId) {
        this.stack = stack;
        this.entityId = entityId;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeItem(stack);
        buf.writeInt(entityId);
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            playTotem(stack, entityId); //处理服务端发送给客户端的消息
        });
        ctx.get().setPacketHandled(true);
    }

    //播放图腾动画，声音，粒子
    @Environment(EnvType.CLIENT)
    public static void playTotem(ItemStack stack, int entityId) {
        Minecraft instance = Minecraft.getInstance();
        ClientLevel world = instance.level;

        if (world != null) {
            Entity entity = world.getEntity(entityId);
            if (entity != null) {
                instance.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
                instance.gameRenderer.displayItemActivation(stack);
            }
        }
    }
}
