package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.registry.ModSounds;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import io.github.fabricators_of_create.porting_lib.data.SoundDefinitionsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.PackOutput;

/**
 * Author cnlimiter
 * CreateTime 2023/6/18 0:06
 * Name ModSoundDefinitions
 * Description
 */

public class ModSoundDefinitions extends SoundDefinitionsProvider {

    public ModSoundDefinitions(FabricDataOutput output, ExistingFileHelper helper) {
        super(output, Static.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        this.add(ModSounds.GAPING_VOID, definition().with(
                sound("avaritia:gaping_void").stream()
        ));
    }
}
