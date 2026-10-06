package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinitySwordItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.InfinityDamageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

/** Real lifecycle fixtures; a loaded Jonn must drop its actual trophy, not a simulated substitute. */
@EventBusSubscriber(modid = "avaritia_gametest")
public final class DeathLootGameTests {
    private static final String[] TESTS = {"ordinary_player_kill", "infinity_player_kill", "finite_infinity_sword_kill",
            "canceled_death_notifies_once", "canceled_incoming_notifies_once"};

    private DeathLootGameTests() {
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        for (int index = 0; index < TESTS.length; index++) {
            final int scenario = index;
            event.register(Registries.TEST_FUNCTION, Const.rl("death_loot_" + TESTS[index]),
                    () -> helper -> checkDeath(helper, scenario != 0, scenario != 2, scenario == 3, scenario == 4));
        }
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Const.rl("death_loot"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            Identifier id = Const.rl("death_loot_" + name);
            ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(Registries.TEST_FUNCTION, id);
            event.registerTest(id, new FunctionGameTestInstance(function,
                    new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 40, 0, true)));
        }
    }

    private static void checkDeath(GameTestHelper helper, boolean infinity, boolean endless, boolean cancelDeath, boolean cancelIncoming) {
        var player = GameTestPlayers.create(helper, "LootProbe");
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
        Consumer<LivingIncomingDamageEvent> incomingListener = event -> {
            if (cancelIncoming && event.getEntity() == victim) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, deathListener);
        NeoForge.EVENT_BUS.addListener(dropListener);
        NeoForge.EVENT_BUS.addListener(incomingListener);
        boolean previous = ModConfig.isSwordAttackEndless.get();
        try {
            ModConfig.isSwordAttackEndless.set(endless);
            if (infinity) {
                if (endless) {
                    InfinityDamageUtils.forceKill(helper.getLevel(), victim, source);
                } else {
                    var sword = (InfinitySwordItem) ModItems.infinity_sword.get();
                    sword.onLeftClickEntity(new ItemStack(sword), player, victim);
                }
            } else {
                victim.hurtServer(helper.getLevel(), source, 100.0F);
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
            InfinityDamageUtils.forceKill(helper.getLevel(), victim, source);
            helper.assertTrue(deaths[0] == 1 && drops[0] == 1, "terminal targets must not emit repeated hooks");
            helper.succeed();
        } finally {
            ModConfig.isSwordAttackEndless.set(previous);
            NeoForge.EVENT_BUS.unregister(deathListener);
            NeoForge.EVENT_BUS.unregister(dropListener);
            NeoForge.EVENT_BUS.unregister(incomingListener);
        }
    }

    private static int countItem(GameTestHelper helper, LivingEntity victim, net.minecraft.world.item.Item item) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, victim.getBoundingBox().inflate(2.0D),
                entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
    }
}
