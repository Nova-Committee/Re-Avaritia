package com.avaritia.init.registry;

import com.avaritia.Avaritia;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 注册模组的数据附件类型。
 *
 * <p>源端 {@code ModCaps} 为空占位类，旧 Capabilities 调用将在 Wave 5 中迁移。</p>
 */
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(Registries.ATTACHMENT_TYPE, Const.MOD_ID);
}
