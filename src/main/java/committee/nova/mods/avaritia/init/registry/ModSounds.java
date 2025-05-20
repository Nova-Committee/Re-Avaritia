package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/20 17:34
 * Version: 1.0
 */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Const.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> GAPING_VOID = SOUNDS.register("gaping_void", () -> SoundEvent.createVariableRangeEvent(Const.rl("gaping_void")));
    public static final SoundType END_PORTAL = new DeferredSoundType(1.0F, 1.0F,
            () -> SoundEvents.END_PORTAL_FRAME_FILL,
            () -> SoundEvents.END_PORTAL_FRAME_FILL,
            () -> SoundEvents.END_PORTAL_FRAME_FILL,
            () -> SoundEvents.END_PORTAL_FRAME_FILL,
            () -> SoundEvents.END_PORTAL_FRAME_FILL);

}
