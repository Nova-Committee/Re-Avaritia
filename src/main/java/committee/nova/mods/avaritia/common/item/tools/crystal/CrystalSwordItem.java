package committee.nova.mods.avaritia.common.item.tools.crystal;

import committee.nova.mods.avaritia.init.registry.ModItems;

import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import committee.nova.mods.avaritia.init.registry.modes.ToolMode;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class CrystalSwordItem extends Item implements ITooltip, ISwitchable {
    public CrystalSwordItem() {
        super(ModItems.properties()
                        .component(ModDataComponents.TOOL_MODE, ToolMode.DEFAULT)
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
                        .sword(ModToolTiers.CRYSTAL, 0, ModToolTiers.BLAZE.speed())
        );
    }

    @Override
    public boolean hasDescTooltip() {
        return true;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean onLeftClickEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity entity) {
        if (player instanceof ServerPlayer serverPlayer) {

            serverPlayer.resetAttackStrengthTicker();
        }
        entity.setInvulnerable(false);

        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            switchMode(world, player, hand, "blade_slash");
            return InteractionResult.SUCCESS;
        }
        if (isActive(stack, "blade_slash")) ToolUtils.shootBladeSlash(stack, player);
        return super.use(world, player, hand);
    }
}
