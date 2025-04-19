package committee.nova.mods.avaritia.init.data;

import committee.nova.mods.avaritia.Static;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static committee.nova.mods.avaritia.init.registry.ModDamageTypes.DAMAGE_BUILDER;

/**
 * Name: Avaritia-forge / ModRegistries
 * Author: cnlimiter
 * CreateTime: 2023/9/10 0:40
 * Description:
 */

public class ModRegistries extends DatapackBuiltinEntriesProvider {

    public ModRegistries(PackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future, DAMAGE_BUILDER, Set.of("minecraft", Static.MOD_ID));
    }
}
