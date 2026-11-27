package committee.nova.mods.avaritia.api.utils.text;



import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/24 19:43
 * @apiNote From Mekanism
 */
public interface IHasTranslationKey {

    String getTranslationKey();

    interface IHasEnumNameTranslationKey extends IHasTranslationKey, TranslatableEnum {

        @NotNull
        @Override
        default Component getTranslatedName() {
            return Component.translatable(getTranslationKey());
        }
    }
}
