package committee.nova.mods.avaritia.common.item;

import committee.nova.mods.avaritia.api.common.item.BaseItem;
import committee.nova.mods.avaritia.common.entity.EndestPearlEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
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
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        if (!world.isClientSide) {
            EndestPearlEntity pearl = ModEntities.ENDER_PEARL.get().create(player.level());
            if (pearl != null){
                pearl.setItem(stack);
                pearl.setShooter(player);
                pearl.setPos(player.getX(), player.getEyeY() + 0.1, player.getZ());
                pearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                world.addFreshEntity(pearl);
                player.getCooldowns().addCooldown(stack.getItem(), 30);
            }
        }
        world.playSound(player, player.getOnPos(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
