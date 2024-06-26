package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.common.item.InfinityArmorItem;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.common.item.tools.*;
import committee.nova.mods.avaritia.common.net.S2CTotemPacket;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.AbilityUtils;
import committee.nova.mods.avaritia.util.lang.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:46
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InfinityHandler {

    //黑名单功能
    private static boolean isGarbageBlock(BlockState state) {
        return state.is(Tags.Blocks.COBBLESTONE) || state.is(Tags.Blocks.STONE) || state.is(Tags.Blocks.NETHERRACK);
    }


    //特殊效果（附魔）
    @SubscribeEvent
    public static void opTool(PlayerEvent.ItemCraftedEvent event) {
        ItemStack stack = event.getCrafting();
        if (stack.is(ModItems.infinity_sword.get())) {
            if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.MOB_LOOTING, stack) < 10) {
                stack.enchant(Enchantments.MOB_LOOTING, 10);
            }
        }
        if (stack.is(ModItems.infinity_pickaxe.get())) {
            if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack) < 10) {
                stack.enchant(Enchantments.BLOCK_FORTUNE, 10);
            }
        }
        if (stack.is(ModItems.infinity_bow.get())) {
            if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) < 10) {
                stack.enchant(Enchantments.INFINITY_ARROWS, 10);
            }
        }
        if (stack.is(ModItems.infinity_horse_armor.get())) {
            if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.FROST_WALKER, stack) < 10) {
                stack.enchant(Enchantments.FROST_WALKER, 10);
            }
            if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.ALL_DAMAGE_PROTECTION, stack) < 10) {
                stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 10);
            }
            if (EnchantmentHelper.getTagEnchantmentLevel(Enchantments.FALL_PROTECTION, stack) < 10) {
                stack.enchant(Enchantments.FALL_PROTECTION, 10);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerMine(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getFace() == null || event.getLevel().isClientSide || event.getItemStack().isEmpty() || event.getEntity().isCreative()) {
            return;
        }
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        if (event.getItemStack().getItem() == ModItems.infinity_pickaxe.get()) {
            if (state.getDestroySpeed(world, event.getPos()) <= -1 || state.getMapColor(world, pos) == MapColor.STONE || state.getMapColor(world, pos) == MapColor.METAL) {
                if (event.getItemStack().getOrCreateTag().getBoolean("hammer")) {
                    ModItems.infinity_pickaxe.get().onBlockStartBreak(event.getEntity().getMainHandItem(), event.getPos(), event.getEntity());
                }
            }

        }

    }

    @SubscribeEvent
    public static void digging(PlayerEvent.BreakSpeed event) {
        if (!event.getEntity().getMainHandItem().isEmpty()) {
            ItemStack held = event.getEntity().getMainHandItem();
            if (held.is(ModItems.infinity_pickaxe.get()) || held.is(ModItems.infinity_shovel.get())) {
                if (!event.getEntity().onGround()) {
                    event.setNewSpeed(event.getNewSpeed() * 5);
                }
                if (!event.getEntity().isInWater() && !EnchantmentHelper.hasAquaAffinity(event.getEntity())) {
                    event.setNewSpeed(event.getNewSpeed() * 5);
                }
                if (held.getOrCreateTag().getBoolean("hammer") || held.getOrCreateTag().getBoolean("destroyer")) {
                    event.setNewSpeed(event.getNewSpeed() * 0.5F);
                }
            }
        }
    }

    @SubscribeEvent
    public static void canHarvest(PlayerEvent.HarvestCheck event) {
        if (!event.getEntity().getMainHandItem().isEmpty()) {
            var level = event.getEntity().level();
            ItemStack held = event.getEntity().getMainHandItem();
            if (held.is(ModItems.infinity_pickaxe.get()) && event.getTargetBlock().getMapColor(level, BlockPos.ZERO) == MapColor.STONE) {
                if (held.getOrCreateTag().getBoolean("destroyer") && isGarbageBlock(event.getTargetBlock().getBlock().defaultBlockState())) {
                    event.setResult(Event.Result.ALLOW);
                }
            }
        }
    }

    //合并物质团
    @SubscribeEvent
    public static void clusterCluster(EntityItemPickupEvent event) {
        if (event.getEntity() != null && event.getItem().getItem().is(ModItems.matter_cluster.get())) {
            ItemStack stack = event.getItem().getItem();
            boolean mergedAny = false;
            Player player = event.getEntity();

            for (ItemStack slot : player.getInventory().items) {
                if (stack.isEmpty()) {
                    break;
                }
                if (slot.is(ModItems.matter_cluster.get())) {
                    mergedAny |= MatterClusterItem.mergeClusters(stack, slot);
                }
            }

            if (mergedAny) {
                player.level().playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (player.level().random.nextFloat() - player.level().random.nextFloat()) * 1.4F + 2.0F);
            }
        }
    }

    @SubscribeEvent
    public static void expCancel(ItemExpireEvent event) {
        if (event.getEntity() instanceof ImmortalItemEntity) {
            event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof InfinitySwordItem) {
            for (int x = 0; x < event.getToolTip().size(); x++) {
                if (event.getToolTip().get(x).getString().contains(I18n.get("tooltip.infinity.desc")) || event.getToolTip().get(x).getString().equals(I18n.get("attribute.name.generic.attack_damage"))) {
                    event.getToolTip().set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.infinity.desc").withStyle(ChatFormatting.BLUE)));
                    return;
                }
            }
        } else if (event.getItemStack().getItem() instanceof InfinityArmorItem) {
            for (int x = 0; x < event.getToolTip().size(); x++) {
                if (event.getToolTip().get(x).getString().contains(I18n.get("tooltip.armor.desc"))) {
                    event.getToolTip().set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.armor.desc").withStyle(ChatFormatting.BLUE)));
                    return;
                } else if (event.getToolTip().get(x).getString().contains(I18n.get("tooltip.armor_toughness.desc"))) {
                    event.getToolTip().set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.armor_toughness.desc").withStyle(ChatFormatting.BLUE)));
                    return;
                }

            }
        }
    }

    //取消身穿无尽套时的伤害
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (AbilityUtils.isInfinite(player) && !(event.getSource() instanceof ModDamageTypes.DamageSourceRandomMessages)) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth());
            }
            ItemStack totem = getPlayerTotemItem(player);
            if (!totem.isEmpty()){
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CTotemPacket(totem, player.getId()));

                player.removeAllEffects();
                int damage = totem.getUseDuration();
                if (damage == 9){ //最后一次
                    player.setHealth(player.getMaxHealth());
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 800, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 800, 1));
                    AbilityUtils.attackAOE(player, 8, 1000.0f, false);//触发无尽图腾后对附近造成伤害
                    player.displayClientMessage(Component.translatable("tooltip.avaritia.totem_break"), false);
                }else {
                    player.setHealth(10.0F);
                }
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2600, 4));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 700, 2));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1100, 0));
                totem.hurtAndBreak(1, player, e -> e.swing(InteractionHand.MAIN_HAND));
                event.setCanceled(true);
            }
        }
    }

    //取消身穿无尽套时受到的所有伤害
    @SubscribeEvent
    public static void onGetHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().is(ModItems.infinity_sword.get()) && player.getMainHandItem().useOnRelease()) {
            event.setCanceled(true);
        }
        if (AbilityUtils.isInfinite(player) && !event.getSource().is(ModDamageTypes.INFINITY)) {
            event.setCanceled(true);
        }
    }

    //取消对无尽套的伤害
    @SubscribeEvent
    public static void onAttacked(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (AbilityUtils.isInfinite(player) && !event.getSource().is(ModDamageTypes.INFINITY)) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.isRecentlyHit() &&
                event.getEntity() instanceof WitherSkeleton witherSkeleton
                && event.getSource().getEntity() instanceof Player player
        ) {
            if (player.getMainHandItem().is(ModItems.skull_sword.get()) || player.getOffhandItem().is(ModItems.skull_sword.get())) {
                if (event.getDrops().isEmpty()) {
                    addDrop(event, new ItemStack(Items.WITHER_SKELETON_SKULL, 1));
                } else {
                    int skulls = 0;

                    for (var drop : event.getDrops()){
                        ItemStack stack = drop.getItem();
                        if (stack.is(Items.WITHER_SKELETON_SKULL)) {
                            if (stack.getDamageValue() == 1) {
                                skulls++;
                            } else if (stack.getDamageValue() == 0) {
                                skulls++;
                                stack.setDamageValue(1);
                            }
                        }
                    }

                    if (skulls == 0) {
                        addDrop(event, new ItemStack(Items.WITHER_SKELETON_SKULL, 1));
                    }
                }

            }
        }
    }

    @SubscribeEvent
    public static void entityItemUnDeath(ItemEvent event) {
        ItemEntity entityItem = event.getEntity();
        Item item = entityItem.getItem().getItem();
        if (item instanceof InfinityArmorItem || item instanceof InfinityAxeItem || item instanceof InfinityBowItem ||
                item instanceof InfinityHoeItem || item instanceof InfinityShovelItem || item instanceof InfinityPickaxeItem ||
                item instanceof InfinitySwordItem) {
            entityItem.setInvulnerable(true);
        }
    }

    private static void addDrop(LivingDropsEvent event, ItemStack drop) {
        ItemEntity entity = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), drop);
        entity.setDefaultPickUpDelay();
        event.getDrops().add(entity);
    }

    /**
     * 获取玩家背包中的图腾
     * @param player 玩家
     * @return 图腾
     */
    private static ItemStack getPlayerTotemItem(Player player){
        AtomicReference<ItemStack> totemItem = new AtomicReference<>(ItemStack.EMPTY);;
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.is(ModItems.infinity_totem.get())){
            totemItem.set(mainHandItem);
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ModItems.infinity_totem.get())){
            totemItem.set(offhand);
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.infinity_totem.get()))
                totemItem.set(stack);
        }

        if (Static.isLoad("curios") && Static.isLoad("charmofundying")){
            CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                curiosInventory.getStacksHandler("charm").ifPresent(slotInventory -> {
                    if (slotInventory.getStacks().getStackInSlot(0).is(ModItems.infinity_totem.get())){
                        totemItem.set(slotInventory.getStacks().getStackInSlot(0));
                    }
                });
            });
        }

        return totemItem.get();
    }
}
