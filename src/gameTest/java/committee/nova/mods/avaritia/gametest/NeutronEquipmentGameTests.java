package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.handler.NeutronHarnessHandler;
import committee.nova.mods.avaritia.init.handler.NeutronNautilusArmorHandler;
import committee.nova.mods.avaritia.init.registry.ModDataAttachments;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

/**
 * 中子战魂铠与中子鹦鹉螺铠的机制回归：召唤驮载、立体防御免疫、陆地不窒息。
 */
@EventBusSubscriber(modid = "avaritia_gametest")
public final class NeutronEquipmentGameTests {
    private static final String[] TESTS = {"harness_summon", "harness_shield", "harness_ghast_reduction",
            "harness_owner_binding", "nautilus_land_breath", "nautilus_water_reduction"};

    private NeutronEquipmentGameTests() {
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        for (int index = 0; index < TESTS.length; index++) {
            final int scenario = index;
            event.register(Registries.TEST_FUNCTION, Const.rl("neutron_equipment_" + TESTS[index]),
                    () -> helper -> check(helper, scenario));
        }
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Const.rl("neutron_equipment"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            Identifier id = Const.rl("neutron_equipment_" + name);
            ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(Registries.TEST_FUNCTION, id);
            event.registerTest(id, new FunctionGameTestInstance(function,
                    new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 60, 0, true)));
        }
    }

    private static void check(GameTestHelper helper, int scenario) {
        switch (scenario) {
            case 0 -> checkSummon(helper);
            case 1 -> checkShield(helper);
            case 2 -> checkGhastReduction(helper);
            case 3 -> checkOwnerBinding(helper);
            case 4 -> checkLandBreath(helper);
            default -> checkWaterReduction(helper);
        }
    }

    /** 空中按键：把已加载区域里最近的穿铠善良恶魂瞬移到脚下。 */
    private static void checkSummon(GameTestHelper helper) {
        var ghast = helper.spawn(EntityType.HAPPY_GHAST, new BlockPos(8, 3, 8));
        ghast.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.neutron_harness.get()));
        var player = GameTestPlayers.create(helper, "SummonProbe");
        var high = helper.absolutePos(new BlockPos(2, 10, 2));
        player.setPos(high.getX() + 0.5D, high.getY(), high.getZ() + 0.5D);
        player.setOnGround(false);
        helper.assertTrue(!player.onGround() && !player.isPassenger(),
                "the probe player must be airborne and unseated before summoning");
        // FakePlayer 的 startRiding 恒为 false（NeoForge 保护），此处验证瞬移结果。
        // 召唤会挑选「已加载区域里最近的一只」，因此断言玩家脚下出现穿铠恶魂，而不是绑定某一只。
        NeutronHarnessHandler.summonMount(player);
        var candidates = helper.getLevel().getEntitiesOfClass(HappyGhast.class, player.getBoundingBox().inflate(5.0D));
        boolean teleported = candidates.stream().anyMatch(candidate ->
                candidate.getItemBySlot(EquipmentSlot.BODY).is(ModItems.neutron_harness.get())
                        && candidate.getY() < player.getY()
                        && Math.abs(candidate.getX() - player.getX()) < 2.0D
                        && Math.abs(candidate.getZ() - player.getZ()) < 2.0D);
        helper.assertTrue(teleported,
                "summon must teleport an armored happy ghast under the player (spawned ghast at "
                        + ghast.getX() + "," + ghast.getZ() + ", player at " + player.getX() + "," + player.getZ() + ")");
        helper.succeed();
    }

    /** 护盾内的友方实体免疫一切伤害。 */
    private static void checkShield(GameTestHelper helper) {
        var player = GameTestPlayers.create(helper, "ShieldProbe");
        var ghast = helper.spawn(EntityType.HAPPY_GHAST, new BlockPos(2, 2, 2));
        ghast.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.neutron_harness.get()));
        player.setPos(ghast.getX(), ghast.getY() + 2.0D, ghast.getZ());
        player.startRiding(ghast, true, true);
        NeutronHarnessHandler.registerShieldForTest(player, ghast, 100L);
        var wolf = helper.spawn(EntityType.WOLF, new BlockPos(4, 2, 4));
        float healthBefore = wolf.getHealth();
        wolf.hurtServer(helper.getLevel(), helper.getLevel().damageSources().magic(), 10.0F);
        helper.assertTrue(wolf.getHealth() == healthBefore,
                "entities inside the harness dome must be immune to damage");
        helper.succeed();
    }

    /** 穿铠的善良恶魂比裸装的少挨一大截伤害。 */
    private static void checkGhastReduction(GameTestHelper helper) {
        var bare = helper.spawn(EntityType.HAPPY_GHAST, new BlockPos(6, 2, 6));
        var armored = helper.spawn(EntityType.HAPPY_GHAST, new BlockPos(2, 2, 2));
        armored.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.neutron_harness.get()));
        float bareBefore = bare.getHealth();
        float armoredBefore = armored.getHealth();
        bare.hurtServer(helper.getLevel(), helper.getLevel().damageSources().magic(), 5.0F);
        armored.hurtServer(helper.getLevel(), helper.getLevel().damageSources().magic(), 5.0F);
        float bareLost = bareBefore - bare.getHealth();
        float armoredLost = armoredBefore - armored.getHealth();
        helper.assertTrue(bareLost > 0.0F, "the bare ghast must take damage (lost=" + bareLost + ")");
        helper.assertTrue(armoredLost <= bareLost * 0.5F,
                "an armored happy ghast must take far less damage (bare=" + bareLost + ", armored=" + armoredLost + ")");
        helper.succeed();
    }

    /** 召唤无主的穿铠恶魂后，它会认下召唤者。 */
    private static void checkOwnerBinding(GameTestHelper helper) {
        var ghast = helper.spawn(EntityType.HAPPY_GHAST, new BlockPos(8, 3, 8));
        ghast.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.neutron_harness.get()));
        var player = GameTestPlayers.create(helper, "OwnerProbe");
        var high = helper.absolutePos(new BlockPos(2, 10, 2));
        player.setPos(high.getX() + 0.5D, high.getY(), high.getZ() + 0.5D);
        player.setOnGround(false);
        NeutronHarnessHandler.summonMount(player);
        // 召唤挑选的是全局最近的一只，断言「有恶魂认下了召唤者」而不是绑定某一只。
        boolean bound = false;
        for (Entity entity : helper.getLevel().getAllEntities()) {
            if (entity instanceof HappyGhast candidate) {
                var owner = candidate.getExistingDataOrNull(ModDataAttachments.HARNESS_OWNER);
                if (owner != null && owner.isOwnedBy(player.getUUID())) {
                    bound = true;
                    break;
                }
            }
        }
        helper.assertTrue(bound, "summoning an unowned ghast must bind it to the summoner");
        helper.succeed();
    }

    /** 水下减伤按配置换算，默认 99%。 */
    private static void checkWaterReduction(GameTestHelper helper) {
        float reduced = NeutronNautilusArmorHandler.reduceWaterDamage(100.0F);
        helper.assertTrue(reduced <= 2.0F, "submerged armored nautilus must take about 1% damage (got=" + reduced + ")");
        helper.succeed();
    }

    /** 穿铠的鹦鹉螺离水时应当补氧。 */
    private static void checkLandBreath(GameTestHelper helper) {
        var nautilus = helper.spawn(EntityType.NAUTILUS, new BlockPos(2, 2, 2));
        nautilus.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.neutron_nautilus_armor.get()));
        helper.assertTrue(NeutronNautilusArmorHandler.shouldRefillAir(nautilus),
                "an armored nautilus outside water must refill air");
        nautilus.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
        helper.assertTrue(!NeutronNautilusArmorHandler.shouldRefillAir(nautilus),
                "a bare nautilus must not gain the air refill");
        helper.succeed();
    }
}
