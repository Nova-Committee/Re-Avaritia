package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.api.common.item.BaseItem;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/3 0:31
 * Version: 1.0
 */
public class EndestPearlItem extends BaseItem {
    public EndestPearlItem() {
        super(properties -> properties
                .rarity(ModRarities.EPIC)
                .stacksTo(16)
        );
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        ToolUtils.pearlAttack(player, stack, world);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
