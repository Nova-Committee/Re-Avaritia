package com.avaritia.init.registry;

/**
 * 兼容旧源码中的 ModEntities 命名。
 *
 * <p>目标工程已将实体注册类迁移为 {@link ModEntityTypes}，本类只继承静态字段，避免批量移植文件继续引用旧类名时报错。</p>
 */
public final class ModEntities extends ModEntityTypes {
    private ModEntities() {
    }
}
