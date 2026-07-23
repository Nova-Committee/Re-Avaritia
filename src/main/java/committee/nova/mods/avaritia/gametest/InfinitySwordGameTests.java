package committee.nova.mods.avaritia.gametest;

import com.mojang.serialization.MapCodec;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinitySwordItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.function.Consumer;

/** Runtime regression coverage for Infinity Sword boss and range damage. */
@EventBusSubscriber(modid = Const.MOD_ID)
public final class InfinitySwordGameTests {
    private static final Identifier EMPTY_STRUCTURE = Identifier.withDefaultNamespace("empty");

    private InfinitySwordGameTests() {
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Const.rl("infinity_sword"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(Const.rl("infinity_sword_ender_dragon_part"),
                runtimeTest(environment, InfinitySwordGameTests::killsEnderDragonThroughPart));
        event.registerTest(Const.rl("infinity_sword_ranged_kill"),
                runtimeTest(environment, InfinitySwordGameTests::killsRangedTarget));
    }

    private static GameTestInstance runtimeTest(
            Holder<TestEnvironmentDefinition<?>> environment, Consumer<GameTestHelper> test) {
        return new RuntimeGameTest(new TestData<>(environment, EMPTY_STRUCTURE, 40, 0, true), test);
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

    private static final class RuntimeGameTest extends GameTestInstance {
        private final Consumer<GameTestHelper> test;

        private RuntimeGameTest(
                TestData<Holder<TestEnvironmentDefinition<?>>> data, Consumer<GameTestHelper> test) {
            super(data);
            this.test = test;
        }

        @Override
        public void run(GameTestHelper helper) {
            test.accept(helper);
        }

        @Override
        public MapCodec<? extends GameTestInstance> codec() {
            return FunctionGameTestInstance.CODEC;
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.literal("Avaritia runtime regression");
        }
    }
}
