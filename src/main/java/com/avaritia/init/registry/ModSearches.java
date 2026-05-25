package com.avaritia.init.registry;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 注册模组客户端使用的自定义搜索树。
 * <p>
 * 1.21.1 源文件当前没有实际搜索项，迁移时保留空客户端初始化入口，供后续屏幕迁移继续调用。
 */
public class ModSearches {

    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup() {
    }
}
