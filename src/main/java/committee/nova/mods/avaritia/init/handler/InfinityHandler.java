package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.api.init.event.EntityItemPickupEvent;
import committee.nova.mods.avaritia.api.init.event.PlayerHarvestCheckEvent;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.common.item.ArmorInfinityItem;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.common.item.tools.*;
import committee.nova.mods.avaritia.common.net.TotemPacket;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.AbilityUtil;
import committee.nova.mods.avaritia.util.ToolUtil;
import committee.nova.mods.avaritia.util.lang.TextUtil;
import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.*;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingHurtEvent;
import io.github.fabricators_of_create.porting_lib.event.common.BlockEvents;
import io.github.fabricators_of_create.porting_lib.event.common.ItemCraftedCallback;
import io.github.fabricators_of_create.porting_lib.tags.Tags;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:46
 * Version: 1.0
 */
public class InfinityHandler {

    public static void init(){
        opTool();
        onAttacked();
        onDeath();
        onGetHurt();
        onLivingDrops();
        onPlayerMine();
        onTooltip();
        handleExtraLuck();
        digging();
        canHarvest();
        entityItemUnDeath();
    }

    public static void clientInit(){
        onTooltip();
    }



    @Environment(EnvType.CLIENT)
    public static void onTooltip() {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (stack.getItem() instanceof InfinitySwordItem) {
                for (int x = 0; x < lines.size(); x++) {
                    if (lines.get(x).getString().contains(I18n.get("tooltip.infinity.desc")) || lines.get(x).getString().equals(I18n.get("attribute.name.generic.attack_damage"))) {
                        lines.set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtil.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.infinity.desc").withStyle(ChatFormatting.BLUE)));
                        return;
                    }
                }
            } else if (stack.getItem() instanceof ArmorInfinityItem) {
                for (int x = 0; x < lines.size(); x++) {
                    if (lines.get(x).getString().contains(I18n.get("tooltip.armor.desc"))) {
                        lines.set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtil.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.armor.desc").withStyle(ChatFormatting.BLUE)));
                        return;
                    } else if (lines.get(x).getString().contains(I18n.get("tooltip.armor_toughness.desc"))) {
                        lines.set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtil.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.armor_toughness.desc").withStyle(ChatFormatting.BLUE)));
                        return;
                    }

                }
            }
        });

    }

    //特殊效果（附魔）
    public static void opTool() {
        ItemCraftedCallback.EVENT.register((player, stack, container) -> {
            if (stack.is(ModItems.infinity_sword.get())) {
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, stack) < 10) {
                    stack.enchant(Enchantments.MOB_LOOTING, 10);
                }
            }
            if (stack.is(ModItems.infinity_pickaxe.get())) {
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack) < 10) {
                    stack.enchant(Enchantments.BLOCK_FORTUNE, 10);
                }
            }
            if (stack.is(ModItems.infinity_bow.get())) {
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) < 10) {
                    stack.enchant(Enchantments.INFINITY_ARROWS, 10);
                }
            }
            if (stack.is(ModItems.infinity_horse_armor.get())) {
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FROST_WALKER, stack) < 10) {
                    stack.enchant(Enchantments.FROST_WALKER, 10);
                }
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.ALL_DAMAGE_PROTECTION, stack) < 10) {
                    stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 10);
                }
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FALL_PROTECTION, stack) < 10) {
                    stack.enchant(Enchantments.FALL_PROTECTION, 10);
                }
            }
        });

    }

    public static void onPlayerMine() {
        PlayerInteractionEvents.LeftClickBlock.LEFT_CLICK_BLOCK.register(event -> {
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
        });
    }

    //给稿子添加时运
    public static void handleExtraLuck() {
        BlockEvents.BreakEvent.BLOCK_BREAK.register(event -> {
            if (event.getPlayer() == null) {
                return;
            }
            ItemStack mainHand = event.getPlayer().getMainHandItem();

            if (!mainHand.isEmpty() && mainHand.is(ModItems.infinity_pickaxe.get())) {
                applyLuck(event, 4);
            }
        });
    }

    public static void digging() {
        PlayerEvents.BreakSpeed.BREAK_SPEED.register(event -> {
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
        });
    }

    public static void canHarvest() {
        PlayerHarvestCheckEvent.HARVEST_CHECK.register(event -> {
            if (!event.getEntity().getMainHandItem().isEmpty()) {
                var level = event.getEntity().level();
                ItemStack held = event.getEntity().getMainHandItem();
                if (held.is(ModItems.infinity_pickaxe.get()) && event.getTargetBlock().getMapColor(level, BlockPos.ZERO) == MapColor.STONE) {
                    if (held.getOrCreateTag().getBoolean("destroyer") && isGarbageBlock(event.getTargetBlock().getBlock().defaultBlockState())) {
                        event.setResult(BaseEvent.Result.ALLOW);
                    }
                }
            }
        });
    }

    //合并物质团
    public static void clusterCluster() {
        EntityItemPickupEvent.ENTITY_ITEM_PICKUP.register(event -> {
            if (event.getEntity() != null && event.getItem().getItem().is(ModItems.matter_cluster.get())) {
                ItemStack stack = event.getItem().getItem();
                Player player = event.getEntity();

                for (ItemStack slot : player.getInventory().items) {
                    if (stack.isEmpty()) {
                        break;
                    }
                    if (slot.is(ModItems.matter_cluster.get())) {
                        MatterClusterItem.mergeClusters(stack, slot);
                    }
                }
            }
            return true;
        });
    }

    public static void entityItemUnDeath() {
        EntityEvents.ON_JOIN_WORLD.register((entity, world, loadedFromDisk) -> {
            if (entity instanceof ItemEntity entityItem) {
                Item item = entityItem.getItem().getItem();
                if (item instanceof ArmorInfinityItem || item instanceof InfinityAxeItem || item instanceof InfinityBowItem ||
                        item instanceof InfinityHoeItem || item instanceof InfinityShovelItem || item instanceof InfinityPickaxeItem ||
                        item instanceof InfinitySwordItem) {
                    entityItem.setInvulnerable(true);
                    return false;
                }
            }
            if (entity instanceof ImmortalItemEntity immortalItem) {
                immortalItem.age = Integer.MAX_VALUE;
                return false;
            }
            return true;
        });

    }


    //取消身穿无尽套时的伤害
    public static void onDeath() {
        LivingDeathEvent.DEATH.register(event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                if (AbilityUtil.isInfinite(player) && !(event.getSource() instanceof ModDamageTypes.DamageSourceRandomMessages)) {
                    event.setCanceled(true);
                    player.setHealth(player.getMaxHealth());
                }
                ItemStack totem = getPlayerBagItem(player);
                if (!totem.isEmpty()){
                    NetworkHandler.getChannel().sendToClient(new TotemPacket(totem, player.getId()), player);

                    player.removeAllEffects();
                    int damage = totem.getUseDuration();
                    if (damage == 9){ //最后一次
                        player.setHealth(player.getMaxHealth());
                        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 800, 1));
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 800, 1));
                        AbilityUtil.attackAOE(player, 8, 1000.0f, false);
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
        });

    }

    //取消身穿无尽套时受到的所有伤害
    public static void onGetHurt() {
        LivingHurtEvent.HURT.register(event -> {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }
            if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().is(ModItems.infinity_sword.get()) && player.getMainHandItem().useOnRelease()) {
                event.setCanceled(true);
            }
            if (AbilityUtil.isInfinite(player) && !event.getSource().is(ModDamageTypes.INFINITY)) {
                event.setCanceled(true);
            }
        });
    }

    //取消对无尽套的伤害
    public static void onAttacked() {
        LivingAttackEvent.ATTACK.register(event -> {
            if (!(event.getEntity() instanceof Player player)) {
                return;
            }
            if (AbilityUtil.isInfinite(player) && !event.getSource().is(ModDamageTypes.INFINITY)) {
                event.setCanceled(true);
            }
        });
    }


    public static void onLivingDrops() {
        LivingEntityEvents.DROPS.register((entity, source, drops, lootingLevel, recentlyHit) -> {
            if (recentlyHit &&
                    entity instanceof WitherSkeleton witherSkeleton
                    && source.getEntity() instanceof Player player
            ) {
                if (player.getMainHandItem().is(ModItems.skull_sword.get()) || player.getOffhandItem().is(ModItems.skull_sword.get())) {
                    if (drops.isEmpty()) {
                        addDrop(entity, drops, new ItemStack(Items.WITHER_SKELETON_SKULL, 1));
                        return true;
                    } else {
                        int skulls = 0;

                        for (var drop : drops){
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
                            addDrop(entity, drops, new ItemStack(Items.WITHER_SKELETON_SKULL, 1));
                            return true;
                        }
                    }

                }
            }
            return false;
        });
    }

    //黑名单功能
    private static boolean isGarbageBlock(BlockState state) {
        return state.is(Tags.Blocks.COBBLESTONE) || state.is(Tags.Blocks.STONE) || state.is(Tags.Blocks.NETHERRACK);
    }

    public static void applyLuck(BlockEvents.BreakEvent event, int multiplier) {
        if (ToolUtil.canUseTool(event.getState(), ToolUtil.materialsPick)) {
            LootParams.Builder lootcontext$builder = (new LootParams.Builder((ServerLevel) event.getPlayer().level())).withLuck(event.getPlayer().level().random.nextFloat()).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(event.getPos())).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, event.getPlayer().level().getBlockEntity(event.getPos()));
            List<ItemStack> drops = event.getState().getDrops(lootcontext$builder);
            for (ItemStack drop : drops) {
                if (!drop.is(event.getState().getBlock().asItem()) && !(drop.getItem() instanceof BlockItem)) {
                    drop.setCount(Math.min(drop.getCount() * multiplier, drop.getMaxStackSize()));
                }
            }

        }
    }

    private static void addDrop(LivingEntity livingEntity, Collection<ItemEntity> drops, ItemStack drop) {
        ItemEntity entity = new ItemEntity(livingEntity.level(), livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), drop);
        entity.setDefaultPickUpDelay();
        drops.add(entity);
    }

    /**
     * 获取玩家背包中的图腾
     * @param player 玩家
     * @return 图腾
     */
    private static ItemStack getPlayerBagItem(Player player){
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.is(ModItems.infinity_totem.get())){
            return mainHandItem;
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ModItems.infinity_totem.get())){
            return offhand;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.infinity_totem.get()))
                return stack;
        }

        return ItemStack.EMPTY;
    }
}
