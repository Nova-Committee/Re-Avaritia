package committee.nova.mods.avaritia.api.common.item;

import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
        super(ModItems.properties());
    }

    public BaseItem(Function<Properties, Properties> properties) {
        super(properties.apply(ModItems.properties()));
    }

    public @NotNull InteractionResult rightClick(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        if (pPlayer.isCrouching()) return rightClick(pLevel, pPlayer, pUsedHand);
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
