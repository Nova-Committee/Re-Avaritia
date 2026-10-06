package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.handler.SkullDropTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class GameplayEntityLootMixin {
    @Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("RETURN"))
    private void avaritia$trackSkull(ItemStack stack, float offset, CallbackInfoReturnable<ItemEntity> cir) {
        if (cir.getReturnValue() != null && stack.is(Items.WITHER_SKELETON_SKULL) && (Object) this instanceof SkullDropTracker tracker) tracker.avaritia$recordSkullDrop();
    }
}
