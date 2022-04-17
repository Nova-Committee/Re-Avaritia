package nova.committee.avaritia.common.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import nova.committee.avaritia.common.entity.EndestPearlEntity;
import nova.committee.avaritia.init.registry.ModEntities;
import nova.committee.avaritia.init.registry.ModTab;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 0:31
 * Version: 1.0
 */
public class EndestPearlItem extends Item {
    public EndestPearlItem() {
        super(new Properties().tab(ModTab.TAB).stacksTo(64));
        setRegistryName("endest_pearl");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (!world.isClientSide) {
            EndestPearlEntity pearl = ModEntities.EnderstPearl.get().create(world);
            if (pearl != null) {
                pearl.shoot(player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                world.addFreshEntity(pearl);
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

}
