package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.container.InfinityChestContainer;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.core.chest.ChestHandler;
import committee.nova.mods.avaritia.core.chest.ClientChestHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = "avaritia_gametest")
public final class InfinityChestGameTests {
    private static final Identifier PROJECTION = Const.rl("infinity_chest_projection_sync");
    private static final Identifier AMOUNTS = Const.rl("infinity_chest_identity_amounts");

    private InfinityChestGameTests() {
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, PROJECTION, () -> InfinityChestGameTests::pagedSyncRefreshesScrolledProjection);
        event.register(Registries.TEST_FUNCTION, AMOUNTS, () -> InfinityChestGameTests::transactionsConserveImmutableIdentity);
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Const.rl("infinity_chest"), new TestEnvironmentDefinition.AllOf());
        register(event, PROJECTION, environment);
        register(event, AMOUNTS, environment);
    }

    private static void register(RegisterGameTestsEvent event, Identifier id, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(id, new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id),
                new TestData<>(environment, Identifier.withDefaultNamespace("empty"), 100, 0, true)));
    }

    private static void pagedSyncRefreshesScrolledProjection(GameTestHelper helper) {
        var player = GameTestPlayers.create(helper, "ChestScrollGT");
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ClientChestHandler chest = null;
        try {
            buffer.writeBlockPos(BlockPos.ZERO);
            buffer.writeUUID(player.getUUID());
            buffer.writeBoolean(false);
            buffer.writeUtf("");
            buffer.writeByte(6);
            buffer.writeUUID(UUID.randomUUID());
            InfinityChestMenu menu = new InfinityChestMenu(1, player.getInventory(), buffer);
            chest = (ClientChestHandler) menu.getChest();
            chest.clear();
            chest.addListener(menu.getChestContainer());
            List<ChestHandler.StoredItem> entries = new ArrayList<>();
            for (int index = 0; index < InfinityChestContainer.SIZE + 1; index++) {
                ItemStack stack = new ItemStack(Items.STONE);
                stack.set(DataComponents.CUSTOM_NAME, Component.literal("variant-" + index));
                entries.add(new ChestHandler.StoredItem(ItemResource.of(stack), index + 1L));
            }
            chest.fullUpdatePage(entries.subList(0, 50), true, false);
            helper.assertTrue(chest.variantCount() == 0 && menu.getChestContainer().totalVariants() == 0,
                    "incomplete full synchronization must not publish a partial map or projection");
            chest.fullUpdatePage(entries.subList(50, entries.size()), false, true);
            var projection = menu.getChestContainer();
            projection.scrollTo(1.0D);
            int last = entries.size() - InfinityChestContainer.WIDTH - 1;
            helper.assertTrue(projection.resource(last).equals(entries.getLast().resource())
                            && projection.amount(last) == entries.getLast().amount(),
                    "partial final row must show the final sorted identity and matching long amount");
            for (int slot = 0; slot <= last; slot++) {
                helper.assertTrue(ItemResource.of(projection.getItem(slot)).equals(projection.resource(slot))
                                && projection.amount(slot) == chest.amount(projection.resource(slot)),
                        "scrolled icon and quantity must belong to the same current identity");
            }
            ItemResource removed = projection.resource(0);
            var changed = new ChestHandler.StoredItem(entries.getLast().resource(), 1L);
            chest.update(List.of(changed), List.of(removed));
            helper.assertTrue(chest.amount(removed) == 0L && chest.amount(changed.resource()) == 1L,
                    "delta must replace quantities, not add or duplicate them");
            for (int slot = 0; slot < InfinityChestContainer.SIZE; slot++) {
                helper.assertTrue(!projection.resource(slot).equals(removed)
                                && projection.amount(slot) == chest.amount(projection.resource(slot)),
                        "delta must synchronously rebuild sorted snapshots without a removed identity");
            }
            chest.fullUpdatePage(List.of(new ChestHandler.StoredItem(entries.getFirst().resource(), 3L)), true, true);
            helper.assertTrue(!projection.canScroll() && projection.getScroll() == 0.0D
                            && projection.amount(0) == 3L && projection.getItem(1).isEmpty(),
                    "shrinking full state must reset scrolling and clear old page-tail slots");
        } finally {
            if (chest != null) chest.clear();
            buffer.release();
        }
        helper.succeed();
    }

    private static void transactionsConserveImmutableIdentity(GameTestHelper helper) {
        ChestHandler chest = new ChestHandler();
        ItemStack input = new ItemStack(Items.STONE);
        input.set(DataComponents.CUSTOM_NAME, Component.literal("original"));
        ItemResource identity = ItemResource.of(input);
        try (Transaction transaction = Transaction.openRoot()) {
            helper.assertTrue(chest.insertLong(identity, Long.MAX_VALUE - 2L, transaction) == Long.MAX_VALUE - 2L,
                    "long insertion must not truncate through int");
            transaction.commit();
        }
        input.set(DataComponents.CUSTOM_NAME, Component.literal("changed-input"));
        ItemStack exposed = identity.toStack();
        exposed.set(DataComponents.CUSTOM_NAME, Component.literal("changed-output"));
        try (Transaction transaction = Transaction.openRoot()) {
            helper.assertTrue(chest.insertLong(identity, 9L, transaction) == 2L,
                    "saturation must return only the accepted remainder");
            transaction.commit();
        }
        helper.assertTrue(chest.variantCount() == 1 && chest.amount(identity) == Long.MAX_VALUE,
                "external stack mutation must not change stored resource identity");
        try (Transaction transaction = Transaction.openRoot()) {
            helper.assertTrue(chest.extractLong(identity, 9L, transaction) == 9L, "rollback fixture must extract a real amount");
        }
        helper.assertTrue(chest.amount(identity) == Long.MAX_VALUE, "uncommitted extraction must restore the whole amount");
        try (Transaction transaction = Transaction.openRoot()) {
            helper.assertTrue(chest.extractLong(identity, Long.MAX_VALUE, transaction) == Long.MAX_VALUE,
                    "committed full extraction must return exactly the stored amount");
            transaction.commit();
        }
        helper.assertTrue(chest.isEmpty() && chest.amount(identity) == 0L && chest.getResource(0).isEmpty(),
                "final extraction must clear identity and quantity together");
        helper.succeed();
    }
}
