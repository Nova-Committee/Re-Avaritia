//package committee.nova.mods.avaritia.common.crafting.condition;
//
//import com.google.gson.JsonObject;
//import committee.nova.mods.avaritia.Static;
//import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraftforge.common.crafting.conditions.ICondition;
//import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
//
///**
// * Description:
// * Author: cnlimiter
// * Date: 2022/7/10 12:46
// * Version: 1.0
// */
//public class InfinityCatalystCondition implements ConditionJsonProvider {
//    private static final ResourceLocation ID = new ResourceLocation(Static.MOD_ID, "infinity_catalyst_recipe");
//
//    @Override
//    public ResourceLocation getConditionId() {
//        return ID;
//    }
//
//    @Override
//    public void writeParameters(JsonObject object) {
//
//    }
//
//    public static class Serializer implements Condition<InfinityCatalystCondition> {
//        public static final Serializer INSTANCE = new Serializer();
//
//        @Override
//        public void write(JsonObject json, InfinityCatalystCondition value) {
//
//        }
//
//        @Override
//        public InfinityCatalystCondition read(JsonObject json) {
//            return new InfinityCatalystCondition();
//        }
//
//        @Override
//        public ResourceLocation getID() {
//            return InfinityCatalystCondition.ID;
//        }
//    }
//}
