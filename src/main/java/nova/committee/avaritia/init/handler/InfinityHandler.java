package nova.committee.avaritia.init.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nova.committee.avaritia.common.entity.ImmortalItemEntity;
import nova.committee.avaritia.common.item.ArmorInfinityItem;
import nova.committee.avaritia.common.item.MatterClusterItem;
import nova.committee.avaritia.common.item.tools.*;
import nova.committee.avaritia.init.event.AEOCrawlerTask;
import nova.committee.avaritia.init.registry.ModItems;

import java.util.*;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:46
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InfinityHandler {
    public static Set<String> defaultTrashOres = new HashSet<>();
    private static Map<DimensionType, List<AEOCrawlerTask>> crawlerTasks = new HashMap<>();
    private static boolean doItemCapture = false;
    private static Set<ItemStack> capturedDrops = new LinkedHashSet<>();

    public static boolean isInfinite(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorInfinityItem)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isInfiniteChestplate(Player player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorInfinityItem))
        	return false;

        return true;
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
//        List<String > id = new ArrayList<>();
//        for(ItemStack entity : dropsCopy){
//            id.add(entity.getDisplayName().getString());
//        }
//        Static.LOGGER.info(id);
        capturedDrops.clear();
        return dropsCopy;
    }


    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinWorldEvent event) {
        if (doItemCapture) {
            if (event.getEntity() instanceof ItemEntity) {
                ItemStack stack = ((ItemEntity) event.getEntity()).getItem();
                capturedDrops.add(stack);
                event.setCanceled(true);
            }
        }
    }


    public static AEOCrawlerTask startCrawlerTask(Level world, Player player, ItemStack stack, BlockPos coords, int steps, boolean leaves, boolean force, Set<BlockPos> posChecked) {
        AEOCrawlerTask swapper = new AEOCrawlerTask(world, player, stack, coords, steps, leaves, force, posChecked);
        DimensionType dim = world.dimensionType();
        if (!crawlerTasks.containsKey(dim)) {
            crawlerTasks.put(dim, new ArrayList<>());
        }
        crawlerTasks.get(dim).add(swapper);
        return swapper;
    }

    @SubscribeEvent
    public static void onTickEnd(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            DimensionType dim = event.world.dimensionType();
            if (crawlerTasks.containsKey(dim)) {
                List<AEOCrawlerTask> swappers = crawlerTasks.get(dim);
                List<AEOCrawlerTask> swappersSafe = new ArrayList<>(swappers);
                swappers.clear();
                for (AEOCrawlerTask s : swappersSafe) {
                    if (s != null) {
                        s.tick();
                    }
                }
            }
        }
    }

    public static void applyLuck(BlockEvent.BreakEvent event, int multiplier) {
        if (event.getState().getMaterial() == Material.STONE) {
            LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel) event.getPlayer().level)).withRandom(event.getPlayer().level.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(event.getPos())).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, event.getPlayer().level.getBlockEntity(event.getPos()));
            List<ItemStack> drops = event.getState().getDrops(lootcontext$builder);
            for (ItemStack drop : drops) {
                if (drop.getItem() != Item.byBlock(event.getState().getBlock()) && !(drop.getItem() instanceof BlockItem)) {
                    drop.setCount(Math.min(drop.getCount() * multiplier, drop.getMaxStackSize()));
                }
            }

        }
    }

    @SubscribeEvent
    public static void onPlayerMine(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getFace() == null || event.getWorld().isClientSide || event.getItemStack().isEmpty() || event.getPlayer().isCreative()) {
            return;
        }
        Level world = event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (event.getItemStack().getItem() == ModItems.pick_axe) {
            if (state.getDestroySpeed(world, event.getPos()) <= -1 || state.getMaterial() == Material.STONE || state.getMaterial() == Material.METAL) {
                if (event.getItemStack().getOrCreateTag().getBoolean("hammer")) {
                    ModItems.pick_axe.onBlockStartBreak(event.getPlayer().getMainHandItem(), event.getPos(), event.getPlayer());
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

        if (!mainHand.isEmpty() && mainHand.getItem() == ModItems.pick_axe) {
            applyLuck(event, 4);
        }
    }


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {

    }

    @SubscribeEvent
    public static void digging(PlayerEvent.BreakSpeed event) {
        if (!event.getEntityLiving().getMainHandItem().isEmpty()) {
            ItemStack held = event.getEntityLiving().getMainHandItem();
            if (held.getItem() == ModItems.pick_axe || held.getItem() == ModItems.infinity_shovel) {
                if (!event.getEntityLiving().isOnGround()) {
                    event.setNewSpeed(event.getNewSpeed() * 5);
                }
                if (!event.getEntityLiving().isInWater() && !EnchantmentHelper.hasAquaAffinity(event.getEntityLiving())) {
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
        if (!event.getEntityLiving().getMainHandItem().isEmpty()) {
            ItemStack held = event.getEntityLiving().getMainHandItem();
            if (held.getItem() == ModItems.pick_axe && event.getTargetBlock().getMaterial() == Material.STONE) {
                if (held.getOrCreateTag().getBoolean("destroyer")) {
                    event.setResult(Event.Result.ALLOW);
                }
            }
        }
    }

    //黑名单功能
//    private static boolean isGarbageBlock(Block block) {
//        Static.LOGGER.info(TagCollectionManager.getInstance().getBlocks().getAllTags().keySet());
//        for (ResourceLocation id : BlockTags.getAllTags().getMatchingTags(block)) {
//            String ore = id.getPath();
//            if (ore.contains("cobblestone") || ore.contains("stone") || ore.contains("netherrack")) {
//                return true;
//            }
//        }
//        return false;
//    }


    //合并物质团
    @SubscribeEvent
    public static void clusterClustererererer(EntityItemPickupEvent event) {
        if (event.getPlayer() != null && event.getItem().getItem().getItem() == ModItems.matter_cluster) {
            ItemStack stack = event.getItem().getItem();
            Player player = event.getPlayer();

            for (ItemStack slot : player.getInventory().items) {
                if (stack.isEmpty()) {
                    break;
                }
                if (slot.getItem() == ModItems.matter_cluster) {
                    MatterClusterItem.mergeClusters(stack, slot);
                }
            }
        }
    }


    @SubscribeEvent
    public static void expCancel(ItemExpireEvent event) {
        if (event.getEntityItem() instanceof ImmortalItemEntity) {
            event.setCanceled(true);
        }
    }


    //取消身穿无尽套时受到的所有伤害
    @SubscribeEvent
    public static void onGetHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof Player player)) {
            return;
        }
        if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() == ModItems.infinity_sword && player.getMainHandItem().useOnRelease()) {
            event.setCanceled(true);
        }
        if (isInfinite(player) && !event.getSource().getMsgId().equals("infinity")) {
            event.setCanceled(true);
        }
    }

    //取消对无尽套的伤害
    @SubscribeEvent
    public static void onAttacked(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof Player player)) {
            return;
        }
        if (event.getSource().getEntity() != null && event.getSource().getEntity() instanceof Player) {
            return;
        }
        if (isInfinite(player) && !event.getSource().getMsgId().equals("infinity")) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.isRecentlyHit() && event.getEntityLiving() instanceof Skeleton && event.getSource().getEntity() instanceof Player player) {
            if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() == ModItems.skull_sword) {
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
        ItemEntity entityItem = event.getEntityItem();
        Item item = entityItem.getItem().getItem();
        if (item instanceof ArmorInfinityItem || item instanceof AxeInfinityItem || item instanceof BowInfinityItem ||
                item instanceof HoeInfinityItem || item instanceof ShovelInfinityItem || item instanceof PickaxeInfinityItem ||
                item instanceof SwordInfinityItem) {
            entityItem.setInvulnerable(true);
        }
    }

    private static void addDrop(LivingDropsEvent event, ItemStack drop) {
        ItemEntity entityitem = new ItemEntity(event.getEntityLiving().level, event.getEntityLiving().getX(), event.getEntityLiving().getY(), event.getEntityLiving().getZ(), drop);
        entityitem.setDefaultPickUpDelay();
        event.getDrops().add(entityitem);
    }

}
