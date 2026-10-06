package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.block.ResourceBlock;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentTableBlock.class)
public abstract class GameplayEnchantmentTableBlockMixin {
    @Redirect(method = "isValidBookShelf", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private static boolean avaritia$resourcePowerProvider(BlockState state, TagKey<Block> tag) {
        return state.getBlock() instanceof ResourceBlock || state.is(tag);
    }
}
