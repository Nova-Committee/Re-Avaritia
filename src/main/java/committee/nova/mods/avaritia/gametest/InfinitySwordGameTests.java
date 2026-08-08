package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinitySwordItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ToolUtils;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

/** Runtime regression coverage for Infinity Sword boss and range damage. */
@EventBusSubscriber(modid = Const.MOD_ID)
public final class InfinitySwordGameTests {
    private static final Identifier EMPTY_STRUCTURE = Identifier.withDefaultNamespace("empty");
    private static final Identifier ENDER_DRAGON_PART_ID = Const.rl("infinity_sword_ender_dragon_part");
    private static final Identifier RANGED_KILL_ID = Const.rl("infinity_sword_ranged_kill");
    private static final ResourceKey<Consumer<GameTestHelper>> ENDER_DRAGON_PART = ResourceKey.create(
            Registries.TEST_FUNCTION, ENDER_DRAGON_PART_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> RANGED_KILL = ResourceKey.create(
            Registries.TEST_FUNCTION, RANGED_KILL_ID);

    private InfinitySwordGameTests() {
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, ENDER_DRAGON_PART_ID,
                () -> InfinitySwordGameTests::killsEnderDragonThroughPart);
        event.register(Registries.TEST_FUNCTION, RANGED_KILL_ID,
                () -> InfinitySwordGameTests::killsRangedTarget);
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Const.rl("infinity_sword"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(ENDER_DRAGON_PART_ID, runtimeTest(ENDER_DRAGON_PART, environment));
        event.registerTest(RANGED_KILL_ID, runtimeTest(RANGED_KILL, environment));
    }

    private static FunctionGameTestInstance runtimeTest(
            ResourceKey<Consumer<GameTestHelper>> test, Holder<TestEnvironmentDefinition<?>> environment) {
        return new FunctionGameTestInstance(test, new TestData<>(environment, EMPTY_STRUCTURE, 40, 0, true));
    }

    private static void killsEnderDragonThroughPart(GameTestHelper helper) {
        boolean previous = ModConfig.isSwordAttackEndless.get();
        try {
            ModConfig.isSwordAttackEndless.set(true);
            var attacker = helper.makeMockPlayer(GameType.SURVIVAL);
            var dragon = helper.spawn(EntityType.ENDER_DRAGON, new BlockPos(2, 2, 2));
            var sword = (InfinitySwordItem) ModItems.infinity_sword.get();

            boolean handled = sword.onLeftClickEntity(new ItemStack(sword), attacker, dragon.getParts()[0]);

            helper.assertTrue(handled, "Infinity Sword should handle an Ender Dragon part hit");
            helper.assertTrue(dragon.dead, "Ender Dragon parent should enter the terminal death state");
            helper.assertValueEqual(dragon.getHealth(), 0.0F, "Ender Dragon health after an infinity hit");
            helper.succeed();
        } finally {
            ModConfig.isSwordAttackEndless.set(previous);
        }
    }

    private static void killsRangedTarget(GameTestHelper helper) {
        var attacker = helper.makeMockPlayer(GameType.SURVIVAL);
        attacker.setPos(helper.absoluteVec(new Vec3(1.0D, 2.0D, 1.0D)));
        var zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(12, 2, 1));

        ToolUtils.aoeAttack(attacker, 16.0F, 1.0F, true, false, true);

        helper.assertTrue(zombie.dead, "Configured ranged infinity damage should be terminal");
        helper.assertValueEqual(zombie.getHealth(), 0.0F, "Ranged target health after infinity damage");
        helper.succeed();
    }

}