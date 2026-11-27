package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.handler.NeutronWolfArmorHandler;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

/**
 * 中子战狼铠机制回归：距离曲线、护卫免伤、图腾拒死、羁绊代受。
 */
@EventBusSubscriber(modid = "avaritia_gametest")
public final class NeutronWolfArmorGameTests {
    private static final String[] TESTS = {"guard_radius_curve", "guard_absorbs_damage", "totem_saves_wolf",
            "totem_needs_durability", "bond_saves_owner"};

    private NeutronWolfArmorGameTests() {
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        for (int index = 0; index < TESTS.length; index++) {
            final int scenario = index;
            event.register(Registries.TEST_FUNCTION, Const.rl("neutron_wolf_armor_" + TESTS[index]),
                    () -> helper -> check(helper, scenario));
        }
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Const.rl("neutron_wolf_armor"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            Identifier id = Const.rl("neutron_wolf_armor_" + name);
            ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(Registries.TEST_FUNCTION, id);
            event.registerTest(id, new FunctionGameTestInstance(function,
                    new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 40, 0, true)));
        }
    }

    private static void check(GameTestHelper helper, int scenario) {
        switch (scenario) {
            case 0 -> checkRadiusCurve(helper);
            case 1 -> checkGuardAbsorbsDamage(helper);
            case 2 -> checkTotem(helper);
            case 3 -> checkTotemWithoutDurability(helper);
            default -> checkBond(helper);
        }
    }

    /** 5 格内满减伤，5→25 格线性，25 格外保底。 */
    private static void checkRadiusCurve(GameTestHelper helper) {
        double bond = ModConfig.neutronWolfBondRadius.get();
        double max = Math.max(bond + 1.0E-4D, ModConfig.neutronWolfMaxRange.get());
        double floor = ModConfig.neutronWolfMinReduction.get();
        helper.assertTrue(Math.abs(NeutronWolfArmorHandler.reductionAt(0.0D) - 1.0D) < 1.0E-6D,
                "damage must be fully negated at zero distance");
        helper.assertTrue(Math.abs(NeutronWolfArmorHandler.reductionAt(bond) - 1.0D) < 1.0E-6D,
                "damage must be fully negated at the bond radius");
        double midpoint = (bond + max) / 2.0D;
        helper.assertTrue(Math.abs(NeutronWolfArmorHandler.reductionAt(midpoint) - (1.0D - floor / 2.0D)) < 1.0E-6D,
                "reduction must decay linearly between bond radius and max range");
        helper.assertTrue(Math.abs(NeutronWolfArmorHandler.reductionAt(max) - (1.0D - floor)) < 1.0E-6D,
                "reduction must reach the configured floor at max range");
        helper.assertTrue(Math.abs(NeutronWolfArmorHandler.reductionAt(max + 32.0D) - (1.0D - floor)) < 1.0E-6D,
                "reduction must not fall below the floor beyond max range");
        helper.succeed();
    }

    /** 羁绊半径内、主人身边的狼不承受普通伤害。 */
    private static void checkGuardAbsorbsDamage(GameTestHelper helper) {
        var owner = GameTestPlayers.create(helper, "GuardProbe");
        var wolf = helper.spawn(EntityType.WOLF, new BlockPos(2, 2, 2));
        wolf.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.neutron_wolf_armor.get()));
        wolf.setOwner(owner);
        owner.setPos(wolf.getX() + 1.0D, wolf.getY(), wolf.getZ());
        float healthBefore = wolf.getHealth();
        wolf.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(owner), 100.0F);
        helper.assertTrue(wolf.getHealth() == healthBefore,
                "a wolf inside the bond radius must not lose health from ordinary damage");
        helper.succeed();
    }

    /** 图腾拒死时消耗铠甲耐久，不再走冷却。 */
    private static void checkTotem(GameTestHelper helper) {
        var wolf = helper.spawn(EntityType.WOLF, new BlockPos(2, 2, 2));
        var armor = new ItemStack(ModItems.neutron_wolf_armor.get());
        wolf.setItemSlot(EquipmentSlot.BODY, armor);
        wolf.setHealth(1.0F);
        int durabilityBefore = armor.getDamageValue();
        wolf.hurtServer(helper.getLevel(), helper.getLevel().damageSources().magic(), 100.0F);
        helper.assertTrue(wolf.isAlive(), "the wolf totem must refuse a lethal hit");
        helper.assertTrue(wolf.getHealth() > 1.0F, "the wolf totem must restore health");
        int spent = armor.getDamageValue() - durabilityBefore;
        helper.assertTrue(spent == 500, "the wolf totem must spend exactly 500 durability (spent=" + spent + ")");
        helper.succeed();
    }

    /** 耐久不足时图腾不生效，狼该牺牲就得牺牲。 */
    private static void checkTotemWithoutDurability(GameTestHelper helper) {
        var wolf = helper.spawn(EntityType.WOLF, new BlockPos(2, 2, 2));
        var armor = new ItemStack(ModItems.neutron_wolf_armor.get());
        armor.setDamageValue(armor.getMaxDamage() - 100);
        wolf.setItemSlot(EquipmentSlot.BODY, armor);
        wolf.setHealth(1.0F);
        wolf.hurtServer(helper.getLevel(), helper.getLevel().damageSources().magic(), 100.0F);
        helper.assertTrue(wolf.isDeadOrDying(), "the wolf totem must not fire when durability cannot pay the cost");
        helper.succeed();
    }

    /** 主人被一击打死时，radius 内的穿铠狼代受，主人存活。 */
    private static void checkBond(GameTestHelper helper) {
        var owner = GameTestPlayers.create(helper, "BondProbe");
        var wolf = helper.spawn(EntityType.WOLF, new BlockPos(2, 2, 2));
        wolf.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.neutron_wolf_armor.get()));
        wolf.setOwner(owner);
        owner.setPos(wolf.getX() + 1.0D, wolf.getY(), wolf.getZ());
        owner.setHealth(1.0F);
        owner.hurtServer(helper.getLevel(), helper.getLevel().damageSources().magic(), 100.0F);
        helper.assertTrue(owner.isAlive() && owner.getHealth() > 0.0F,
                "the bond must absorb a lethal hit aimed at the owner");
        helper.succeed();
    }
}
