package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.WolfArmorState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 模组自定义的实体 / 方块实体数据附件。
 */
public final class ModDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Const.MOD_ID);

    /** 中子战狼铠的羁绊冷却，挂在狼实体上，避免脱穿铠甲刷新冷却。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WolfArmorState>> WOLF_ARMOR_STATE =
            ATTACHMENT_TYPES.register("wolf_armor_state", () -> AttachmentType.builder(() -> WolfArmorState.EMPTY)
                    .serialize(WolfArmorState.CODEC)
                    .build());

    private ModDataAttachments() {
    }
}
