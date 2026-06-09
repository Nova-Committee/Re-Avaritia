package com.avaritia.init.handler;

import com.avaritia.Const;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * @author cnlimiter
 */
@EventBusSubscriber(modid = Const.MOD_ID)
public class PackResourceHandler {
    @SubscribeEvent
    public static void addPackFinders(final AddPackFindersEvent event) {
        // 内置资源包位于 jar 的 resources/ 目录下，必须交给 NeoForge 的 helper 读取 jar 内容。
        // 直接把 jar 内路径当成普通 Path 会导致打包启动时读取不到 pack.mcmeta，并向仓库提交 null pack。
        event.addPackFinders(
                Const.rl("resourcepacks/avaritia"),
                PackType.CLIENT_RESOURCES,
                Component.translatable("title.avaritia.resourcepack"),
                PackSource.BUILT_IN,
                false,
                Pack.Position.TOP
        );
    }
}
