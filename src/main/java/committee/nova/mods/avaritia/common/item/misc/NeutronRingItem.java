package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import committee.nova.mods.avaritia.api.utils.InventoryUtils;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.common.item.resources.ResourceItem;
import committee.nova.mods.avaritia.common.net.S2CNeutronRingOpenPack;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** Pocket structure library bound to the player UUID, not the stack. */
public class NeutronRingItem extends ResourceItem {
    public NeutronRingItem() {
        super(ModRarities.EPIC, true, new Properties().stacksTo(1));
    }

    public static ItemStack find(Player player) {
        return InventoryUtils.findItemInInv(player, stack -> stack.is(ModItems.neutron_ring.get()), stack -> stack);
    }

    public static ItemStack resolve(Player player, int hand) {
        ItemStack held = hand == 1 ? player.getOffhandItem() : player.getMainHandItem();
        if (held.is(ModItems.neutron_ring.get())) {
            return held;
        }
        return find(player);
    }

    public static NeutronRingContents contents(ItemStack stack) {
        NeutronRingContents data = stack.get(ModDataComponents.NEUTRON_RING.get());
        if (data != null) {
            return data;
        }
        data = NeutronRingContents.create();
        stack.set(ModDataComponents.NEUTRON_RING.get(), data);
        return data;
    }

    public static NeutronRingContents bind(ServerPlayer player, ItemStack stack) {
        NeutronRingContents data = contents(stack);
        UUID owner = player.getUUID();
        if (!data.playerBound()) {
            if (NeutronRingSavedData.get(player.server).adopt(data.storageId(), owner)) {
                data = new NeutronRingContents(owner, data.captureBase(), data.placeBase(), data.selectedId(),
                        data.size(), true);
                stack.set(ModDataComponents.NEUTRON_RING.get(), data);
            }
            return data;
        }
        if (!data.storageId().equals(owner)) {
            data = NeutronRingContents.owned(owner);
            stack.set(ModDataComponents.NEUTRON_RING.get(), data);
        }
        return data;
    }

    public static NeutronRingContents libraryOf(ServerPlayer player) {
        ItemStack stack = find(player);
        return stack.isEmpty() ? NeutronRingContents.owned(player.getUUID()) : bind(player, stack);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        NeutronRingContents data = stack.get(ModDataComponents.NEUTRON_RING.get());
        return data != null && data.selectedId().isPresent();
    }

    public static void openGui(ServerPlayer player) {
        if (find(player).isEmpty()) {
            return;
        }
        NeutronRingContents data = libraryOf(player);
        InteractionHand hand = player.getOffhandItem().is(ModItems.neutron_ring.get())
                && !player.getMainHandItem().is(ModItems.neutron_ring.get())
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        PacketDistributor.sendToPlayer(player, S2CNeutronRingOpenPack.from(player.server, data, hand));
    }

    public static void openGui(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        PacketDistributor.sendToPlayer(player, S2CNeutronRingOpenPack.from(player.server, bind(player, stack), hand));
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(@NotNull ItemStack stack, @NotNull UseOnContext context) {
        return handleBlockUse(stack, context);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        return handleBlockUse(context.getItemInHand(), context);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                openGui(serverPlayer, stack, hand);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        return execute(stack, player, level)
                ? InteractionResultHolder.sidedSuccess(stack, false)
                : InteractionResultHolder.fail(stack);
    }

    private InteractionResult handleBlockUse(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                openGui(serverPlayer, stack, context.getHand());
            }
            return InteractionResult.SUCCESS;
        }
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }
        NeutronRingContents data = bind(serverPlayer, stack);
        GlobalPos clicked = GlobalPos.of(level.dimension(), context.getClickedPos());
        boolean placing = data.selectedId().isPresent();
        GlobalPos current = placing ? data.placeBase().orElse(null) : data.captureBase().orElse(null);
        if (current == null || !current.equals(clicked)) {
            stack.set(ModDataComponents.NEUTRON_RING.get(),
                    placing ? data.withPlace(clicked) : data.withCapture(clicked));
            player.displayClientMessage(Component.translatable(placing
                    ? "message.avaritia.neutron_ring.place_preview"
                    : "message.avaritia.neutron_ring.base_set"), true);
            return InteractionResult.SUCCESS;
        }
        return execute(stack, serverPlayer, level) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    private boolean execute(ItemStack stack, Player player, Level level) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        NeutronRingContents data = bind(serverPlayer, stack);
        NeutronRingSavedData store = NeutronRingSavedData.get(serverLevel.getServer());
        UUID library = data.storageId();
        if (data.selectedId().isPresent()) {
            if (data.placeBase().isEmpty() || !NeutronRingSpaces.sameDimension(level, data.placeBase().get())) {
                player.displayClientMessage(Component.translatable("message.avaritia.neutron_ring.no_place"), true);
                return false;
            }
            var space = store.get(library, data.selectedId().get());
            if (space.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.avaritia.neutron_ring.missing"), true);
                return false;
            }
            if (!NeutronRingSpaces.place(serverLevel, data.placeBase().get().pos(), space.get().template())) {
                player.displayClientMessage(Component.translatable("message.avaritia.neutron_ring.place_failed"), true);
                return false;
            }
            store.remove(library, data.selectedId().get());
            stack.set(ModDataComponents.NEUTRON_RING.get(), data.deselect());
            player.displayClientMessage(Component.translatable("message.avaritia.neutron_ring.placed"), true);
            return true;
        }
        if (data.captureBase().isEmpty() || !NeutronRingSpaces.sameDimension(level, data.captureBase().get())) {
            player.displayClientMessage(Component.translatable("message.avaritia.neutron_ring.no_base"), true);
            return false;
        }
        if (store.list(library).size() >= NeutronRingSavedData.MAX_SPACES) {
            player.displayClientMessage(Component.translatable("message.avaritia.neutron_ring.full"), true);
            return false;
        }
        BlockPos base = data.captureBase().get().pos();
        NeutronRingContents.Size captureSize = NeutronRingContents.Size.DEFAULT;
        String name = Component.translatable("gui.avaritia.neutron_ring.default_name", store.list(library).size() + 1)
                .getString();
        if (!store.add(library, name, NeutronRingSpaces.capture(serverLevel, base, captureSize))) {
            player.displayClientMessage(Component.translatable("message.avaritia.neutron_ring.full"), true);
            return false;
        }
        NeutronRingSpaces.clear(serverLevel, base, captureSize);
        stack.set(ModDataComponents.NEUTRON_RING.get(), data.clearCapture());
        player.displayClientMessage(Component.translatable("message.avaritia.neutron_ring.saved", name), true);
        return true;
    }
}
