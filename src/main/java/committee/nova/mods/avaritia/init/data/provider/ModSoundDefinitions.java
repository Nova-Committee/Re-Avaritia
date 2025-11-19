package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Author cnlimiter
 * CreateTime 2023/6/18 0:06
 * Name ModSoundDefinitions
 * Description
 */

public class ModSoundDefinitions extends SoundDefinitionsProvider {

    public ModSoundDefinitions(PackOutput output, ExistingFileHelper helper) {
        super(output, Const.MOD_ID, helper);
    }

    protected void addSoundEvent(DeferredHolder<SoundEvent, SoundEvent> soundEventRO, ResourceLocation location) {
        add(soundEventRO.get(), SoundDefinition.definition().with(sound(location)));
    }
    @Override
    public void registerSounds() {
        addSoundEvent(ModSounds.GAPING_VOID, Const.rl("gaping_void"));
        //addSoundEvent(ModSounds.HEAL, Const.rl("heal"));
        //addSoundEvent(ModSounds.MODE, Const.rl("mode"));
    }
}
