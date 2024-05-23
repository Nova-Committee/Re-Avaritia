package committee.nova.mods.avaritia.api.common.net;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Author cnlimiter
 * CreateTime 2023/9/17 19:08
 * Name Packet
 * Description
 */

public interface Packet {
    void encode(FriendlyByteBuf buf);
}
