package committee.nova.mods.avaritia.init.data.provider;

import committee.nova.mods.avaritia.api.init.data.IBlockStateProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Author cnlimiter
 * CreateTime 2023/6/16 22:22
 * Name ModBlockStates
 * Description
 */

public class ModBlockStates extends IBlockStateProvider {
    public ModBlockStates(PackOutput output, ExistingFileHelper helper) {
        super(output, helper);
    }


    @Override
    public @NotNull String getName() {
        return "Avaritia Block States";
    }

    @Override
    protected void registerStatesAndModels() {
    }
}
