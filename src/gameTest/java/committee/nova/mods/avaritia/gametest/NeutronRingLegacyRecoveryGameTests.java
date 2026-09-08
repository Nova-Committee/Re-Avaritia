package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingLegacyRecovery;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NeutronRingLegacyRecoveryGameTests {
    private static final String TEMPLATE = "portable_ui_empty";
    private static final String HEIRLOOM = "Heirloom";
    private static final String STAR_SHARDS = "Star shards";

    private NeutronRingLegacyRecoveryGameTests() {
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void creativeFullInventoryRetainsRecoveredStacks(GameTestHelper helper) {
        FakePlayer player = player(helper, "NRLegCrea", GameType.CREATIVE);
        fillMain(player, new ItemStack(Items.DIRT, 64));
        ItemStack ring = new ItemStack(ModItems.neutron_ring.get());
        ItemStack named = new ItemStack(Items.DIAMOND, 7);
        named.setHoverName(Component.literal(STAR_SHARDS));
        putInRing(ring, named);

        NeutronRingLegacyRecovery.recover(player, ring);

        helper.assertTrue(countMain(player, Items.DIRT) == 36 * 64, "creative dirt fill must stay untouched");
        helper.assertTrue(countMain(player, Items.DIAMOND) == 0, "full creative inventory must not eat recovered diamonds");
        helper.assertTrue(countNamed(player, STAR_SHARDS) == 0, "named diamonds must not appear in a full inventory");
        List<ItemStack> escrow = escrowOf(player);
        helper.assertTrue(escrow.size() == 1 && escrow.get(0).getCount() == 7, "all 7 named diamonds stay in escrow");
        helper.assertTrue(STAR_SHARDS.equals(escrow.get(0).getHoverName().getString()),
                "escrow must keep the custom name");
        helper.assertTrue(ringEmpty(ring) && recovered(ring), "ring slots clear only after escrow import");
        NeutronRingLegacyRecovery.recover(player, ring);
        helper.assertTrue(escrowOf(player).size() == 1 && escrowOf(player).get(0).getCount() == 7,
                "repeated recover must not duplicate or destroy escrow");
        helper.assertTrue(countMain(player, Items.DIAMOND) == 0, "retry must not insert into a still-full inventory");
        assertNoItemDrops(helper, player);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void survivalPartialCapacityRetainsExactRemainder(GameTestHelper helper) {
        FakePlayer player = player(helper, "NRLegSurv", GameType.SURVIVAL);
        fillMain(player, new ItemStack(Items.COBBLESTONE, 64));
        player.getInventory().items.set(0, new ItemStack(Items.DIRT, 60));
        ItemStack ring = new ItemStack(ModItems.neutron_ring.get());
        putInRing(ring, new ItemStack(Items.DIRT, 16));

        NeutronRingLegacyRecovery.recover(player, ring);

        helper.assertTrue(countMain(player, Items.DIRT) == 64, "survival merge should fill the partial dirt stack");
        helper.assertTrue(countMain(player, Items.COBBLESTONE) == 35 * 64, "unrelated cobble stacks must stay 64");
        List<ItemStack> escrow = escrowOf(player);
        helper.assertTrue(escrow.size() == 1 && escrow.get(0).is(Items.DIRT) && escrow.get(0).getCount() == 12,
                "uninserted dirt remainder stays in SavedData");
        helper.assertTrue(NeutronRingLegacyRecovery.get(player.server).isDirty(),
                "partial insertion must mark SavedData dirty");
        helper.assertTrue(ringEmpty(ring) && recovered(ring), "legacy ring inventory is cleared after import");
        NeutronRingLegacyRecovery.recover(player, ring);
        helper.assertTrue(countMain(player, Items.DIRT) == 64, "retry without space must not grow inventory");
        helper.assertTrue(escrowOf(player).size() == 1 && escrowOf(player).get(0).getCount() == 12,
                "retry without space must keep the exact remainder once");
        assertNoItemDrops(helper, player);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 100)
    public static void freeingCapacityThenDrainReturnsNamedNbtOnce(GameTestHelper helper) {
        FakePlayer player = player(helper, "NRLegFree", GameType.SURVIVAL);
        fillMain(player, new ItemStack(Items.DIRT, 64));
        ItemStack ring = new ItemStack(ModItems.neutron_ring.get());
        ItemStack heirloom = new ItemStack(Items.DIAMOND_SWORD);
        heirloom.setHoverName(Component.literal(HEIRLOOM));
        heirloom.setDamageValue(12);
        putInRing(ring, heirloom);

        NeutronRingLegacyRecovery.recover(player, ring);
        helper.assertTrue(countNamed(player, HEIRLOOM) == 0, "full inventory cannot receive the named sword");
        List<ItemStack> escrow = escrowOf(player);
        helper.assertTrue(escrow.size() == 1 && HEIRLOOM.equals(escrow.get(0).getHoverName().getString())
                        && escrow.get(0).getDamageValue() == 12,
                "named damaged sword remains in escrow");

        NeutronRingLegacyRecovery.recover(player, ring);
        helper.assertTrue(countNamed(player, HEIRLOOM) == 0 && escrowOf(player).size() == 1,
                "drain without space must not duplicate or drop the sword");

        player.getInventory().items.set(0, ItemStack.EMPTY);
        NeutronRingLegacyRecovery.recover(player, ring);
        helper.assertTrue(countNamed(player, HEIRLOOM) == 1, "freed slot should receive the named sword once");
        helper.assertTrue(escrowOf(player).isEmpty(), "escrow empties after the sword is accepted");
        ItemStack restored = findNamed(player, HEIRLOOM);
        helper.assertTrue(restored.is(Items.DIAMOND_SWORD) && restored.getDamageValue() == 12,
                "restored sword keeps item and damage NBT");

        NeutronRingLegacyRecovery.recover(player, ring);
        helper.assertTrue(countNamed(player, HEIRLOOM) == 1 && escrowOf(player).isEmpty(),
                "later drain must not clone the restored sword");
        assertNoItemDrops(helper, player);
        helper.succeed();
    }

    private static FakePlayer player(GameTestHelper helper, String name, GameType mode) {
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.setGameMode(mode);
        player.getInventory().clearContent();
        BlockPos origin = helper.absolutePos(new BlockPos(5, 2, 5));
        player.moveTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, 0.0F, 0.0F);
        return player;
    }

    private static void fillMain(ServerPlayer player, ItemStack template) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.items.size(); i++) {
            inventory.items.set(i, template.copy());
        }
    }

    private static void putInRing(ItemStack ring, ItemStack... stacks) {
        IItemHandler handler = ring.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .orElseThrow(() -> new AssertionError("Legacy ring inventory capability missing"));
        IItemHandlerModifiable modifiable = (IItemHandlerModifiable) handler;
        for (int i = 0; i < stacks.length; i++) {
            modifiable.setStackInSlot(i, stacks[i]);
        }
    }

    private static boolean ringEmpty(ItemStack ring) {
        IItemHandler handler = ring.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .orElseThrow(() -> new AssertionError("Legacy ring inventory capability missing"));
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean recovered(ItemStack ring) {
        return ring.hasTag() && ring.getTag().getBoolean(NeutronRingLegacyRecovery.RECOVERED_TAG);
    }

    private static List<ItemStack> escrowOf(ServerPlayer player) {
        ListTag owners = NeutronRingLegacyRecovery.get(player.server).save(new CompoundTag())
                .getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < owners.size(); i++) {
            CompoundTag owner = owners.getCompound(i);
            if (!player.getUUID().equals(NbtUtils.loadUUID(owner.get("Id")))) {
                continue;
            }
            ListTag stored = owner.getList("Items", Tag.TAG_COMPOUND);
            List<ItemStack> items = new ArrayList<>();
            for (int j = 0; j < stored.size(); j++) {
                items.add(ItemStack.of(stored.getCompound(j)));
            }
            return items;
        }
        return List.of();
    }

    private static int countMain(ServerPlayer player, Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int countNamed(ServerPlayer player, String name) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && name.equals(stack.getHoverName().getString())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static ItemStack findNamed(ServerPlayer player, String name) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && name.equals(stack.getHoverName().getString())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void assertNoItemDrops(GameTestHelper helper, ServerPlayer player) {
        helper.assertEntityNotPresent(EntityType.ITEM);
        AABB area = player.getBoundingBox().inflate(16.0);
        List<ItemEntity> nearby = player.serverLevel().getEntities(EntityType.ITEM, area, entity -> true);
        helper.assertTrue(nearby.isEmpty(), "recovery must not drop leftover stacks into the world");
    }
}
