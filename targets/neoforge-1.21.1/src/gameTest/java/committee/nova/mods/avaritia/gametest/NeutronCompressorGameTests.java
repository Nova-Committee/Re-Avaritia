package committee.nova.mods.avaritia.gametest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

@GameTestHolder(Const.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NeutronCompressorGameTests {
    private NeutronCompressorGameTests() {
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 120)
    public static void activeInputWithBlockedOutputKeepsStacksBounded(GameTestHelper helper) {
        var level = helper.getLevel();
        List<Block> blocks = List.of(ModBlocks.neutron_compressor.get(), ModBlocks.dense_neutron_compressor.get(),
                ModBlocks.denser_neutron_compressor.get(), ModBlocks.densest_neutron_compressor.get());
        for (int index = 0; index < blocks.size(); index++) {
            BlockPos relative = new BlockPos(index * 3 + 1, 1, 1);
            helper.setBlock(relative, blocks.get(index));
            helper.setBlock(relative.above(), Blocks.CHEST);
            var compressor = (NeutronCompressorTile) level.getBlockEntity(helper.absolutePos(relative));
            var chest = (ChestBlockEntity) level.getBlockEntity(helper.absolutePos(relative.above()));
            ItemStack material = new ItemStack(Items.DEEPSLATE, 64);
            material.set(DataComponents.CUSTOM_NAME, Component.literal("issue-386-material"));
            // Learn the real loaded recipe, then prime the next active-IO tick with a full input.
            compressor.getInventory().setStackInSlot(1, material.copy());
            NeutronCompressorTile.tick(level, compressor.getBlockPos(), compressor.getBlockState(), compressor);
            helper.assertTrue(compressor.hasRecipe(), "the built-in deepslate compressor recipe must be loaded");
            var saved = compressor.saveWithFullMetadata(level.registryAccess());
            saved.putInt("MaterialCount", 0);
            saved.putInt("Progress", 0);
            compressor.loadWithComponents(saved, level.registryAccess());
            compressor.getInventory().setStackInSlot(0, new ItemStack(Items.BEDROCK, 64));
            compressor.getInventory().setStackInSlot(1, material.copy());
            chest.setItem(0, material.copy());
            compressor.getSideConfiguration().setSideMode(Direction.UP, SideConfiguration.SideMode.ACTIVE_INPUT);
            // Nineteen empty ticks set the real active-IO timer to its boundary. Restore the full
            // input just before it pulls from the chest, as a faster external supplier can do.
            compressor.getInventory().setStackInSlot(1, ItemStack.EMPTY);
            for (int tick = 0; tick < 18; tick++) {
                NeutronCompressorTile.tick(level, compressor.getBlockPos(), compressor.getBlockState(), compressor);
            }
            compressor.getInventory().setStackInSlot(1, material.copy());
            NeutronCompressorTile.tick(level, compressor.getBlockPos(), compressor.getBlockState(), compressor);
            saved = compressor.saveWithFullMetadata(level.registryAccess());
            saved.putInt("MaterialCount", compressor.getMaterialsRequired());
            saved.putInt("Progress", 0);
            compressor.loadWithComponents(saved, level.registryAccess());
            compressor.getInventory().setStackInSlot(1, material.copy());
            chest.setItem(0, material.copy());
        }
        // No menu/player is created. The real server ticker runs active IO every twenty ticks.
        helper.runAfterDelay(60, () -> {
            for (int index = 0; index < blocks.size(); index++) {
                BlockPos relative = new BlockPos(index * 3 + 1, 1, 1);
                var compressor = (NeutronCompressorTile) level.getBlockEntity(helper.absolutePos(relative));
                var chest = (ChestBlockEntity) level.getBlockEntity(helper.absolutePos(relative.above()));
                int input = compressor.getInventory().getStackInSlot(1).getCount();
                int external = chest.getItem(0).getCount();
                helper.assertTrue(input == 64 && external == 64,
                        "a full input must not drain its neighbor: input=" + input + ", external=" + external);
                var packet = compressor.getUpdatePacket();
                var restored = (NeutronCompressorTile) BlockEntity.loadStatic(compressor.getBlockPos(),
                        compressor.getBlockState(), roundTripPacket(helper, packet).getTag(), level.registryAccess());
                helper.assertTrue(restored.getMaterialCount() == compressor.getMaterialsRequired()
                                && restored.getInventory().getStackInSlot(1).getCount() == 64,
                        "packet persistence must preserve the material buffer and input quantities");
            }
            helper.succeed();
        });
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void partialActiveInputConservesItemsAndComponents(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), ModBlocks.densest_neutron_compressor.get());
        var compressor = (NeutronCompressorTile) helper.getLevel().getBlockEntity(helper.absolutePos(new BlockPos(1, 1, 1)));
        ItemStack material = new ItemStack(Items.DEEPSLATE, 60);
        material.set(DataComponents.CUSTOM_NAME, Component.literal("issue-386-components"));
        compressor.getInventory().setStackInSlot(1, material.copy());
        NeutronCompressorTile.tick(helper.getLevel(), compressor.getBlockPos(), compressor.getBlockState(), compressor);
        compressor.getInventory().setStackInSlot(1, material.copy());
        ItemStackHandler source = new ItemStackHandler(1);
        source.setStackInSlot(0, material.copyWithCount(64));
        compressor.extractFromHandler(source, Direction.UP);
        helper.assertTrue(compressor.getInventory().getStackInSlot(1).getCount() == 64
                        && source.getStackInSlot(0).getCount() == 60,
                "only four available input positions may be filled, with no loss or duplication");
        var packet = compressor.getUpdatePacket();
        var restored = (NeutronCompressorTile) BlockEntity.loadStatic(compressor.getBlockPos(),
                compressor.getBlockState(), roundTripPacket(helper, packet).getTag(), helper.getLevel().registryAccess());
        helper.assertTrue(ItemStack.isSameItemSameComponents(material, restored.getMaterialStack())
                        && restored.getMaterialCount() == 60,
                "material identity components and separate accumulated quantity must survive serialization");
        helper.succeed();
    }

    @GameTest(template = "portable_ui_empty", timeoutTicks = 100)
    public static void oversizedMaterialInputStillSavesIdentity(GameTestHelper helper) {
        BlockPos relative = new BlockPos(1, 1, 1);
        helper.setBlock(relative, ModBlocks.densest_neutron_compressor.get());
        var compressor = (NeutronCompressorTile) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
        ItemStack material = new ItemStack(Items.DEEPSLATE, 128);
        material.set(DataComponents.CUSTOM_NAME, Component.literal("issue-386-128"));
        // Existing ItemStackWrapper saves integer counts, including old oversized input slots.
        compressor.getInventory().setStackInSlot(1, material.copy());
        NeutronCompressorTile.tick(helper.getLevel(), compressor.getBlockPos(), compressor.getBlockState(), compressor);
        helper.assertTrue(compressor.getMaterialCount() == 128 && compressor.getInventory().getStackInSlot(1).isEmpty(),
                "processing an oversized old input must account for every item");
        var packet = compressor.getUpdatePacket();
        var restored = (NeutronCompressorTile) BlockEntity.loadStatic(compressor.getBlockPos(),
                compressor.getBlockState(), roundTripPacket(helper, packet).getTag(), helper.getLevel().registryAccess());
        helper.assertTrue(restored.getMaterialCount() == 128
                        && ItemStack.isSameItemSameComponents(material, restored.getMaterialStack()),
                "the saved material identity must retain components while its actual quantity remains 128");
        helper.succeed();
    }

    private static ClientboundBlockEntityDataPacket roundTripPacket(GameTestHelper helper, ClientboundBlockEntityDataPacket packet) {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            ClientboundBlockEntityDataPacket.STREAM_CODEC.encode(buffer, packet);
            return ClientboundBlockEntityDataPacket.STREAM_CODEC.decode(buffer);
        } finally {
            buffer.release();
        }
    }
}
