package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.api.common.item.BaseItem;
import committee.nova.mods.avaritia.common.entity.EndestPearlEntity;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 0:31
 * Version: 1.0
 */
public class EndestPearlItem extends BaseItem implements ProjectileItem {
    public EndestPearlItem() {
        super(properties -> properties
                .rarity(ModRarities.EPIC)
                .stacksTo(16)
        );
    }
    @Override
    public @NotNull Projectile asProjectile(@NotNull Level level, Position pos, @NotNull ItemStack stack, @NotNull Direction direction) {
        EndestPearlEntity arrow = new EndestPearlEntity(level, pos.x(), pos.y(), pos.z());
        arrow.setItem(stack);
        return arrow;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        ToolUtils.pearlAttack(player, stack, world);
        return InteractionResult.SUCCESS;
    }
}
