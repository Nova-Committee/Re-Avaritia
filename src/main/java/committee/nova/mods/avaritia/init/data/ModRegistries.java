package committee.nova.mods.avaritia.init.data;

import committee.nova.mods.avaritia.Const;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static committee.nova.mods.avaritia.init.registry.ModDamageTypes.DAMAGE_BUILDER;

/**
 * Name: Avaritia-forge / ModRegistries
 * @author cnlimiter
 * CreateTime: 2023/9/10 0:40
 * Description:
 */

public class ModRegistries extends DatapackBuiltinEntriesProvider {

    public ModRegistries(DataGenerator generator, CompletableFuture<HolderLookup.Provider> future) {
        super(generator.getPackOutput(), future, DAMAGE_BUILDER, Set.of("minecraft", Const.MOD_ID));
    }
}
