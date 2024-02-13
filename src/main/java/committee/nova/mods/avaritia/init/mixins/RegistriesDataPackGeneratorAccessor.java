package committee.nova.mods.avaritia.init.mixins;



import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;
/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2024/2/12 19:23
 * @Description:
 */
@SuppressWarnings("deprecation")
@Mixin(RegistriesDatapackGenerator.class)
public interface RegistriesDataPackGeneratorAccessor {

    @Accessor("registries")
    CompletableFuture<HolderLookup.Provider> getRegistries();
}
