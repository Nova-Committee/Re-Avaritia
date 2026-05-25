package com.avaritia;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

/**
 * Avaritia — 无尽物品模组。
 * <p>
 * 主模组类，使用 NeoForge {@link Mod} 注解注册。
 * 初始化逻辑将在后续开发阶段中逐步添加。
 */
@Mod(Avaritia.MOD_ID)
public class Avaritia {

    public static final String MOD_ID = "avaritia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @SuppressWarnings("unused")
    public Avaritia() {
        // 初始化将在后续开发阶段中添加
    }
}
