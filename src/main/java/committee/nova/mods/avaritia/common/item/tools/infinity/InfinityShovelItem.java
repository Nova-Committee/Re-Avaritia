package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/5/15 16:33
 * Version: 1.0
 */
public class InfinityShovelItem extends Item implements ISwitchable, IUndamageable {
    private static final ToolMaterial INFINITY_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 9999, 100.0F, 100.0F, 9999,
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("avaritia", "infinity_tool_materials")));

    public InfinityShovelItem() {
        super(ModItems.properties()
                .rarity(ModRarities.COSMIC)
                .stacksTo(1)
                .fireResistant()
                .shovel(INFINITY_MATERIAL, 0, INFINITY_MATERIAL.speed()));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        if (isActive(stack, "infinity_shovel_destroyer")) {
            return 5.0F;
        }
        return Math.max(super.getDestroySpeed(stack, state), 6.0f);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level pLevel, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(pLevel, player, hand, "infinity_shovel_destroyer");
            return InteractionResult.SUCCESS;
        }

        //右键发射发射终望珍珠,冷却20s
        if (isActive(stack, "infinity_shovel_destroyer")) {
            ToolUtils.pearlAttack(player, ModItems.endest_pearl.get().getDefaultInstance(), pLevel);//
            player.getCooldowns().addCooldown(stack, 200);
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miningEntity) {
        if (miningEntity instanceof ServerPlayer player && isActive(stack, "infinity_shovel_destroyer")) {
            ToolUtils.destroyShovelBlocks(player, pos, ModConfig.shovelBreakRange.get());
        }
        return false;
    }
}
