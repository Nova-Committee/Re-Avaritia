package committee.nova.mods.avaritia.common.crafting.condition;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.Static;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/7/10 12:46
 * Version: 1.0
 */
public class InfinityCatalystCondition implements ICondition {
    private static final ResourceLocation ID = new ResourceLocation(Static.MOD_ID, "infinity_catalyst_recipe");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return true;
    }

    public static class Serializer implements IConditionSerializer<InfinityCatalystCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, InfinityCatalystCondition value) {

        }

        @Override
        public InfinityCatalystCondition read(JsonObject json) {
            return new InfinityCatalystCondition();
        }

        @Override
        public ResourceLocation getID() {
            return InfinityCatalystCondition.ID;
        }
    }
}
