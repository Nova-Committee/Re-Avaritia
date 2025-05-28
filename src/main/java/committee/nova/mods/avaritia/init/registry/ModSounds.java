package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import moze_intel.projecte.gameObjs.registration.impl.SoundEventRegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2022/3/31 11:37
 * @Description:
 */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Const.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> GAPING_VOID = registerSound("gaping_void");
    public static final DeferredHolder<SoundEvent, SoundEvent> HEAL = registerSound("heal");
    public static final DeferredHolder<SoundEvent, SoundEvent> MODE = registerSound("mode");
    public static final SoundType END_PORTAL = new DeferredSoundType(1.0F, 1.0F,
            () -> SoundEvents.END_PORTAL_FRAME_FILL,
            () -> SoundEvents.END_PORTAL_FRAME_FILL,
            () -> SoundEvents.END_PORTAL_FRAME_FILL,
            () -> SoundEvents.END_PORTAL_FRAME_FILL,
            () -> SoundEvents.END_PORTAL_FRAME_FILL);


    public static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Const.rl(name)));
    }
}
