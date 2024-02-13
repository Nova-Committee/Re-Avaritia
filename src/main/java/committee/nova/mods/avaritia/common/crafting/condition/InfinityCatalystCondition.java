package committee.nova.mods.avaritia.common.crafting.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import committee.nova.mods.avaritia.Static;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/7/10 12:46
 * Version: 1.0
 */
public class InfinityCatalystCondition implements ICondition {
    private static final ResourceLocation ID = new ResourceLocation(Static.MOD_ID, "infinity_catalyst_recipe");
    public static final InfinityCatalystCondition INSTANCE = new InfinityCatalystCondition();

    public static final Codec<InfinityCatalystCondition> CODEC = MapCodec.unit(INSTANCE).stable().codec();

    @Override
    public boolean test(@NotNull IContext context) {
        return true;
    }

    @Override
    public @NotNull Codec<? extends ICondition> codec() {
        return CODEC;
    }
}
