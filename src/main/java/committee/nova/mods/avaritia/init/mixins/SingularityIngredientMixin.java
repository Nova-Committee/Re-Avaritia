package committee.nova.mods.avaritia.init.mixins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import committee.nova.mods.avaritia.common.crafting.recipe.NbtIngredientAccess;
import committee.nova.mods.avaritia.common.crafting.recipe.NbtIngredients;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ingredient.class)
public abstract class SingularityIngredientMixin implements NbtIngredientAccess {
    @Unique private ItemStack avaritia$nbtStack;
    @Unique private Ingredient[] avaritia$alternatives;

    @Override
    public void avaritia$setNbtStack(ItemStack stack) { this.avaritia$nbtStack = stack; }
    @Override
    public void avaritia$setAlternatives(Ingredient[] alternatives) { this.avaritia$alternatives = alternatives; }

    @Inject(method = "fromJson(Lcom/google/gson/JsonElement;Z)Lnet/minecraft/world/item/crafting/Ingredient;",
            at = @At("HEAD"), cancellable = true)
    private static void avaritia$decodeJson(JsonElement json, boolean allowEmpty,
                                          CallbackInfoReturnable<Ingredient> callback) {
        Ingredient special = NbtIngredients.parseSpecial(json);
        if (special != null) callback.setReturnValue(special);
    }

    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void avaritia$matchNbt(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        if (this.avaritia$nbtStack != null) {
            callback.setReturnValue(stack != null && ItemStack.isSameItemSameTags(this.avaritia$nbtStack, stack));
        } else if (this.avaritia$alternatives != null) {
            for (Ingredient alternative : this.avaritia$alternatives) {
                if (alternative.test(stack)) {
                    callback.setReturnValue(true);
                    return;
                }
            }
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "toJson", at = @At("HEAD"), cancellable = true)
    private void avaritia$encodeJson(CallbackInfoReturnable<JsonElement> callback) {
        if (this.avaritia$nbtStack != null) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "avaritia:nbt");
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(this.avaritia$nbtStack.getItem()).toString());
            json.addProperty("count", this.avaritia$nbtStack.getCount());
            if (this.avaritia$nbtStack.hasTag()) json.addProperty("nbt", this.avaritia$nbtStack.getTag().toString());
            callback.setReturnValue(json);
        } else if (this.avaritia$alternatives != null) {
            JsonArray json = new JsonArray();
            for (Ingredient alternative : this.avaritia$alternatives) json.add(alternative.toJson());
            callback.setReturnValue(json);
        }
    }

    @Inject(method = "toNetwork", at = @At("HEAD"), cancellable = true)
    private void avaritia$encodeNetwork(FriendlyByteBuf buffer, CallbackInfo callback) {
        if (this.avaritia$nbtStack != null || this.avaritia$alternatives != null) {
            buffer.writeVarInt(-1);
            buffer.writeUtf(((Ingredient) (Object) this).toJson().toString());
            callback.cancel();
        }
    }

    @Inject(method = "fromNetwork", at = @At("HEAD"), cancellable = true)
    private static void avaritia$decodeNetwork(FriendlyByteBuf buffer, CallbackInfoReturnable<Ingredient> callback) {
        int start = buffer.readerIndex();
        if (buffer.readVarInt() == -1) {
            callback.setReturnValue(Ingredient.fromJson(JsonParser.parseString(buffer.readUtf())));
        } else {
            buffer.readerIndex(start);
        }
    }
}
