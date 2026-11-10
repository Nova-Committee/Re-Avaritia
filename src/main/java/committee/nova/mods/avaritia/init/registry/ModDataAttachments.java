package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.WolfArmorState;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/** Entity-owned cooldown survives world reload and armor removal. */
public final class ModDataAttachments {
    public static final AttachmentType<WolfArmorState> WOLF_ARMOR_STATE = AttachmentRegistry.createPersistent(
            Const.rl("wolf_armor_state"), WolfArmorState.CODEC.codec());

    public static void initialize() {}
    private ModDataAttachments() {}
}
