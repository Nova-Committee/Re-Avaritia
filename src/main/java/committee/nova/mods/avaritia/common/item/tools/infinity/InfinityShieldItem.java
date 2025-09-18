package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class InfinityShieldItem extends ShieldItem implements IUndamageable {
    public InfinityShieldItem() {
        super((new Item.Properties())
                .rarity(ModRarities.COSMIC)
                .stacksTo(1)
                .fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (player.isUsingItem() && player.getUsedItemHand() == hand) {

            player.releaseUsingItem();
            return InteractionResultHolder.success(itemstack);
        } else {

            player.startUsingItem(hand);


            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {

                for (Entity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4.0D))) {
                    if (entity instanceof Player attacker && entity != player) {

                        if (attacker.getLastHurtMob() == player && attacker.getAttackStrengthScale(0.0F) > 0.5F) {

                            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                                    player.getX(),
                                    player.getY() + player.getBbHeight() / 2,
                                    player.getZ(),
                                    10,
                                    0.5D, 0.5D, 0.5D, 0.1D);
                        }
                    }
                }
            }

            return InteractionResultHolder.consume(itemstack);
        }
    }
    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack attackerStack, LivingEntity attacker, LivingEntity blocker) {
        return false;
    }
}
