package committee.nova.mods.avaritia.common.item.tools.blaze;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:00
 * Version: 1.0
 */
public class BlazeSoulHoeItem extends HoeItem {
    public BlazeSoulHoeItem() {
        super(ModToolTiers.BLAZE_SWORD, 0, -2.4f,
                new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant());

    }
    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.add(Component.translatable(ChatFormatting.DARK_GRAY + "" + ChatFormatting.ITALIC + I18n.get("tooltip.skullfire_sword.desc")));
    }

//    @Override
//    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
//        var heldItem = player.getItemInHand(hand);
//        if (!level.isClientSide) {
//            List<Entity> entities = level.getEntities(player, player.getBoundingBox().deflate(10));
//            double d2 = 0;
//            if (!entities.isEmpty()) d2 = entities.get(0).getY(0.5D) - player.getY(0.5D);
//
//            FireBallEntity fireBallEntity = ModEntities.FIRE_BALL.get().create(level);
//            if (fireBallEntity != null){
//                fireBallEntity.setOwner(player);
//                fireBallEntity.setPos(player.getX(), player.getEyeY() + 0.1, player.getZ());
//                fireBallEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
//                level.addFreshEntity(fireBallEntity);
//            }
//
//        }
//        level.playSound(player, player.getOnPos(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
//        return InteractionResultHolder.success(heldItem);
//    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }
}
