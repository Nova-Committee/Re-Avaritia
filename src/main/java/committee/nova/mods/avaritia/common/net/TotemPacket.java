package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.common.net.SimplePacketBase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/**
 * TotemPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public class TotemPacket extends SimplePacketBase {
    private final ItemStack stack;
    private final int entityId;
    public TotemPacket(FriendlyByteBuf buf) {
        this.stack = buf.readItem();
        this.entityId = buf.readInt();
    }
    public TotemPacket(ItemStack stack, int entityId) {
        this.stack = stack;
        this.entityId = entityId;
    }

    //播放图腾动画，声音，粒子
    @Environment(EnvType.CLIENT)
    public static void playTotem(ItemStack stack, int entityId) {
        Minecraft instance = Minecraft.getInstance();
        ClientLevel world = instance.level;

        if (world != null){
            Entity entity = world.getEntity(entityId);
            if (entity != null){
                instance.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
                instance.gameRenderer.displayItemActivation(stack);
            }
        }
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeItem(this.stack);
        buffer.writeInt(this.entityId);
    }

    @Override
    public boolean handle(Context context) {
        if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            return false;
        context.enqueueWork(() ->
                playTotem(this.stack, this.entityId) //处理服务端发送给客户端的消息
        );
        return true;
    }
}
