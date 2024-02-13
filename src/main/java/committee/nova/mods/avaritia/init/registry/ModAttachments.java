package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.level.LevelDamage;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2024/2/12 14:30
 * @Description:
 */

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Static.MOD_ID);
    // Level Attachments
    public static final ResourceLocation DAMAGE_HANDLER = new ResourceLocation(Static.MOD_ID, "damage_handler");
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LevelDamage>> LEVEL_DAMAGE = ATTACHMENT_TYPES.register(DAMAGE_HANDLER.getPath(), () -> AttachmentType.builder(new LevelDamage.Factory()).build());

}
