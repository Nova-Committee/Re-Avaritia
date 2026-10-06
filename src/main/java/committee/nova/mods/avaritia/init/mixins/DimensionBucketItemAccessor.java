package committee.nova.mods.avaritia.init.mixins;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BucketItem.class)
public interface DimensionBucketItemAccessor {
    @Accessor("content")
    Fluid avaritia$getContent();
}
