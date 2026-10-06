package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import committee.nova.mods.avaritia.common.tile.TesseractTile;
import committee.nova.mods.avaritia.core.channel.ServerChannel;
import committee.nova.mods.avaritia.core.channel.ServerChannelManager;
import committee.nova.mods.avaritia.core.chest.ServerChestHandler;
import committee.nova.mods.avaritia.core.io.NativeInventory;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import java.util.UUID;
import java.util.function.Supplier;

/** Real vanilla hopper transfers against the storage used by project menus and persistence. */
public final class NativeStorageGameTests {
    private static final String TEMPLATE = "avaritia:portable_ui_empty";
    private static final long[] BULK_COUNTS = {129L, (long) Integer.MAX_VALUE + 4096L};
    private static final BlockPos STORAGE = new BlockPos(2, 2, 2);
    private static final BlockPos PUSH_HOPPER = STORAGE.above();
    private static final BlockPos PULL_HOPPER = STORAGE.below();

    private NativeStorageGameTests() {}

    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void infinityChestPushSeparatesNbtAndPreservesLongCounts(GameTestHelper helper) {
        InfinityChestTile chest = chest(helper);
        ServerChestHandler handler = chest.getChannel();
        HopperBlockEntity hopper = hopper(helper, PUSH_HOPPER);
        helper.setBlock(PUSH_HOPPER.above(), Blocks.STONE);
        ItemStack first = taggedDiamond("first");
        ItemStack second = taggedDiamond("second");
        String firstKey = StorageUtils.getNbtItemId(first);
        String secondKey = StorageUtils.getNbtItemId(second);
        helper.assertTrue(!firstKey.equals(secondKey), "distinct item NBT must have distinct storage keys");

        for (long count : BULK_COUNTS) {
            CompoundTag expected = chestData(first, firstKey, count, second, secondKey, 17L);
            expected.getCompound("items").putLong("minecraft:diamond", 9L);
            handler.initialize(expected.copy());
            assertDiscovered(helper, chest);

            push(helper, hopper, first.copyWithCount(3));
            expected.getCompound("items").putLong(firstKey, count + 1L);
            helper.assertTrue(expected.equals(handler.buildData()),
                    "one hopper push must increment only the exact first NBT variant above " + count);
            assertStack(helper, hopper.getItem(0), first.copyWithCount(2),
                    "successful push must leave exactly two unchanged tagged diamonds in the hopper");

            push(helper, hopper, second.copyWithCount(2));
            expected.getCompound("items").putLong(secondKey, 18L);
            helper.assertTrue(expected.equals(handler.buildData()),
                    "second NBT variant must not merge with the first variant or untagged diamonds");
            assertStack(helper, hopper.getItem(0), second,
                    "second push must consume exactly one item with its own NBT");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void infinityChestBlockedPullIsExactAndSuccessfulPullMovesOne(GameTestHelper helper) {
        InfinityChestTile chest = chest(helper);
        ServerChestHandler handler = chest.getChannel();
        HopperBlockEntity hopper = hopper(helper, PULL_HOPPER);
        ItemStack source = taggedDiamond("stored");
        String key = StorageUtils.getNbtItemId(source);

        for (long count : BULK_COUNTS) {
            CompoundTag expected = chestData(source, key, count, null, null, 0L);
            handler.initialize(expected.copy());
            assertDiscovered(helper, chest);
            helper.assertTrue(chest.getItem(27).getCount() == 64,
                    "native container must expose a bounded snapshot of the virtual bulk slot");
            expected.getCompound("items").putLong(key, count - 1L);
            assertPullBoundaries(helper, hopper, source, handler::buildData, expected);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void infinityChestSaturationRejectsThenAcceptsExactlyOne(GameTestHelper helper) {
        InfinityChestTile chest = chest(helper);
        ServerChestHandler handler = chest.getChannel();
        HopperBlockEntity pushHopper = hopper(helper, PUSH_HOPPER);
        HopperBlockEntity pullHopper = hopper(helper, PULL_HOPPER);
        helper.setBlock(PUSH_HOPPER.above(), Blocks.STONE);
        ItemStack source = taggedDiamond("saturated");
        String key = StorageUtils.getNbtItemId(source);
        CompoundTag saturated = chestData(source, key, Long.MAX_VALUE, null, null, 0L);
        handler.initialize(saturated.copy());
        pushHopper.setItem(0, source.copy());
        resetCooldown(pushHopper);
        CompoundTag beforeHopper = pushHopper.saveWithoutMetadata().copy();

        tickPush(helper, pushHopper);
        helper.assertTrue(saturated.equals(handler.buildData()),
                "full long-count storage must reject insertion without changing any item NBT or count");
        helper.assertTrue(beforeHopper.equals(pushHopper.saveWithoutMetadata()),
                "rejected vanilla hopper push must restore its original exact stack and NBT");

        helper.assertTrue(HopperBlockEntity.suckInItems(helper.getLevel(), pullHopper),
                "a hopper below saturated storage must extract one item");
        CompoundTag afterPull = saturated.copy();
        afterPull.getCompound("items").putLong(key, Long.MAX_VALUE - 1L);
        helper.assertTrue(afterPull.equals(handler.buildData()),
                "successful saturated extraction must decrement the exact long count, not a bounded snapshot");
        assertStack(helper, pullHopper.getItem(0), source, "extracted item must retain its full NBT");
        helper.assertTrue(itemCount(pullHopper) == 1, "extraction must create exactly one hopper item");

        resetCooldown(pushHopper);
        tickPush(helper, pushHopper);
        helper.assertTrue(pushHopper.isEmpty(), "the newly available single space must consume one source item");
        helper.assertTrue(saturated.equals(handler.buildData()),
                "pull then push must restore the exact saturated count and NBT without overflow");
        helper.assertTrue(itemCount(pullHopper) == 1, "the extracted item must not be duplicated or consumed by insertion");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void tesseractPushAndPullConserveVirtualBulkItems(GameTestHelper helper) {
        TesseractTile tesseract = tesseract(helper);
        ServerChannel channel = tesseract.getChannel();
        HopperBlockEntity pushHopper = hopper(helper, PUSH_HOPPER);
        HopperBlockEntity pullHopper = hopper(helper, PULL_HOPPER);
        helper.setBlock(PUSH_HOPPER.above(), Blocks.STONE);
        ItemStack source = new ItemStack(Items.EMERALD);
        String key = "minecraft:emerald";

        for (long count : BULK_COUNTS) {
            CompoundTag expected = new CompoundTag();
            CompoundTag items = new CompoundTag();
            items.putLong(key, count);
            expected.put("items", items);
            channel.initialize(expected);
            assertDiscovered(helper, tesseract);
            CompoundTag before = channel.buildData().copy();

            push(helper, pushHopper, source.copyWithCount(2));
            CompoundTag afterPush = before.copy();
            afterPush.getCompound("items").putLong(key, count + 1L);
            helper.assertTrue(afterPush.equals(channel.buildData()),
                    "vanilla hopper push must add exactly one to the tesseract's exact long count");
            assertStack(helper, pushHopper.getItem(0), source, "tesseract push must consume only one source item");
            helper.assertTrue(tesseract.getItem(27).getCount() == 64,
                    "tesseract native view must bound the virtual bulk stack without changing stored count");

            assertPullBoundaries(helper, pullHopper, source, channel::buildData, before);
            helper.assertTrue(channel.getRealItemAmount(key) == count,
                    "one push followed by one pull must conserve the exact tesseract count above " + count);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 40)
    public static void tesseractRejectsTaggedItemsAndUnboundFaces(GameTestHelper helper) {
        TesseractTile tesseract = tesseract(helper);
        ServerChannel channel = tesseract.getChannel();
        channel.addItem("minecraft:emerald", BULK_COUNTS[1]);
        HopperBlockEntity hopper = hopper(helper, PUSH_HOPPER);
        helper.setBlock(PUSH_HOPPER.above(), Blocks.STONE);
        ItemStack tagged = new ItemStack(Items.EMERALD, 3);
        tagged.getOrCreateTag().putString("storage_test", "must-not-strip");
        CompoundTag before = channel.buildData().copy();
        push(helper, hopper, tagged.copy());
        assertStack(helper, hopper.getItem(0), tagged, "tesseract must reject tagged input intact");
        helper.assertTrue(before.equals(channel.buildData()),
                "tagged hopper input must not merge into the untagged tesseract count");

        UUID channelOwner = tesseract.getChannelOwner();
        int channelId = tesseract.getChannelID();
        for (Direction side : Direction.values()) {
            helper.assertTrue(!tesseract.canPlaceItemThroughFace(0, tagged, side),
                    "tagged item must be denied by the real handler on " + side);
            ItemStack rejected = HopperBlockEntity.addItem(hopper, tesseract, tagged.copy(), side);
            assertStack(helper, rejected, tagged, "native hopper insertion must honor NBT rejection on " + side);
            helper.assertTrue(before.equals(channel.buildData()), "face rejection must not mutate channel data");
        }

        tesseract.setChannel(null, -1);
        for (Direction side : Direction.values()) {
            helper.assertTrue(tesseract.getSlotsForFace(side).length == 0,
                    "unbound tesseract must expose no automation slots on " + side);
            helper.assertTrue(!tesseract.canTakeItemThroughFace(27, new ItemStack(Items.EMERALD), side)
                            && !tesseract.canPlaceItemThroughFace(0, new ItemStack(Items.EMERALD), side),
                    "unbound tesseract must deny both permissions on " + side);
            ItemStack incoming = new ItemStack(Items.EMERALD, 2);
            ItemStack rejected = HopperBlockEntity.addItem(hopper, tesseract, incoming.copy(), side);
            assertStack(helper, rejected, incoming, "unbound hopper insertion must return the entire input on " + side);
        }
        HopperBlockEntity pullHopper = hopper(helper, PULL_HOPPER);
        helper.assertTrue(!HopperBlockEntity.suckInItems(helper.getLevel(), pullHopper) && pullHopper.isEmpty(),
                "vanilla hopper must not pull items from an unbound tesseract");
        tesseract.setChannel(channelOwner, channelId);
        helper.assertTrue(tesseract.getChannel() == channel && before.equals(channel.buildData()),
                "failed face transfers must leave the original bound channel byte-equivalent");
        helper.succeed();
    }

    private static void assertPullBoundaries(GameTestHelper helper, HopperBlockEntity hopper, ItemStack source,
                                             Supplier<CompoundTag> data, CompoundTag expectedAfterPull) {
        CompoundTag before = data.get().copy();
        for (int slot = 0; slot < hopper.getContainerSize(); slot++) {
            hopper.setItem(slot, source.copyWithCount(64));
        }
        assertBlockedPull(helper, hopper, data, before, "full destination");

        // Not full, but the only available stack space belongs to an incompatible item/tag.
        ItemStack incompatible = source.hasTag() ? taggedDiamond("incompatible") : new ItemStack(Items.IRON_INGOT);
        hopper.setItem(0, incompatible.copyWithCount(63));
        assertBlockedPull(helper, hopper, data, before, "NBT/item-incompatible partial destination");

        hopper.setItem(0, source.copyWithCount(63));
        int beforeItems = itemCount(hopper);
        helper.assertTrue(HopperBlockEntity.suckInItems(helper.getLevel(), hopper),
                "matching space must allow a real hopper to extract exactly one item");
        helper.assertTrue(expectedAfterPull.equals(data.get()),
                "successful extraction must decrement exactly one long-count item and retain all storage NBT");
        assertStack(helper, hopper.getItem(0), source.copyWithCount(64), "destination must receive the original item and NBT");
        helper.assertTrue(itemCount(hopper) == beforeItems + 1, "successful pull must increase the hopper total by exactly one");
    }

    private static void assertBlockedPull(GameTestHelper helper, HopperBlockEntity hopper, Supplier<CompoundTag> data,
                                          CompoundTag beforeStorage, String reason) {
        CompoundTag beforeHopper = hopper.saveWithoutMetadata().copy();
        helper.assertTrue(!HopperBlockEntity.suckInItems(helper.getLevel(), hopper), reason + " must reject extraction");
        helper.assertTrue(beforeStorage.equals(data.get()), reason + " must preserve exact long storage counts and NBT");
        helper.assertTrue(beforeHopper.equals(hopper.saveWithoutMetadata()), reason + " must preserve the entire hopper NBT");
    }

    private static InfinityChestTile chest(GameTestHelper helper) {
        helper.setBlock(STORAGE, ModBlocks.infinity_chest.get());
        InfinityChestTile chest = (InfinityChestTile) helper.getLevel().getBlockEntity(helper.absolutePos(STORAGE));
        chest.setOwner(UUID.randomUUID());
        chest.setChannelId(UUID.randomUUID());
        helper.assertTrue(chest.getItemHandler(Direction.DOWN) == chest.getChannel(),
                "chest automation must use the real bound server chest handler");
        return chest;
    }

    private static TesseractTile tesseract(GameTestHelper helper) {
        helper.setBlock(STORAGE, ModBlocks.tesseract.get());
        TesseractTile tile = (TesseractTile) helper.getLevel().getBlockEntity(helper.absolutePos(STORAGE));
        var player = NativeTestPlayers.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "StorageTest"));
        ServerChannelManager manager = ServerChannelManager.getInstance();
        manager.tryAddChannel(player, "Native hopper regression", false);
        tile.setOwner(player.getUUID());
        tile.setChannel(player.getUUID(), 0);
        helper.assertTrue(!tile.getChannel().isRemoved()
                        && tile.getItemHandler(Direction.DOWN) == manager.getChannel(player.getUUID(), 0),
                "tesseract automation must use a real manager-owned server channel");
        return tile;
    }

    private static HopperBlockEntity hopper(GameTestHelper helper, BlockPos position) {
        helper.setBlock(position, Blocks.HOPPER.defaultBlockState()
                .setValue(HopperBlock.FACING, Direction.DOWN).setValue(HopperBlock.ENABLED, true));
        HopperBlockEntity hopper = (HopperBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(position));
        resetCooldown(hopper);
        return hopper;
    }

    private static void assertDiscovered(GameTestHelper helper, NativeInventory storage) {
        helper.assertTrue(HopperBlockEntity.getContainerAt(helper.getLevel(), helper.absolutePos(STORAGE)) == storage,
                "vanilla hopper discovery must resolve the actual project block entity as a container");
    }

    private static void push(GameTestHelper helper, HopperBlockEntity hopper, ItemStack source) {
        hopper.clearContent();
        hopper.setItem(0, source);
        resetCooldown(hopper);
        tickPush(helper, hopper);
    }

    private static void resetCooldown(HopperBlockEntity hopper) {
        // 1.20.1's cooldown setter is private; use the real persisted hopper state for fixture setup.
        CompoundTag data = hopper.saveWithoutMetadata();
        data.putInt("TransferCooldown", 0);
        hopper.load(data);
    }

    private static void tickPush(GameTestHelper helper, HopperBlockEntity hopper) {
        // Call the actual vanilla server ticker synchronously: no scheduled-world timing or sleeps.
        HopperBlockEntity.pushItemsTick(helper.getLevel(), hopper.getBlockPos(), hopper.getBlockState(), hopper);
    }

    private static CompoundTag chestData(ItemStack first, String firstKey, long firstCount,
                                         ItemStack second, String secondKey, long secondCount) {
        CompoundTag data = new CompoundTag();
        CompoundTag items = new CompoundTag();
        CompoundTag nbt = new CompoundTag();
        items.putLong(firstKey, firstCount);
        nbt.put(firstKey, first.getTag().copy());
        if (second != null) {
            items.putLong(secondKey, secondCount);
            nbt.put(secondKey, second.getTag().copy());
        }
        data.put("items", items);
        data.put("nbtData", nbt);
        return data;
    }

    private static ItemStack taggedDiamond(String variant) {
        ItemStack stack = new ItemStack(Items.DIAMOND);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("storage_test", variant);
        CompoundTag nested = new CompoundTag();
        nested.putLong("serial", 0x100000001L);
        nested.putIntArray("payload", new int[]{7, 11, 19});
        tag.put("nested", nested);
        return stack;
    }

    private static int itemCount(HopperBlockEntity hopper) {
        int count = 0;
        for (int slot = 0; slot < hopper.getContainerSize(); slot++) count += hopper.getItem(slot).getCount();
        return count;
    }

    private static void assertStack(GameTestHelper helper, ItemStack actual, ItemStack expected, String message) {
        helper.assertTrue(ItemStack.matches(actual, expected), message);
    }
}
