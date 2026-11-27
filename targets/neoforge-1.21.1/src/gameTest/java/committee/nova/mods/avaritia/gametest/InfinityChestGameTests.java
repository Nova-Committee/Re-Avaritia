package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.core.chest.ClientChestHandler;
import committee.nova.mods.avaritia.core.chest.InfinityChestContainer;
import committee.nova.mods.avaritia.core.chest.ItemSuper;
import committee.nova.mods.avaritia.core.chest.ServerChestHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InfinityChestGameTests {
    private InfinityChestGameTests() {
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void scrolledProjectionUsesVisibleIdentity(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ChestScrollGT"));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ClientChestHandler chest = null;
        try {
            buffer.writeBlockPos(BlockPos.ZERO);
            buffer.writeUUID(player.getUUID());
            buffer.writeBoolean(false);
            buffer.writeUtf("");
            buffer.writeByte(0);
            buffer.writeUUID(UUID.randomUUID());
            InfinityChestMenu menu = new InfinityChestMenu(1, player.getInventory(), buffer);
            chest = (ClientChestHandler) menu.chest;
            List<ItemSuper> entries = new ArrayList<>();
            for (int index = 0; index < InfinityChestContainer.SIZE + 1; index++) {
                ItemStack stack = new ItemStack(Items.STONE);
                stack.set(DataComponents.CUSTOM_NAME, Component.literal("variant-" + index));
                entries.add(new ItemSuper(stack, index + 1L));
            }
            chest.fullUpdate(entries);
            var projection = menu.chestContainer;
            projection.sortedItems = new ArrayList<>(entries.reversed());
            projection.onChangeItems();
            projection.onScrollTo(1.0D);
            helper.assertTrue(projection.viewingObject.getLast().equals(entries.getFirst()),
                    "partial final row must remain reachable");
            for (int slot = 0; slot < projection.viewingObject.size(); slot++) {
                ItemSuper visible = projection.viewingObject.get(slot);
                ItemStack dummy = projection.removeItemNoUpdate(slot);
                helper.assertTrue(ItemStack.isSameItemSameComponents(dummy, visible.getStack()) && dummy.getCount() == 1,
                        "dummy icon must use sorted/scrolled identity rather than the handler's unsorted slot index");
                helper.assertTrue(projection.getItem(slot).getCount() == visible.getRealCount(),
                        "displayed amount must belong to the visible identity");
            }
            ItemSuper removed = projection.viewingObject.getFirst();
            chest.update(List.of(removed.copyWithCount(0L)));
            projection.onScrollTo(1.0D);
            helper.assertTrue(projection.getItem(0).isEmpty() && projection.removeItemNoUpdate(0).isEmpty(),
                    "removed identity retained by a frozen view must clear its icon and amount");
        } finally {
            if (chest != null) chest.removeListener();
            buffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void deltaPublishesSlotsBeforeNotifyingConsumer(GameTestHelper helper) {
        ClientChestHandler chest = new ClientChestHandler();
        ItemSuper stone = new ItemSuper(new ItemStack(Items.STONE), 32L);
        ItemSuper dirt = new ItemSuper(new ItemStack(Items.DIRT), 4L);
        chest.fullUpdate(List.of(stone, dirt));
        int[] notifications = {0};
        chest.addListener(new InfinityChestContainer(null) {
            @Override
            public void refreshContainer(boolean fullUpdate) {
                notifications[0]++;
                int found = 0;
                for (int slot = 27; slot < chest.getSlots() - 27; slot++) {
                    ItemStack stack = chest.getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        found++;
                        helper.assertTrue(!stack.is(Items.STONE), "removed slot must not survive into the callback");
                    }
                }
                helper.assertTrue(found == chest.storageItems.size(), "all current identities must be visible during callback");
            }
        });
        chest.update(List.of(stone.copyWithCount(0L), new ItemSuper(new ItemStack(Items.GOLD_INGOT), 7L)));
        helper.assertTrue(notifications[0] == 1 && chest.storageItems.get(dirt) == 4L,
                "delta must publish once while retaining unchanged quantities");
        chest.removeListener();
        helper.assertTrue(chest.getStackInSlot(27).isEmpty(), "disconnect must clear cached slots too");
        helper.succeed();
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void immutableIdentityAndLiveAmountsSurviveSave(GameTestHelper helper) {
        ServerChestHandler chest = new ServerChestHandler(helper.getLevel().getServer());
        ItemStack input = new ItemStack(Items.STONE);
        input.set(DataComponents.CUSTOM_NAME, Component.literal("original"));
        ItemSuper identity = new ItemSuper(input, 32L);
        chest.addItem(identity, 32L);
        input.set(DataComponents.CUSTOM_NAME, Component.literal("changed-input"));
        ItemStack exposed = identity.getStack();
        exposed.set(DataComponents.CUSTOM_NAME, Component.literal("changed-output"));
        helper.assertTrue(chest.addItem(identity.copyWithCount(999L), 10L) == 10L, "count metadata must not change identity");
        ItemStack extracted = chest.takeItem(identity, 9);
        helper.assertTrue(extracted.getCount() == 9 && chest.storageItems.size() == 1 && chest.storageItems.get(identity) == 33L,
                "increment and extraction must conserve real quantities independently of key count metadata");
        ServerChestHandler restored = new ServerChestHandler(helper.getLevel().getServer(), chest.buildData());
        helper.assertTrue(restored.storageItems.get(identity) == 33L && restored.storageItems.size() == 1,
                "save/load must serialize live map amounts and preserve original components");
        ItemStack simulated = restored.extractItem(27, 8, true);
        helper.assertTrue(simulated.getCount() == 8 && restored.storageItems.get(identity) == 33L,
                "simulation must not consume items");
        helper.assertTrue(restored.takeItem(identity, 100).getCount() == 33 && restored.isEmpty(),
                "final extraction must return exactly the remainder without duplication");
        helper.succeed();
    }
}
