package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingLegacyItems;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.function.Consumer;

/** Capacity-conserving recovery of pre-library Neutron Ring inventories. */
@EventBusSubscriber(modid = "avaritia_gametest")
public final class NeutronRingLegacyGameTests {
    private static final Identifier EMPTY_STRUCTURE = Identifier.withDefaultNamespace("empty");
    private static final Identifier CREATIVE_FULL = Const.rl("neutron_ring_legacy_creative_full");
    private static final Identifier CREATIVE_PARTIAL = Const.rl("neutron_ring_legacy_creative_partial");
    private static final Identifier SURVIVAL_RETRY = Const.rl("neutron_ring_legacy_survival_retry");
    private static final ResourceKey<Consumer<GameTestHelper>> CREATIVE_FULL_FN =
            ResourceKey.create(Registries.TEST_FUNCTION, CREATIVE_FULL);
    private static final ResourceKey<Consumer<GameTestHelper>> CREATIVE_PARTIAL_FN =
            ResourceKey.create(Registries.TEST_FUNCTION, CREATIVE_PARTIAL);
    private static final ResourceKey<Consumer<GameTestHelper>> SURVIVAL_RETRY_FN =
            ResourceKey.create(Registries.TEST_FUNCTION, SURVIVAL_RETRY);

    private NeutronRingLegacyGameTests() {
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, CREATIVE_FULL, () -> NeutronRingLegacyGameTests::creativeFullInventoryRetainsEscrow);
        event.register(Registries.TEST_FUNCTION, CREATIVE_PARTIAL, () -> NeutronRingLegacyGameTests::creativePartialStackRetainsRemainder);
        event.register(Registries.TEST_FUNCTION, SURVIVAL_RETRY, () -> NeutronRingLegacyGameTests::survivalPartialRetryRestoresNamedOnce);
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Const.rl("neutron_ring_legacy"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CREATIVE_FULL, new FunctionGameTestInstance(CREATIVE_FULL_FN, new TestData<>(environment, EMPTY_STRUCTURE, 40, 0, true)));
        event.registerTest(CREATIVE_PARTIAL, new FunctionGameTestInstance(CREATIVE_PARTIAL_FN, new TestData<>(environment, EMPTY_STRUCTURE, 40, 0, true)));
        event.registerTest(SURVIVAL_RETRY, new FunctionGameTestInstance(SURVIVAL_RETRY_FN, new TestData<>(environment, EMPTY_STRUCTURE, 40, 0, true)));
    }

    private static void creativeFullInventoryRetainsEscrow(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.CREATIVE);
        fillMain(player, new ItemStack(Items.COBBLESTONE, 64));
        ItemStack ring = legacyRing(64);
        NeutronRingLegacyItems.recover(player, ring);

        helper.assertTrue(!ring.has(ModDataComponents.NEUTRON_RING_INVENTORY.get()),
                "legacy component must be stripped after import");
        helper.assertTrue(count(player, Items.COBBLESTONE) == Inventory.INVENTORY_SIZE * 64,
                "creative full inventory must stay filled with filler");
        helper.assertTrue(countNamed(player) == 0, "creative overflow must not enter inventory");
        List<ItemStack> escrow = NeutronRingLegacyItems.get(helper.getLevel().getServer()).peek(player.getUUID());
        helper.assertTrue(escrow.size() == 1 && matchesLegacy(escrow.getFirst(), 64),
                "creative overflow must remain in escrow with name and custom data");
        NeutronRingLegacyItems.get(helper.getLevel().getServer()).drain(player);
        List<ItemStack> afterRetry = NeutronRingLegacyItems.get(helper.getLevel().getServer()).peek(player.getUUID());
        helper.assertTrue(afterRetry.size() == 1 && matchesLegacy(afterRetry.getFirst(), 64),
                "retry against a still-full creative inventory must not duplicate or consume escrow");
        helper.assertTrue(countNamed(player) == 0, "retry must not insert into a full creative inventory");
        helper.assertEntityNotPresent(EntityType.ITEM);
        helper.succeed();
    }

    private static void creativePartialStackRetainsRemainder(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.CREATIVE);
        fillMain(player, new ItemStack(Items.COBBLESTONE, 64));
        player.getInventory().setItem(35, namedDiamond(32));
        ItemStack ring = legacyRing(64);
        NeutronRingLegacyItems.recover(player, ring);

        helper.assertTrue(!ring.has(ModDataComponents.NEUTRON_RING_INVENTORY.get()),
                "legacy component must be stripped after import");
        helper.assertTrue(countNamed(player) == 64, "creative merge must fill the matching stack only");
        List<ItemStack> escrow = NeutronRingLegacyItems.get(helper.getLevel().getServer()).peek(player.getUUID());
        helper.assertTrue(escrow.size() == 1 && matchesLegacy(escrow.getFirst(), 32),
                "creative overflow after a partial merge must stay in escrow");
        helper.assertTrue(count(player, Items.COBBLESTONE) == 35 * 64, "filler stacks must be untouched");
        helper.assertEntityNotPresent(EntityType.ITEM);
        helper.succeed();
    }

    private static void survivalPartialRetryRestoresNamedOnce(GameTestHelper helper) {
        ServerPlayer player = player(helper, GameType.SURVIVAL);
        fillMain(player, new ItemStack(Items.COBBLESTONE, 64));
        player.getInventory().setItem(35, namedDiamond(32));
        ItemStack ring = legacyRing(64);
        NeutronRingLegacyItems escrow = NeutronRingLegacyItems.get(helper.getLevel().getServer());
        NeutronRingLegacyItems.recover(player, ring);

        helper.assertTrue(!ring.has(ModDataComponents.NEUTRON_RING_INVENTORY.get()),
                "legacy component must be stripped after import");
        helper.assertTrue(countNamed(player) == 64, "survival merge must accept only remaining stack space");
        List<ItemStack> remaining = escrow.peek(player.getUUID());
        helper.assertTrue(remaining.size() == 1 && matchesLegacy(remaining.getFirst(), 32),
                "uninserted named remainder must stay in escrow");

        var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        Tag encoded = NeutronRingLegacyItems.CODEC.encodeStart(ops, escrow).getOrThrow();
        NeutronRingLegacyItems decoded = NeutronRingLegacyItems.CODEC.parse(ops, encoded).getOrThrow();
        List<ItemStack> roundTrip = decoded.peek(player.getUUID());
        helper.assertTrue(roundTrip.size() == 1 && ItemStack.matches(remaining.getFirst(), roundTrip.getFirst()),
                "escrow codec round-trip must keep remainder count, name and components");

        escrow.drain(player);
        helper.assertTrue(countNamed(player) == 64, "retry without space must not duplicate inserted items");
        helper.assertTrue(escrow.peek(player.getUUID()).size() == 1 && matchesLegacy(escrow.peek(player.getUUID()).getFirst(), 32),
                "retry without space must keep the same remainder");

        player.getInventory().setItem(0, ItemStack.EMPTY);
        escrow.drain(player);
        helper.assertTrue(countNamed(player) == 96, "retry into a free slot must restore the remaining named items once");
        helper.assertTrue(escrow.peek(player.getUUID()).isEmpty(), "successful retry must clear escrow");
        helper.assertTrue(namedSlots(player) == 2, "restored remainder must occupy the freed slot without extra copies");
        helper.assertEntityNotPresent(EntityType.ITEM);
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper, GameType mode) {
        ServerPlayer player = GameTestPlayers.create(helper, "AvaritiaLegacy");
        player.setGameMode(mode);
        player.getInventory().clearContent();
        return player;
    }

    private static ItemStack legacyRing(int count) {
        ItemStack ring = new ItemStack(ModItems.neutron_ring.get());
        ring.set(ModDataComponents.NEUTRON_RING_INVENTORY.get(), ItemContainerContents.fromItems(List.of(namedDiamond(count))));
        return ring;
    }

    private static ItemStack namedDiamond(int count) {
        ItemStack stack = new ItemStack(Items.DIAMOND, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Legacy Gem"));
        CompoundTag mark = new CompoundTag();
        mark.putInt("mark", 7);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(mark));
        return stack;
    }

    private static boolean matchesLegacy(ItemStack stack, int count) {
        return stack.is(Items.DIAMOND)
                && stack.getCount() == count
                && "Legacy Gem".equals(stack.getHoverName().getString())
                && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("mark", 0) == 7;
    }

    private static void fillMain(ServerPlayer player, ItemStack filler) {
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            player.getInventory().setItem(i, filler.copy());
        }
    }

    private static int count(ServerPlayer player, net.minecraft.world.item.Item item) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static int countNamed(ServerPlayer player) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (matchesLegacy(stack, stack.getCount())) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static int namedSlots(ServerPlayer player) {
        int slots = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (matchesLegacy(stack, stack.getCount())) {
                slots++;
            }
        }
        return slots;
    }
}
