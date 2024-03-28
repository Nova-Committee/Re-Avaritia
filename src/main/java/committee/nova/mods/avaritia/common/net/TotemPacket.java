package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.common.net.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * TotemPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public class TotemPacket extends IPacket<TotemPacket> {
    private ItemStack stack;
    private Entity entity;
    public TotemPacket() {

    }

    public TotemPacket(ItemStack stack, Entity entity) {
        this.stack = stack;
        this.entity = entity;
    }
    @Override
    public TotemPacket read(FriendlyByteBuf buf) {
        Minecraft instance = Minecraft.getInstance();
        var stack = buf.readItem();
        Entity entity = null;
        if (instance.level != null) {
            entity = instance.level.getEntity(buf.readInt());
        }
        return new TotemPacket(stack, entity);
    }

    @Override
    public void write(TotemPacket msg, FriendlyByteBuf buf) {
        buf.writeItemStack(stack, false);
        buf.writeInt(entity.getId());
    }

    @Override
    public void run(TotemPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> playTotem(msg.stack, msg.entity)); //处理服务端发送给客户端的消息
        });
        ctx.get().setPacketHandled(true);
    }

    //播放图腾动画，声音，粒子
    @OnlyIn(Dist.CLIENT)
    public static void playTotem(ItemStack stack, Entity entity) {
        Minecraft instance = Minecraft.getInstance();
        ClientLevel world = instance.level;
        if (world != null){
            instance.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
            world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
            instance.gameRenderer.displayItemActivation(stack);
        }
    }
}
