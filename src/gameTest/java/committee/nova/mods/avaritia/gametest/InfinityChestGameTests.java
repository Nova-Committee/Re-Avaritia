package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.core.chest.ClientChestHandler;
import committee.nova.mods.avaritia.core.chest.InfinityChestContainer;
import committee.nova.mods.avaritia.core.chest.ServerChestHandler;
import committee.nova.mods.avaritia.util.StorageUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InfinityChestGameTests {
    private InfinityChestGameTests() {
    }

    @OnlyIn(Dist.CLIENT)
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
            ArrayList<String> identities = new ArrayList<>();
            CompoundTag payload = new CompoundTag();
            CompoundTag items = new CompoundTag();
            CompoundTag nbtData = new CompoundTag();
            for (int index = 0; index < InfinityChestContainer.SIZE + 1; index++) {
                ItemStack stack = variant(index);
                String identity = StorageUtils.getNbtItemId(stack);
                identities.add(identity);
                items.putLong(identity, index + 1L);
                nbtData.put(identity, stack.getTag().copy());
            }
            payload.put("items", items);
            payload.put("nbtData", nbtData);
            chest.fullUpdate(payload);
            var projection = menu.chestContainer;
            projection.sortedItems = new ArrayList<>(identities);
            Collections.reverse(projection.sortedItems);
            projection.onChangeItems();
            projection.onScrollTo(1.0D);
            helper.assertTrue(projection.viewingObject.get(projection.viewingObject.size() - 1).equals(identities.get(0)),
                    "partial final row must remain reachable");
            for (int slot = 0; slot < projection.viewingObject.size(); slot++) {
                String identity = projection.viewingObject.get(slot);
                ItemStack dummy = projection.removeItemNoUpdate(slot);
                helper.assertTrue(StorageUtils.getNbtItemId(dummy).equals(identity) && dummy.getCount() == 1,
                        "dummy icon must use sorted/scrolled identity rather than unsorted handler index");
                helper.assertTrue(projection.getItem(slot).getCount() == items.getLong(identity),
                        "displayed amount must belong to the visible identity");
            }
            String first = projection.viewingObject.get(0);
            ItemStack exposed = projection.getItem(0);
            exposed.getTag().putInt("variant", -1);
            helper.assertTrue(StorageUtils.getNbtItemId(projection.getItem(0)).equals(first),
                    "consumer must not mutate cached NBT identity through a projected stack");
            CompoundTag removed = new CompoundTag();
            CompoundTag delta = new CompoundTag();
            delta.putLong(first, 0L);
            removed.put("items", delta);
            chest.update(removed);
            projection.onScrollTo(1.0D);
            helper.assertTrue(projection.getItem(0).isEmpty() && projection.removeItemNoUpdate(0).isEmpty(),
                    "removed identity retained by a frozen view must clear its icon and amount");

            ItemStack shiftVariant = variant(700);
            String shiftIdentity = StorageUtils.getNbtItemId(shiftVariant);
            CompoundTag shiftState = new CompoundTag();
            CompoundTag shiftItems = new CompoundTag();
            CompoundTag shiftNbt = new CompoundTag();
            shiftItems.putLong(shiftIdentity, 100L);
            shiftNbt.put(shiftIdentity, shiftVariant.getTag().copy());
            shiftState.put("items", shiftItems);
            shiftState.put("nbtData", shiftNbt);
            chest.fullUpdate(shiftState);
            player.getInventory().clearContent();
            menu.setCarried(ItemStack.EMPTY);
            menu.onLeftShiftDummySlot(shiftIdentity);
            menu.onRightShiftDummySlot(shiftIdentity);
            int received = 0;
            for (int slot = 0; slot < InfinityChestMenu.CONTAINER_SLOT_START; slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty()) {
                    helper.assertTrue(StorageUtils.getNbtItemId(stack).equals(shiftIdentity),
                            "both shift extraction methods must preserve stored NBT in actual player slots");
                    received += stack.getCount();
                }
            }
            helper.assertTrue(received == 65 && chest.getRealItemAmount(shiftIdentity) == 35L,
                    "shift extraction must move only accepted amounts and deduct the same variant, without loss or duplication");
            for (int slot = 0; slot < InfinityChestMenu.CONTAINER_SLOT_START; slot++) {
                player.getInventory().setItem(slot, new ItemStack(Items.DIRT, 64));
            }
            menu.onLeftShiftDummySlot(shiftIdentity);
            helper.assertTrue(chest.getRealItemAmount(shiftIdentity) == 35L,
                    "full player inventory must not consume projected chest items");
        } finally {
            player.getInventory().clearContent();
            if (chest != null) chest.removeListener();
            buffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void deltaPublishesSlotsBeforeNotifyingConsumer(GameTestHelper helper) {
        ClientChestHandler chest = new ClientChestHandler();
        CompoundTag initial = new CompoundTag();
        CompoundTag initialItems = new CompoundTag();
        initialItems.putLong("minecraft:stone", 32L);
        initialItems.putLong("minecraft:dirt", 4L);
        initial.put("items", initialItems);
        chest.fullUpdate(initial);
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
        CompoundTag delta = new CompoundTag();
        CompoundTag changed = new CompoundTag();
        changed.putLong("minecraft:stone", 0L);
        changed.putLong("minecraft:gold_ingot", 7L);
        delta.put("items", changed);
        chest.update(delta);
        helper.assertTrue(notifications[0] == 1 && chest.getRealItemAmount("minecraft:dirt") == 4L,
                "delta must publish once while retaining unchanged quantities");
        chest.removeListener();
        helper.assertTrue(chest.getStackInSlot(27).isEmpty(), "disconnect must clear cached slots too");
        helper.succeed();
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void extractionConservesNbtAndAmounts(GameTestHelper helper) {
        var chest = new ServerChestHandler() {
            int cachedVariants() {
                return nbtDataCache.size();
            }
        };
        ItemStack input = variant(7);
        input.setCount(100);
        String identity = StorageUtils.getNbtItemId(input);
        chest.addItem(input);
        ItemStack first = chest.takeItem(identity, 9);
        ItemStack second = chest.saveTakeItem(identity, 8);
        ItemStack half = chest.saveTakeItem(identity, true);
        helper.assertTrue(first.getCount() == 9 && second.getCount() == 8 && half.getCount() == 32
                        && chest.getRealItemAmount(identity) == 51L,
                "successive extraction APIs must conserve quantities");
        helper.assertTrue(StorageUtils.getNbtItemId(first).equals(identity)
                        && StorageUtils.getNbtItemId(second).equals(identity)
                        && StorageUtils.getNbtItemId(half).equals(identity),
                "partial extractions must retain NBT for the remainder");
        helper.assertTrue(chest.takeItem(identity, -1).isEmpty() && chest.saveTakeItem(identity, -1).isEmpty()
                        && chest.extractItem(27, -1, false).isEmpty() && chest.getRealItemAmount(identity) == 51L,
                "negative extraction must not increase stored amounts");
        first.getTag().putInt("variant", -1);
        ItemStack simulated = chest.extractItem(27, 64, true);
        helper.assertTrue(simulated.getCount() == 51 && chest.getRealItemAmount(identity) == 51L
                        && StorageUtils.getNbtItemId(simulated).equals(identity),
                "simulation and mutation of an extracted stack must not change stored identity or quantity");
        ItemStack finalStack = chest.extractItem(27, 64, false);
        helper.assertTrue(finalStack.getCount() == 51 && chest.isEmpty() && StorageUtils.getNbtItemId(finalStack).equals(identity),
                "full extraction must read NBT before removing the last stored variant");
        helper.assertTrue(chest.extractItem(27, 1, false).isEmpty(), "empty chest must not duplicate the last extracted item");
        ItemStack insertProbe = variant(7);
        helper.assertTrue(chest.insertItem(27, insertProbe, true).isEmpty() && chest.isEmpty() && chest.cachedVariants() == 0,
                "simulated insertion must not publish storage or cache an NBT identity");
        insertProbe.getTag().putInt("variant", -1);
        chest.insertItem(27, variant(7), false);
        helper.assertTrue(StorageUtils.getNbtItemId(chest.getStackInSlot(27)).equals(identity),
                "simulated insertion must not retain a stale NBT cache entry");
        helper.succeed();
    }

    private static ItemStack variant(int index) {
        ItemStack stack = new ItemStack(Items.STONE);
        CompoundTag tag = new CompoundTag();
        tag.putInt("variant", index);
        stack.setTag(tag);
        return stack;
    }
}
