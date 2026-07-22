package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.container.DummyChannelContainer;
import committee.nova.mods.avaritia.common.tile.TesseractTile;
import committee.nova.mods.avaritia.core.channel.Channel;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import committee.nova.mods.avaritia.core.channel.NullChannel;
import committee.nova.mods.avaritia.core.channel.ServerChannel;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/** Complete Tesseract storage/crafting menu. All virtual resource clicks are resolved server-side. */
public class TesseractMenu extends AbstractContainerMenu {
    public static final int PLAYER_START = 0;
    public static final int PLAYER_END = 36;
    public static final int CRAFT_START = 36;
    public static final int CRAFT_END = 45;
    public static final int RESULT_SLOT = 45;
    public static final int CHANNEL_START = 46;
    public static final int CHANNEL_SLOTS = DummyChannelContainer.SIZE;

    private static final int[] CRAFT_COUNTS = {1, 8, 64, 512};

    private final DummyChannelContainer projected = new DummyChannelContainer();
    private final TransientCraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private final BlockPos pos;
    private final @Nullable TesseractTile tile;
    private final UUID owner;
    private final Player player;
    private String filter;
    private byte sortType;
    private byte viewType;
    private boolean locked;
    private boolean craftingMode;

    private final ContainerData terminalData = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> locked ? 1 : 0;
                case 1 -> craftingMode ? 1 : 0;
                case 2 -> sortType;
                case 3 -> viewType;
                default -> 0;
            };
        }

        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> locked = value != 0;
                case 1 -> craftingMode = value != 0;
                case 2 -> sortType = sanitizeSort((byte) value);
                case 3 -> viewType = sanitizeView((byte) value);
                default -> { }
            }
            refresh(currentData());
        }

        @Override public int getCount() { return 4; }
    };

    public TesseractMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, readInitialData(buffer));
    }

    private TesseractMenu(int id, Inventory inventory, InitialData initial) {
        this(id, inventory, initial.pos(),
                inventory.player.level().getBlockEntity(initial.pos()) instanceof TesseractTile found ? found : null,
                initial.owner(), initial.locked(), initial.craftingMode(), initial.filter(),
                initial.sortType(), initial.viewType());
    }

    public TesseractMenu(int id, Inventory inventory, TesseractTile tile) {
        this(id, inventory, tile.getBlockPos(), tile, tile.getTerminalOwner(), tile.isLocked(),
                tile.isCraftingMode(), tile.getFilter(), tile.getSortType(), tile.getViewType());
    }

    private TesseractMenu(int id, Inventory inventory, BlockPos pos, @Nullable TesseractTile tile,
                          UUID owner, boolean locked, boolean craftingMode, String filter,
                          byte sortType, byte viewType) {
        super(ModMenus.tesseract.get(), id);
        this.pos = pos;
        this.tile = tile;
        this.owner = owner;
        this.player = inventory.player;
        this.locked = locked;
        this.craftingMode = craftingMode;
        this.filter = limit(filter);
        this.sortType = sanitizeSort(sortType);
        this.viewType = sanitizeView(viewType);

        // Keep player slots first so virtual channel slots never participate in vanilla transfers.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 23 + column * 17, 188 + row * 17));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 23 + column * 17, 240));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new CraftSlot(craftSlots, column + row * 3, 92 + column * 17, 136 + row * 17));
            }
        }
        addSlot(new CraftResultSlot(player, craftSlots, resultSlots, 0, 161, 153));
        for (int row = 0; row < 9; row++) {
            for (int column = 0; column < 11; column++) {
                int visible = column + row * 11;
                addSlot(new ChannelSlot(projected, visible, 7 + column * 17, 17 + row * 17, visible));
            }
        }
        addDataSlots(terminalData);

        if (inventory.player instanceof ServerPlayer serverPlayer && tile != null) {
            channel().addListener(serverPlayer);
            refresh(channel().data());
        } else {
            ClientChannelManager.getInstance().channel().listen(() ->
                    refresh(ClientChannelManager.getInstance().channel().data()));
            refresh(ClientChannelManager.getInstance().channel().data());
        }
    }

    public BlockPos getBlockPos() { return pos; }
    public DummyChannelContainer projected() { return projected; }
    public String filter() { return filter; }
    public byte sortType() { return sortType; }
    public byte viewType() { return viewType; }
    public boolean isLocked() { return locked; }
    public boolean isCraftingMode() { return craftingMode; }
    public UUID owner() { return owner; }

    public boolean canPlayerModify(Player player) {
        return tile != null && tile.canPlayerModify(player);
    }

    public void setFilterFromClient(String next) {
        filter = limit(next);
        if (tile != null) tile.setFilter(filter);
        refresh(currentData());
        broadcastChanges();
    }

    public void setViewFromClient(byte sort, byte view) {
        sortType = sanitizeSort(sort);
        viewType = sanitizeView(view);
        saveSettings();
        refresh(currentData());
        broadcastChanges();
    }

    /** Compatibility entry point for the explicit action payload. */
    public void withdraw(int visibleSlot, int requestedAmount, ServerPlayer serverPlayer) {
        if (!validVirtualClick(visibleSlot, serverPlayer)) return;
        DummyChannelContainer.Entry entry = projected.entry(visibleSlot);
        if (entry.kind() != DummyChannelContainer.Kind.ITEM) return;
        giveToInventory(entry.item(), requestedAmount, serverPlayer);
        refresh(channel().data());
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer) || id < 0 || id > 17
                || !stillValid(player) || !canPlayerModify(player)) return false;
        switch (id) {
            case 0 -> {
                if (owner.equals(player.getUUID()) || Const.AVARITIA_FAKE_PLAYER.id().equals(owner)
                        && serverPlayer.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                    locked = !locked;
                    if (tile != null) tile.setLocked(locked);
                }
            }
            case 1 -> {
                craftingMode = !craftingMode;
                if (!craftingMode) returnCraftGrid(player);
                if (tile != null) tile.setCraftingMode(craftingMode);
            }
            case 2 -> sortType = (byte) ((sortType + 2) & 7);
            case 3 -> sortType = (byte) (sortType ^ 1);
            case 4 -> viewType = (byte) ((viewType + 1) % 3);
            case 5 -> {
                if (tile != null) tile.openSelector(serverPlayer);
                return true;
            }
            default -> {
                int action = id - 6;
                craftTo(action / 4, CRAFT_COUNTS[action % 4], serverPlayer);
            }
        }
        saveSettings();
        refresh(channel().data());
        broadcastChanges();
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        int visibleSlot = slotId - CHANNEL_START;
        if (visibleSlot >= 0 && visibleSlot < CHANNEL_SLOTS) {
            if (player instanceof ServerPlayer serverPlayer && validVirtualClick(visibleSlot, player)) {
                DummyChannelContainer.Entry entry = projected.entry(visibleSlot);
                switch (entry.kind()) {
                    case ITEM -> clickItem(entry.item(), button, clickType, serverPlayer);
                    case FLUID -> {
                        if (supportsContainerTransfer(clickType)) clickFluid(entry.fluid(), button, serverPlayer);
                    }
                    case ENERGY -> {
                        if (supportsContainerTransfer(clickType)) clickEnergy(button, serverPlayer);
                    }
                    case EMPTY -> {
                        ItemStack carried = getCarried();
                        if (!carried.isEmpty()) {
                            clickItem(ItemResource.of(carried), button, clickType, serverPlayer);
                        }
                    }
                }
                refresh(channel().data());
                broadcastChanges();
            }
            return;
        }
        if (player instanceof ServerPlayer && (!stillValid(player) || !canPlayerModify(player))) return;
        super.clicked(slotId, button, clickType, player);
    }

    private boolean validVirtualClick(int visibleSlot, Player player) {
        return visibleSlot >= 0 && visibleSlot < (craftingMode ? 77 : CHANNEL_SLOTS)
                && stillValid(player) && canPlayerModify(player);
    }

    private static boolean supportsContainerTransfer(ContainerInput input) {
        return input == ContainerInput.PICKUP || input == ContainerInput.QUICK_MOVE
                || input == ContainerInput.QUICK_CRAFT;
    }

    private void clickItem(ItemResource resource, int button, ContainerInput input, ServerPlayer serverPlayer) {
        if (resource.isEmpty()) return;
        int maxStack = resource.toStack(1).getMaxStackSize();
        switch (input) {
            case QUICK_MOVE -> giveToInventory(resource, maxStack, serverPlayer);
            case THROW -> {
                ItemStack extracted = channel().extract(resource, button == 0 ? 1 : maxStack);
                if (!extracted.isEmpty()) serverPlayer.drop(extracted, false);
            }
            case CLONE -> {
                if (serverPlayer.getAbilities().instabuild) setCarried(resource.toStack(maxStack));
            }
            case PICKUP, PICKUP_ALL, QUICK_CRAFT -> {
                ItemStack carried = getCarried();
                if (carried.isEmpty()) {
                    setCarried(channel().extract(resource, button == 1 ? 1 : maxStack));
                } else {
                    ItemStack moving = carried.copyWithCount(button == 1 ? 1 : carried.getCount());
                    int before = moving.getCount();
                    channel().insert(moving);
                    carried.shrink(before - moving.getCount());
                    setCarried(carried);
                }
            }
            default -> { }
        }
    }

    private void clickFluid(FluidResource resource, int button, ServerPlayer serverPlayer) {
        ItemAccess access = ItemAccess.forPlayerCursor(serverPlayer, this).oneByOne();
        ResourceHandler<FluidResource> cursor = access.getCapability(Capabilities.Fluid.ITEM);
        if (cursor == null) return;
        try (Transaction transaction = Transaction.openRoot()) {
            if (button == 1) {
                for (int index = 0; index < cursor.size(); index++) {
                    FluidResource stored = cursor.getResource(index);
                    if (stored.isEmpty() || !stored.equals(resource)) continue;
                    int extracted = cursor.extract(index, stored, Integer.MAX_VALUE, transaction);
                    int inserted = channel().fluids().insert(stored, extracted, transaction);
                    if (inserted == extracted && inserted > 0) { transaction.commit(); return; }
                    return;
                }
            } else {
                int available = (int) Math.min(Integer.MAX_VALUE, channel().fluidAmount(resource));
                int inserted = cursor.insert(resource, available, transaction);
                int extracted = channel().fluids().extract(resource, inserted, transaction);
                if (inserted == extracted && inserted > 0) transaction.commit();
            }
        }
    }

    private void clickEnergy(int button, ServerPlayer serverPlayer) {
        ItemAccess access = ItemAccess.forPlayerCursor(serverPlayer, this).oneByOne();
        EnergyHandler cursor = access.getCapability(Capabilities.Energy.ITEM);
        if (cursor == null) return;
        try (Transaction transaction = Transaction.openRoot()) {
            if (button == 1) {
                int extracted = cursor.extract(Integer.MAX_VALUE, transaction);
                int inserted = channel().energy().insert(extracted, transaction);
                if (inserted == extracted && inserted > 0) transaction.commit();
            } else {
                int available = (int) Math.min(Integer.MAX_VALUE, channel().energyAmount());
                int inserted = cursor.insert(available, transaction);
                int extracted = channel().energy().extract(inserted, transaction);
                if (inserted == extracted && inserted > 0) transaction.commit();
            }
        }
    }

    private void giveToInventory(ItemResource resource, int amount, ServerPlayer serverPlayer) {
        ItemStack extracted = channel().extract(resource, Math.min(amount, resource.toStack(1).getMaxStackSize()));
        if (extracted.isEmpty()) return;
        serverPlayer.getInventory().add(extracted);
        if (!extracted.isEmpty()) channel().insert(extracted);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!(player instanceof ServerPlayer) || index < 0 || index >= CHANNEL_START
                || !stillValid(player) || !canPlayerModify(player)) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack moving = slot.getItem();
        ItemStack original = moving.copy();
        if (index < PLAYER_END || index >= CRAFT_START && index < CRAFT_END) {
            channel().insert(moving);
            slot.setChanged();
        } else if (index == RESULT_SLOT) {
            if (!moveItemStackTo(moving, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(moving, original);
        }
        if (moving.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (moving.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, moving);
        refresh(channel().data());
        return original;
    }

    @Override
    public void slotsChanged(Container container) {
        if (!(player instanceof ServerPlayer) || player.level().isClientSide()) return;
        CraftingInput input = CraftingInput.of(3, 3, craftSlots.getItems());
        ServerPlayer serverPlayer = (ServerPlayer) player;
        RecipeHolder<CraftingRecipe> recipe = serverPlayer.level().recipeAccess()
                .getRecipeFor(RecipeType.CRAFTING, input, serverPlayer.level()).orElse(null);
        if (recipe == null) {
            resultSlots.setRecipeUsed(null);
            resultSlots.setItem(0, ItemStack.EMPTY);
        } else {
            resultSlots.setRecipeUsed(recipe);
            resultSlots.setItem(0, recipe.value().assemble(input));
        }
        broadcastChanges();
    }

    private void craftTo(int target, int maximum, ServerPlayer serverPlayer) {
        if (!craftingMode) return;
        List<ItemStack> templates = craftSlots.getItems().stream()
                .map(stack -> stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1)).toList();
        for (int crafted = 0; crafted < maximum; crafted++) {
            CraftingInput input = CraftingInput.of(3, 3, craftSlots.getItems());
            RecipeHolder<CraftingRecipe> recipe = serverPlayer.level().recipeAccess()
                    .getRecipeFor(RecipeType.CRAFTING, input, serverPlayer.level()).orElse(null);
            if (recipe == null) break;
            ItemStack result = recipe.value().assemble(input);
            if (result.isEmpty()) break;
            if (target == 0) {
                if (!insertWholeIntoChannel(result)) break;
            } else if (target == 1) {
                if (!canInventoryAccept(serverPlayer.getInventory(), result)) break;
                serverPlayer.getInventory().add(result);
                if (!result.isEmpty()) break;
            } else {
                serverPlayer.drop(result, false);
            }
            consumeRecipe(recipe, input, serverPlayer);
            if (!refillTemplates(templates, serverPlayer)) break;
        }
        slotsChanged(craftSlots);
    }

    private void consumeRecipe(RecipeHolder<CraftingRecipe> recipe, CraftingInput input, ServerPlayer serverPlayer) {
        NonNullList<ItemStack> remaining = recipe.value().getRemainingItems(input);
        for (int index = 0; index < craftSlots.getContainerSize(); index++) {
            craftSlots.removeItem(index, 1);
            ItemStack remainder = remaining.get(index);
            if (remainder.isEmpty()) continue;
            routeStack(remainder, serverPlayer);
        }
    }

    private boolean refillTemplates(List<ItemStack> templates, ServerPlayer serverPlayer) {
        for (int index = 0; index < templates.size(); index++) {
            ItemStack template = templates.get(index);
            if (template.isEmpty()) continue;
            ItemStack existing = craftSlots.getItem(index);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, template)) continue;
            if (!existing.isEmpty()) {
                ItemStack displaced = craftSlots.removeItemNoUpdate(index);
                routeStack(displaced, serverPlayer);
            }
            ItemResource resource = ItemResource.of(template);
            ItemStack next = channel().extract(resource, 1);
            if (next.isEmpty()) next = takeOneFromInventory(serverPlayer.getInventory(), template);
            if (next.isEmpty()) return false;
            craftSlots.setItem(index, next);
        }
        return true;
    }

    private boolean insertWholeIntoChannel(ItemStack stack) {
        ItemResource resource = ItemResource.of(stack);
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = channel().items().insert(resource, stack.getCount(), transaction);
            if (inserted != stack.getCount()) return false;
            transaction.commit();
            stack.shrink(inserted);
            return true;
        }
    }

    private void routeStack(ItemStack stack, ServerPlayer serverPlayer) {
        channel().insert(stack);
        if (!stack.isEmpty()) serverPlayer.getInventory().add(stack);
        if (!stack.isEmpty()) serverPlayer.drop(stack, false);
    }

    private static ItemStack takeOneFromInventory(Inventory inventory, ItemStack template) {
        for (int index = 0; index < inventory.getContainerSize(); index++) {
            ItemStack available = inventory.getItem(index);
            if (ItemStack.isSameItemSameComponents(available, template)) {
                return inventory.removeItem(index, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean canInventoryAccept(Inventory inventory, ItemStack stack) {
        int remaining = stack.getCount();
        for (int index = 0; index < inventory.getContainerSize(); index++) {
            ItemStack existing = inventory.getItem(index);
            if (existing.isEmpty()) {
                remaining -= stack.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                remaining -= Math.max(0, existing.getMaxStackSize() - existing.getCount());
            }
            if (remaining <= 0) return true;
        }
        return false;
    }

    private void returnCraftGrid(Player player) {
        for (int index = 0; index < craftSlots.getContainerSize(); index++) {
            ItemStack stack = craftSlots.removeItemNoUpdate(index);
            if (stack.isEmpty()) continue;
            if (player instanceof ServerPlayer) channel().insert(stack);
            if (!stack.isEmpty()) player.getInventory().add(stack);
            if (!stack.isEmpty()) player.drop(stack, false);
        }
        resultSlots.clearContent();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return tile != null && tile.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        if (!player.level().isClientSide()) returnCraftGrid(player);
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer) channel().removeListener(serverPlayer);
        else ClientChannelManager.getInstance().channel().clearListener();
    }

    private ServerChannel channel() { return tile == null ? NullChannel.INSTANCE : tile.getChannel(); }
    private Channel.Data currentData() { return tile == null ? ClientChannelManager.getInstance().channel().data() : channel().data(); }
    private void refresh(Channel.Data data) { projected.refresh(data, filter, sortType, viewType); }

    private void saveSettings() {
        if (tile == null) return;
        tile.setLocked(locked);
        tile.setCraftingMode(craftingMode);
        tile.setFilter(filter);
        tile.setSortType(sortType);
        tile.setViewType(viewType);
    }

    private static String limit(String value) {
        String safe = value == null ? "" : value;
        return safe.substring(0, Math.min(64, safe.length()));
    }

    private static byte sanitizeSort(byte value) { return value >= 0 && value <= 7 ? value : 0; }
    private static byte sanitizeView(byte value) { return value >= 0 && value <= 2 ? value : 0; }

    private static InitialData readInitialData(FriendlyByteBuf buffer) {
        return new InitialData(buffer.readBlockPos(), buffer.readUUID(), buffer.readBoolean(), buffer.readBoolean(),
                buffer.readUtf(64), buffer.readByte(), buffer.readByte());
    }

    private record InitialData(BlockPos pos, UUID owner, boolean locked, boolean craftingMode,
                               String filter, byte sortType, byte viewType) { }

    private final class ChannelSlot extends Slot {
        private final int visible;
        private ChannelSlot(DummyChannelContainer container, int slot, int x, int y, int visible) {
            super(container, slot, x, y);
            this.visible = visible;
        }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
        @Override public int getMaxStackSize() { return Integer.MAX_VALUE; }
        @Override public boolean isActive() { return visible < (craftingMode ? 77 : CHANNEL_SLOTS); }
    }

    private final class CraftSlot extends Slot {
        private CraftSlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean isActive() { return craftingMode; }
    }

    private final class CraftResultSlot extends ResultSlot {
        private CraftResultSlot(Player player, TransientCraftingContainer input, ResultContainer output,
                                int slot, int x, int y) {
            super(player, input, output, slot, x, y);
        }
        @Override public boolean isActive() { return craftingMode; }
    }
}
