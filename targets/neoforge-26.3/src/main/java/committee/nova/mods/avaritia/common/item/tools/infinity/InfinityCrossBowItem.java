package committee.nova.mods.avaritia.common.item.tools.infinity;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.IUndamageable;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.api.iface.transform.IBowTransform;
import committee.nova.mods.avaritia.common.entity.arrow.HeavenArrowEntity;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.util.ProjectileItemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class InfinityCrossBowItem extends CrossbowItem implements InitEnchantItem, ISwitchable, IUndamageable, IBowTransform {
    private static final float[] MULTI_SHOT_ANGLES = {-20.0F, -10.0F, 0.0F, 10.0F, 20.0F};

    private final InitEnchantment initEnchantment;

    public InfinityCrossBowItem() {
        super(ModItems.properties()
                .stacksTo(1)
                .rarity(ModRarities.COSMIC.getValue())
                .fireResistant());
        this.initEnchantment = new InitEnchantment(Enchantments.INFINITY, 10);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NonNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            switchMode(level, player, hand, "infinity_crossbow_multi");
            return InteractionResult.SUCCESS;
        }

        ChargedProjectiles chargedProjectiles = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
            performShooting(level, player, stack);
            stack.remove(DataComponents.CHARGED_PROJECTILES);
            return InteractionResult.CONSUME;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(@NotNull ItemStack stack, @NotNull Level level,
                                @NotNull LivingEntity entity, int timeLeft) {
        int useTicks = this.getUseDuration(stack, entity) - timeLeft;
        float charge = (float) useTicks / (float) CrossbowItem.getChargeDuration(stack, entity);
        if (charge < 1.0F || CrossbowItem.isCharged(stack)) {
            return false;
        }

        if (!level.isClientSide() && entity instanceof Player player) {
            loadInfinityProjectile(stack, player);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 1.0F,
                    1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
        return true;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return CrossbowItem.getChargeDuration(stack, entity) + 3;
    }

    private void loadInfinityProjectile(ItemStack crossbow, Player player) {
        ItemStack ammo = findAmmo(player);
        ItemStack projectile = ammo.isEmpty() ? new ItemStack(Items.ARROW) : ProjectileItemUtils.copySingle(ammo);
        crossbow.set(DataComponents.CHARGED_PROJECTILES,
                ChargedProjectiles.of(ItemStackTemplate.fromNonEmptyStack(projectile)));
    }

    private void performShooting(Level level, Player player, ItemStack crossbow) {
        if (level.isClientSide()) {
            return;
        }

        ItemStack ammo = findAmmo(player);
        boolean multiShot = isActive(crossbow, "infinity_crossbow_multi");
        int projectileCount = multiShot ? MULTI_SHOT_ANGLES.length : 1;
        for (int i = 0; i < projectileCount; i++) {
            float angle = multiShot ? MULTI_SHOT_ANGLES[i] : 0.0F;
            if (!ammo.isEmpty() && shootAmmo(level, player, ammo, angle)) {
                continue;
            }
            shootInfinityArrow(level, player, angle);
        }
        player.getCooldowns().addCooldown(crossbow, multiShot ? 200 : 20);
    }

    private boolean shootAmmo(Level level, Player player, ItemStack ammo, float angle) {
        ProjectileItemUtils.LaunchProjectile launch = ProjectileItemUtils.createLaunchProjectile(level, player, ammo);
        if (launch == null) {
            return false;
        }
        launch.shootFromRotation(player, angle);
        level.addFreshEntity(launch.entity());
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                launch.sound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private ItemStack findAmmo(Player player) {
        ItemStack offhandItem = player.getOffhandItem();
        return ProjectileItemUtils.isLaunchableProjectileItem(offhandItem) ? offhandItem : ItemStack.EMPTY;
    }

    private void shootInfinityArrow(Level level, Player player, float angle) {
        HeavenArrowEntity arrow = new HeavenArrowEntity(level, player);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + angle, 0.0F, 3.0F, 1.0F);
        level.addFreshEntity(arrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantment) {
        return this.initEnchantment.getLevel(enchantment);
    }
}
