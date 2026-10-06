package committee.nova.mods.avaritia.init.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntries;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/20 17:34
 * Version: 1.0
 */
public class ModSounds {
    public static final RegistryEntries<SoundEvent> SOUNDS = RegistryEntries.create(BuiltInRegistries.SOUND_EVENT, Const.MOD_ID);

    public static final SoundEvent GAPING_VOID = SoundEvent.createVariableRangeEvent(new ResourceLocation("avaritia:gaping_void"));
    public static final SoundType END_PORTAL = new SoundType(1.0F, 1.0F,
            SoundEvents.END_PORTAL_FRAME_FILL, SoundEvents.END_PORTAL_FRAME_FILL,
            SoundEvents.END_PORTAL_FRAME_FILL, SoundEvents.END_PORTAL_FRAME_FILL,
            SoundEvents.END_PORTAL_FRAME_FILL);
    static { SOUNDS.register("gaping_void", () -> GAPING_VOID); }

}
