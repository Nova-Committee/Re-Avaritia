package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.common.item.ArmorInfinityItem;
import committee.nova.mods.avaritia.common.item.MatterClusterItem;
import committee.nova.mods.avaritia.common.item.tools.*;
import committee.nova.mods.avaritia.common.net.TotemPacket;
import committee.nova.mods.avaritia.init.event.MatterCollectEvent;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.AbilityUtil;
import committee.nova.mods.avaritia.util.lang.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
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
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:46
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InfinityHandler {
    private static final Map<DimensionType, List<MatterCollectEvent>> crawlerTasks = new HashMap<>();
    private static boolean doItemCapture = false;
    private static final Set<ItemStack> capturedDrops = new LinkedHashSet<>();

    public static boolean isInfiniteChestPlate(LivingEntity player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        return !stack.isEmpty() && stack.getItem() instanceof ArmorInfinityItem;
    }

    public static void enableItemCapture() {
        doItemCapture = true;
    }

    public static void stopItemCapture() {
        doItemCapture = false;
    }

    public static boolean isItemCaptureEnabled() {
        return doItemCapture;
    }

    public static Set<ItemStack> getCapturedDrops() {
        Set<ItemStack> dropsCopy = new LinkedHashSet<>(capturedDrops);
        capturedDrops.clear();
        return dropsCopy;
    }

    //黑名单功能
    private static boolean isGarbageBlock(Block block) {
        //Static.LOGGER.info(TagCollectionManager.getInstance().getBlocks().getAllTags().keySet());
        for (TagKey<Block> id : block.defaultBlockState().getTags().toList()) {
            ResourceLocation block_main = id.registry().registry();
            String ore = block_main.getPath();
            if (ore.contains("cobblestone") || ore.contains("stone") || ore.contains("netherrack")) {
                return true;
            }
        }
        return false;
    }

    public static void startCrawlerTask(Level world, Player player, ItemStack stack, BlockPos coords, int steps, boolean leaves, boolean force, Set<BlockPos> posChecked) {
        MatterCollectEvent swapper = new MatterCollectEvent(world, player, stack, coords, steps, leaves, force, posChecked);
        DimensionType dim = world.dimensionType();
        if (!crawlerTasks.containsKey(dim)) {
            crawlerTasks.put(dim, new ArrayList<>());
        }
        crawlerTasks.get(dim).add(swapper);
        //return swapper;
    }

    public static void applyLuck(BlockEvent.BreakEvent event, int multiplier) {
        if (event.getState().getMapColor(event.getLevel(), event.getPos()) == MapColor.STONE) {
            LootParams.Builder lootcontext$builder = (new LootParams.Builder((ServerLevel) event.getPlayer().level())).withLuck(event.getPlayer().level().random.nextFloat()).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(event.getPos())).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, event.getPlayer().level().getBlockEntity(event.getPos()));
            List<ItemStack> drops = event.getState().getDrops(lootcontext$builder);
            for (ItemStack drop : drops) {
                if (drop.getItem() != Item.byBlock(event.getState().getBlock()) && !(drop.getItem() instanceof BlockItem)) {
                    drop.setCount(Math.min(drop.getCount() * multiplier, drop.getMaxStackSize()));
                }
            }

        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (doItemCapture) {
            if (event.getEntity() instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getItem();
                capturedDrops.add(stack);
                event.setCanceled(true);
            }
        }
    }


    @SubscribeEvent
    public static void onTickEnd(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            DimensionType dim = event.level.dimensionType();
            if (crawlerTasks.containsKey(dim)) {
                List<MatterCollectEvent> swappers = crawlerTasks.get(dim);
                List<MatterCollectEvent> swappersSafe = new ArrayList<>(swappers);
                swappers.clear();
                for (MatterCollectEvent s : swappersSafe) {
                    if (s != null) {
                        s.tick();
                    }
                }
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

    //给稿子添加时运
    @SubscribeEvent
    public static void handleExtraLuck(BlockEvent.BreakEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        ItemStack mainHand = event.getPlayer().getMainHandItem();

        if (!mainHand.isEmpty() && mainHand.getItem() == ModItems.infinity_pickaxe.get()) {
            applyLuck(event, 4);
        }
    }

    @SubscribeEvent
    public static void digging(PlayerEvent.BreakSpeed event) {
        if (!event.getEntity().getMainHandItem().isEmpty()) {
            ItemStack held = event.getEntity().getMainHandItem();
            if (held.getItem() == ModItems.infinity_pickaxe.get() || held.getItem() == ModItems.infinity_shovel.get()) {
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
            if (held.getItem() == ModItems.infinity_pickaxe.get() && event.getTargetBlock().getMapColor(level, BlockPos.ZERO) == MapColor.STONE) {
                if (held.getOrCreateTag().getBoolean("destroyer") && isGarbageBlock(event.getTargetBlock().getBlock())) {
                    event.setResult(Event.Result.ALLOW);
                }
            }
        }
    }

    //合并物质团
    @SubscribeEvent
    public static void clusterCluster(EntityItemPickupEvent event) {
        if (event.getEntity() != null && event.getItem().getItem().getItem() == ModItems.matter_cluster.get()) {
            ItemStack stack = event.getItem().getItem();
            Player player = event.getEntity();

            for (ItemStack slot : player.getInventory().items) {
                if (stack.isEmpty()) {
                    break;
                }
                if (slot.getItem() == ModItems.matter_cluster.get()) {
                    MatterClusterItem.mergeClusters(stack, slot);
                }
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
        if (event.getItemStack().getItem() instanceof SwordInfinityItem) {
            for (int x = 0; x < event.getToolTip().size(); x++) {
                if (event.getToolTip().get(x).getString().contains(I18n.get("tooltip.infinity.desc")) || event.getToolTip().get(x).getString().equals(I18n.get("attribute.name.generic.attack_damage"))) {
                    event.getToolTip().set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtil.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.infinity.desc").withStyle(ChatFormatting.BLUE)));
                    return;
                }
            }
        } else if (event.getItemStack().getItem() instanceof ArmorInfinityItem) {
            for (int x = 0; x < event.getToolTip().size(); x++) {
                if (event.getToolTip().get(x).getString().contains(I18n.get("tooltip.armor.desc"))) {
                    event.getToolTip().set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtil.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.armor.desc").withStyle(ChatFormatting.BLUE)));
                    return;
                } else if (event.getToolTip().get(x).getString().contains(I18n.get("tooltip.armor_toughness.desc"))) {
                    event.getToolTip().set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtil.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.armor_toughness.desc").withStyle(ChatFormatting.BLUE)));
                    return;
                }

            }
        }
    }

    //取消身穿无尽套时的伤害
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (AbilityUtil.isInfinite(player) && !(event.getSource() instanceof ModDamageTypes.DamageSourceRandomMessages)) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth());
            }
            ItemStack totem = getPlayerBagItem(player);
            if (!totem.isEmpty()){
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TotemPacket(totem, player.getId()));

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
    }

    //取消身穿无尽套时受到的所有伤害
    @SubscribeEvent
    public static void onGetHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() == ModItems.infinity_sword.get() && player.getMainHandItem().useOnRelease()) {
            event.setCanceled(true);
        }
        if (AbilityUtil.isInfinite(player) && !event.getSource().is(ModDamageTypes.INFINITY)) {
            event.setCanceled(true);
        }
    }

    //取消对无尽套的伤害
    @SubscribeEvent
    public static void onAttacked(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getSource().getEntity() != null && event.getSource().getEntity() instanceof Player) {
            return;
        }
        if (AbilityUtil.isInfinite(player) && !event.getSource().is(ModDamageTypes.INFINITY)) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.isRecentlyHit() && event.getEntity() instanceof Skeleton && event.getSource().getEntity() instanceof Player player) {
            if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() == ModItems.skull_sword.get()) {
                if (event.getDrops().isEmpty()) {
                    addDrop(event, new ItemStack(Items.WITHER_SKELETON_SKULL, 1));
                } else {
                    int skulls = 0;

                    for (int i = 0; i < event.getDrops().size(); i++) {
                        ItemEntity drop = event.getDrops().iterator().next();
                        ItemStack stack = drop.getItem();
                        if (stack.getItem() == Items.WITHER_SKELETON_SKULL) {
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
        if (item instanceof ArmorInfinityItem || item instanceof AxeInfinityItem || item instanceof BowInfinityItem ||
                item instanceof HoeInfinityItem || item instanceof ShovelInfinityItem || item instanceof PickaxeInfinityItem ||
                item instanceof SwordInfinityItem) {
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
    private static ItemStack getPlayerBagItem(Player player){
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() == ModItems.infinity_totem.get()){
            return mainHandItem;
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() == ModItems.infinity_totem.get()){
            return offhand;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == ModItems.infinity_totem.get())
                return stack;
        }

        return ItemStack.EMPTY;
    }
}
