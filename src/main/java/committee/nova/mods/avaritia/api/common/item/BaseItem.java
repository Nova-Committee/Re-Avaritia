package committee.nova.mods.avaritia.api.common.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Author cnlimiter
 * CreateTime 2023/6/14 18:22
 * Name BaseItem
 * Description
 */

public class BaseItem extends Item {
    public BaseItem() {
        super(new Properties());
    }

    public BaseItem(Function<Properties, Properties> properties) {
        super(properties.apply(new Properties()));
    }

    public @NotNull InteractionResultHolder<ItemStack> rightClick(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        if (pPlayer.isCrouching()) return rightClick(pLevel, pPlayer, pUsedHand);
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}