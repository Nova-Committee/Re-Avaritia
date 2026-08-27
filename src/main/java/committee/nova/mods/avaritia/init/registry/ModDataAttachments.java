package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.SpearMark;
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

    private ModDataAttachments() {
    }
}
