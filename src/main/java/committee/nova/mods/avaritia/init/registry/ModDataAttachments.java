package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.HarnessOwner;
import committee.nova.mods.avaritia.common.component.HarnessShieldState;
import committee.nova.mods.avaritia.common.component.SpearMark;
import committee.nova.mods.avaritia.common.component.WolfArmorState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Const.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SpearMark>> SPEAR_MARK =
            ATTACHMENT_TYPES.register("spear_mark", () -> AttachmentType.builder(() -> SpearMark.EMPTY)
                    .serialize(SpearMark.CODEC)
                    .sync(SpearMark.STREAM_CODEC)
                    .build());

    /** 中子战狼铠的羁绊 / 图腾冷却，挂在狼实体上，避免脱穿铠甲重置冷却。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WolfArmorState>> WOLF_ARMOR_STATE =
            ATTACHMENT_TYPES.register("wolf_armor_state", () -> AttachmentType.builder(() -> WolfArmorState.EMPTY)
                    .serialize(WolfArmorState.CODEC)
                    .build());

    /** 中子战魂铠的立体防御状态，挂在玩家身上。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HarnessShieldState>> HARNESS_SHIELD =
            ATTACHMENT_TYPES.register("harness_shield", () -> AttachmentType.builder(() -> HarnessShieldState.EMPTY)
                    .serialize(HarnessShieldState.CODEC)
                    .build());

    /** 中子战魂铠的善良恶魂归属，挂在恶魂实体上。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HarnessOwner>> HARNESS_OWNER =
            ATTACHMENT_TYPES.register("harness_owner", () -> AttachmentType.builder(() -> HarnessOwner.EMPTY)
                    .serialize(HarnessOwner.CODEC)
                    .build());

    private ModDataAttachments() {
    }
}
