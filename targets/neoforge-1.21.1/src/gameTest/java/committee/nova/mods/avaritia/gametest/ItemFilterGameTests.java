package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.net.C2SItemFilterPack;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import io.netty.buffer.Unpooled;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.UUID;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ItemFilterGameTests {
    private ItemFilterGameTests() {
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void submissionRoundTripsAndPersists(GameTestHelper helper) {
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "FilterGT"));
        ItemStack tool = new ItemStack(ModItems.infinity_pickaxe.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        try {
            submit(helper, player, new ItemStack(Items.DIRT), 0);
            CompoundTag first = tool.get(ModDataComponents.TOOL_FILTERS.get());
            helper.assertTrue(first != null && first.contains("minecraft:dirt"), "ordinary item without custom data must be stored");

            CompoundTag data = new CompoundTag();
            data.putString("filter_probe", "retained");
            ItemStack stone = new ItemStack(Items.STONE);
            stone.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            submit(helper, player, stone, 0);
            helper.assertTrue(!first.contains("minecraft:stone"), "filter update must not mutate an already-observed component");

            ItemStack restored = ItemStack.parse(helper.getLevel().registryAccess(), tool.save(helper.getLevel().registryAccess())).orElseThrow();
            CompoundTag persisted = restored.get(ModDataComponents.TOOL_FILTERS.get());
            helper.assertTrue(persisted != null && persisted.contains("minecraft:dirt")
                    && "retained".equals(persisted.getCompound("minecraft:stone").getString("filter_probe")),
                    "both filter entries and custom data must survive item save/load");
            player.setItemInHand(InteractionHand.MAIN_HAND, restored);
            submit(helper, player, new ItemStack(Items.DIRT), 1);
            CompoundTag remaining = restored.get(ModDataComponents.TOOL_FILTERS.get());
            helper.assertTrue(!remaining.contains("minecraft:dirt") && remaining.contains("minecraft:stone"),
                    "remove must retain the other filter");
            submit(helper, player, new ItemStack(Items.DIRT), 0);
            submit(helper, player, ItemStack.EMPTY, 2);
            helper.assertTrue(restored.get(ModDataComponents.TOOL_FILTERS.get()).isEmpty(), "clear must safely remove every entry");

            ItemStack ordinaryTool = new ItemStack(Items.IRON_PICKAXE);
            player.setItemInHand(InteractionHand.MAIN_HAND, ordinaryTool);
            submit(helper, player, stone, 0);
            helper.assertTrue(!ordinaryTool.has(ModDataComponents.TOOL_FILTERS.get()), "non-filter items must not accept filter mutations");
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        helper.succeed();
    }

    private static void submit(GameTestHelper helper, FakePlayer player, ItemStack stack, int action) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            // Exercise Minecraft's registered serverbound envelope, not just the payload's private codec.
            ServerboundCustomPayloadPacket.STREAM_CODEC.encode(buffer, new ServerboundCustomPayloadPacket(new C2SItemFilterPack(stack, action)));
            var payload = ServerboundCustomPayloadPacket.STREAM_CODEC.decode(buffer).payload();
            helper.assertTrue(payload instanceof C2SItemFilterPack, "serverbound filter submission must decode as its registered payload");
            new C2SItemFilterPack.Handler().handle((C2SItemFilterPack) payload,
                    new ServerPayloadContext(player.connection, C2SItemFilterPack.TYPE.id()));
        } finally {
            buffer.release();
        }
    }
}
