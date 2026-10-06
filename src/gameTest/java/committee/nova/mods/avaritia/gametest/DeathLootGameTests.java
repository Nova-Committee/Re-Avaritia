package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinitySwordItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.UUID;
import java.util.function.Consumer;

/** Real death/drop lifecycle checks; installing Jonn also asserts its actual trophy item. */
@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DeathLootGameTests {
    private DeathLootGameTests() {
    }

    @GameTest(template = "portable_ui_empty")
    public static void ordinaryPlayerKill(GameTestHelper helper) {
        checkDeath(helper, false, false, false, false);
    }

    @GameTest(template = "portable_ui_empty")
    public static void infinityPlayerKill(GameTestHelper helper) {
        checkDeath(helper, true, true, false, false);
    }

    @GameTest(template = "portable_ui_empty")
    public static void finiteInfinitySwordKill(GameTestHelper helper) {
        checkDeath(helper, true, false, false, false);
    }

    @GameTest(template = "portable_ui_empty")
    public static void canceledDeathStillNotifiesOnce(GameTestHelper helper) {
        checkDeath(helper, true, true, true, false);
    }

    private static void checkDeath(GameTestHelper helper, boolean infinity, boolean endless, boolean cancelDeath, boolean cancelIncoming) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "LootProbe"));
        player.setPos(helper.absolutePos(new BlockPos(1, 2, 1)).getX(),
                helper.absolutePos(new BlockPos(1, 2, 1)).getY(),
                helper.absolutePos(new BlockPos(1, 2, 1)).getZ());
        var victim = helper.spawn(EntityType.COW, new BlockPos(4, 2, 4));
        victim.setNoAi(true);
        var source = infinity ? player.damageSources().source(ModDamageTypes.INFINITY, victim, player)
                : player.damageSources().playerAttack(player);
        int[] deaths = {0};
        int[] drops = {0};
        Consumer<LivingDeathEvent> deathListener = event -> {
            if (event.getEntity() == victim) {
                deaths[0]++;
                helper.assertTrue(event.getSource().getEntity() == player,
                        "death source must retain the exact ServerPlayer killer");
                if (cancelDeath) {
                    event.setCanceled(true);
                    victim.setHealth(victim.getMaxHealth());
                }
            }
        };
        Consumer<LivingDropsEvent> dropListener = event -> {
            if (event.getEntity() == victim) {
                drops[0]++;
                helper.assertTrue(event.isRecentlyHit(), "forced kill must retain player loot attribution");
                event.getDrops().add(new ItemEntity(helper.getLevel(), victim.getX(), victim.getY(), victim.getZ(),
                        new ItemStack(Items.DIAMOND)));
            }
        };
        
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, deathListener);
        NeoForge.EVENT_BUS.addListener(dropListener);
        
        boolean previous = ModConfig.isSwordAttackEndless.get();
        try {
            ModConfig.isSwordAttackEndless.set(endless);
            if (infinity) {
                var sword = (InfinitySwordItem) ModItems.infinity_sword.get();
                sword.onLeftClickEntity(new ItemStack(sword), player, victim);
            } else {
                victim.hurt(source, 100.0F);
            }
            helper.assertTrue(victim.dead && victim.getHealth() == 0.0F, "victim must enter terminal death state");
            helper.assertTrue(deaths[0] == 1, "death hook must fire exactly once");
            helper.assertTrue(drops[0] == 1, "death loot hook must fire exactly once");
            helper.assertTrue(countItem(helper, victim, Items.DIAMOND) == 1,
                    "third-party LivingDrops additions must reach the world exactly once");
            helper.assertTrue(countItem(helper, victim, Items.BEEF) > 0,
                    "vanilla mob loot must remain present alongside third-party drops");
            if (ModList.get().isLoaded("trophymanager") && !cancelDeath) {
                int trophies = helper.getLevel().getEntitiesOfClass(ItemEntity.class, victim.getBoundingBox().inflate(2.0D),
                        item -> BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString()
                                .equals("trophymanager:trophy")).stream().mapToInt(item -> item.getItem().getCount()).sum();
                helper.assertTrue(trophies == 1,
                        "real Jonn trophy must drop exactly once; use General dropChanceMobs/Boss=1, allowFakePlayer=true");
            }
            // Repeating a terminal attack must not duplicate death or loot callbacks.
            if (infinity) {
                ((InfinitySwordItem) ModItems.infinity_sword.get()).hurt(victim, source, Float.MAX_VALUE);
            } else {
                victim.hurt(source, 100.0F);
            }
            helper.assertTrue(deaths[0] == 1 && drops[0] == 1, "terminal targets must not emit repeated hooks");
            helper.succeed();
        } finally {
            ModConfig.isSwordAttackEndless.set(previous);
            NeoForge.EVENT_BUS.unregister(deathListener);
            NeoForge.EVENT_BUS.unregister(dropListener);
            
        }
    }

    private static int countItem(GameTestHelper helper, LivingEntity victim, net.minecraft.world.item.Item item) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, victim.getBoundingBox().inflate(2.0D),
                entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
    }
}
