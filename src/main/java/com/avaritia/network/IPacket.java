package com.avaritia.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/4/21 23:41
 * @Description: 
 */
public interface IPacket extends CustomPacketPayload {
    void handle(IPayloadContext context);
}
