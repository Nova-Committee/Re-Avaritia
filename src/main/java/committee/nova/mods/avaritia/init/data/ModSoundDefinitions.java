package committee.nova.mods.avaritia.init.data;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.registry.ModSounds;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

/**
 * Author cnlimiter
 * CreateTime 2023/6/18 0:06
 * Name ModSoundDefinitions
 * Description
 */

public class ModSoundDefinitions extends SoundDefinitionsProvider {

    public ModSoundDefinitions(DataGenerator output, ExistingFileHelper helper) {
        super(output.getPackOutput(), Static.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        this.add(ModSounds.GAPING_VOID, definition().with(
                sound("avaritia:gaping_void").stream()
        ));
    }
}
