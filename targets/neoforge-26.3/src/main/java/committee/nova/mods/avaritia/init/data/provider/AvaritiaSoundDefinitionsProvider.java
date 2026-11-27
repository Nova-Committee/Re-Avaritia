package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AvaritiaSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public AvaritiaSoundDefinitionsProvider(PackOutput output) {
        super(output, Const.MOD_ID);
    }

    @Override
    public void registerSounds() {
        addSound(ModSounds.GAPING_VOID, "gaping_void");
        addSound(ModSounds.HEAL, "heal");
        addSound(ModSounds.MODE, "mode");
    }

    private void addSound(DeferredHolder<SoundEvent, SoundEvent> soundEvent, String path) {
        add(soundEvent, SoundDefinition.definition().with(sound(Const.rl(path))));
    }
}
