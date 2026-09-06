package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.render.FluidItemRender;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.client.screen.component.OperationMenu;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.widget.SimpleScrollBar;
import committee.nova.mods.avaritia.common.component.InfinityBucketControl;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreature;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreatures;
import committee.nova.mods.avaritia.common.component.InfinityBucketFluids;
import committee.nova.mods.avaritia.common.item.misc.InfinityBucketItem;
import committee.nova.mods.avaritia.common.menu.InfinityBucketMenu;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class InfinityBucketScreen extends BaseContainerScreen<InfinityBucketMenu> {
    private static final int LIST_X = 8;
    private static final int SEARCH_Y = 44;
    private static final int SEARCH_HEIGHT = 20;
    private static final int LIST_Y = SEARCH_Y + SEARCH_HEIGHT + 4;
    private static final int LIST_WIDTH = 232;
    private static final int SEARCH_WIDTH = 244;
    private static final int ROW_HEIGHT = 18;
    private static final int VISIBLE_ROWS = 4;
    private static final int LIST_HEIGHT = ROW_HEIGHT * VISIBLE_ROWS;
    private static final int SCROLL_X = 244;
    private static final int SCROLL_WIDTH = 6;
    private static final Component SELECTED = Component.translatable("gui.avaritia.infinity_bucket.selected");
    private static final Component HINT = Component.translatable("gui.avaritia.infinity_bucket.list_hint");

    private final DecimalFormat amountFormat = new DecimalFormat();
    private final OperationMenu contextMenu = new OperationMenu();
    private final List<Row> rows = new ArrayList<>();
    private final Map<EntityType<?>, ItemStack> creatureIcons = new HashMap<>();
    private SimpleScrollBar scrollBar;
    private EditBox searchBox;
    private Button fluidsTab;
    private Button creaturesTab;
    private boolean creaturesTabActive;
    private String query = "";
    private double scroll;
    private int focusedIndex = -1;
    private boolean cacheValid;
    private InfinityBucketFluids cachedFluids;
    private InfinityBucketCreatures cachedCreatures;
    private InfinityBucketControl control = InfinityBucketControl.DEFAULT;
    private int sourceCount;

    private Modal modal = Modal.NONE;
    private Component modalTitle = CommonComponents.EMPTY;
    private Component modalMessage = CommonComponents.EMPTY;
    private Button modalAccept;
    private Button modalCancel;
    private DeleteTarget deleteTarget;
    private int modalX;
    private int modalY;
    private int modalWidth;
    private int modalHeight;

    public InfinityBucketScreen(InfinityBucketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, null, 260, 240, 256, 256);
        inventoryLabelX = InfinityBucketMenu.PLAYER_INV_X;
        inventoryLabelY = InfinityBucketMenu.PLAYER_INV_Y - 12;
    }

    @Override
    protected void subInit() {
        contextMenu.close();
        fluidsTab = addRenderableWidget(PortableUi.button(leftPos + 8, topPos + 24, 114, 16,
                Component.translatable("gui.avaritia.infinity_bucket.tab.fluids"), button -> switchTab(false)));
        creaturesTab = addRenderableWidget(PortableUi.button(leftPos + 126, topPos + 24, 126, 16,
                Component.translatable("gui.avaritia.infinity_bucket.tab.creatures"), button -> switchTab(true)));
        fluidsTab.active = creaturesTabActive;
        creaturesTab.active = !creaturesTabActive;
        Component searchTitle = Component.translatable("gui.avaritia.infinity_bucket.search");
        searchBox = addRenderableWidget(new EditBox(font, leftPos + LIST_X, topPos + SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT, searchTitle));
        searchBox.setMaxLength(64);
        searchBox.setHint(searchTitle);
        searchBox.setValue(query);
        searchBox.setResponder(this::filter);
        scrollBar = new SimpleScrollBar(leftPos + SCROLL_X, topPos + LIST_Y, SCROLL_WIDTH, LIST_HEIGHT) {
            @Override
            public void draggedTo(double scrolledOn) {
                scroll = scrolledOn;
            }

            @Override
            public void beforeRender() {
            }
        };
        addRenderableWidget(scrollBar);
        cacheValid = false;
        ensureCache();
        layoutModal();
    }

    private void switchTab(boolean creatures) {
        if (creaturesTabActive != creatures) {
            creaturesTabActive = creatures;
            focusedIndex = -1;
            scroll = 0;
            cacheValid = false;
            contextMenu.close();
            fluidsTab.active = creatures;
            creaturesTab.active = !creatures;
        }
    }

    private boolean canOperate() {
        // Slot synchronization replaces client stack instances. Reference ownership stays on the server.
        ItemStack bucket = menu.getBucket();
        return bucket.is(ModItems.infinity_bucket.get()) && bucket.getCount() == 1;
    }

    private void sendAction(int id) {
        if (canOperate() && minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void ensureCache() {
        ItemStack bucket = menu.getBucket();
        InfinityBucketFluids fluids = bucket.getOrDefault(ModDataComponents.INFINITY_BUCKET_FLUIDS.get(), InfinityBucketFluids.EMPTY);
        InfinityBucketCreatures creatures = bucket.getOrDefault(ModDataComponents.INFINITY_BUCKET_CREATURES.get(), InfinityBucketCreatures.EMPTY);
        control = InfinityBucketItem.getControl(bucket);
        if (cacheValid && fluids == cachedFluids && creatures == cachedCreatures) {
            return;
        }
        if (fluids != cachedFluids || creatures != cachedCreatures) {
            contextMenu.close();
        }
        cachedFluids = fluids;
        cachedCreatures = creatures;
        rows.clear();
        String filter = query.toLowerCase(Locale.ROOT);
        if (creaturesTabActive) {
            sourceCount = creatures.size();
            for (int i = 0; i < creatures.size(); i++) {
                InfinityBucketCreature creature = creatures.creatures().get(i);
                Component name = creature.displayName();
                String id = creature.typeId().toString();
                if (matches(filter, name, id)) {
                    ItemStack icon = creatureIcons.computeIfAbsent(creature.entityType(), type -> {
                        SpawnEggItem egg = SpawnEggItem.byId(type);
                        return new ItemStack(egg != null ? egg : Items.WATER_BUCKET);
                    });
                    rows.add(new Row(i, null, creature, name, Component.literal(id), id, icon));
                }
            }
        } else {
            sourceCount = fluids.size();
            for (int i = 0; i < fluids.size(); i++) {
                FluidStack fluid = fluids.fluids().get(i);
                Component name = fluid.getHoverName();
                String id = InfinityBucketItem.getFluidName(fluid);
                if (matches(filter, name, id)) {
                    rows.add(new Row(i, fluid, null, name,
                            Component.literal(amountFormat.format(fluid.getAmount()) + " mB"), id, ItemStack.EMPTY));
                }
            }
        }
        cacheValid = true;
        if (rows.size() <= VISIBLE_ROWS) {
            scroll = 0;
        }
        scrollBar.setScrolledOn(scroll);
        scrollBar.setScrollTagSize(rows.size() <= VISIBLE_ROWS ? LIST_HEIGHT
                : Math.max(8, LIST_HEIGHT * VISIBLE_ROWS / (double) rows.size()));
    }

    private static boolean matches(String filter, Component name, String id) {
        return filter.isEmpty() || name.getString().toLowerCase(Locale.ROOT).contains(filter)
                || id.toLowerCase(Locale.ROOT).contains(filter);
    }

    private boolean active(Row row) {
        return control.creatureSelected() == (row.creature != null) && control.selectedIndex() == row.index;
    }

    private int rowStart() {
        return (int) Math.round(scroll * Math.max(0, rows.size() - VISIBLE_ROWS));
    }

    @Nullable
    private Row hoveredRow(double mouseX, double mouseY) {
        if (mouseX < leftPos + LIST_X || mouseX >= leftPos + LIST_X + LIST_WIDTH
                || mouseY < topPos + LIST_Y || mouseY >= topPos + LIST_Y + LIST_HEIGHT) {
            return null;
        }
        int index = rowStart() + (int) ((mouseY - topPos - LIST_Y) / ROW_HEIGHT);
        return index < rows.size() ? rows.get(index) : null;
    }

    private boolean insideList(double mouseX, double mouseY) {
        return mouseX >= leftPos + LIST_X && mouseX < leftPos + SCROLL_X + SCROLL_WIDTH
                && mouseY >= topPos + LIST_Y && mouseY < topPos + LIST_Y + LIST_HEIGHT;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ensureCache();
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!overlayOpen()) {
            Row row = hoveredRow(mouseX, mouseY);
            if (row != null) {
                graphics.renderComponentTooltip(font, active(row) ? row.activeTooltip : row.tooltip, mouseX, mouseY);
            }
        }
        renderModal(graphics, mouseX, mouseY, partialTick);
        contextMenu.render(graphics, font, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        PortableUi.panel(graphics, leftPos, topPos, imageWidth, imageHeight);
        PortableUi.header(graphics, font, title, leftPos, topPos, imageWidth);
        PortableUi.inset(graphics, leftPos + LIST_X - 1, topPos + LIST_Y - 1, LIST_WIDTH + 2, LIST_HEIGHT + 2);
        for (Slot slot : menu.slots) {
            if (slot.isActive()) {
                PortableUi.slot(graphics, leftPos + slot.x - 1, topPos + slot.y - 1);
            }
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        PortableUi.text(graphics, font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 164, PortableUi.MUTED);
    }

    @Override
    protected void renderFg(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = leftPos + LIST_X;
        int y = topPos + LIST_Y;
        if (rows.isEmpty()) {
            graphics.enableScissor(x, y, x + LIST_WIDTH, y + LIST_HEIGHT);
            Component empty = Component.translatable(sourceCount == 0
                    ? "gui.avaritia.infinity_bucket.empty" : "gui.avaritia.infinity_bucket.no_results");
            graphics.drawCenteredString(font, empty, x + LIST_WIDTH / 2, y + 14, PortableUi.TEXT);
            graphics.drawWordWrap(font, HINT, x + 12, y + 32, LIST_WIDTH - 24, PortableUi.MUTED);
            graphics.disableScissor();
            return;
        }
        graphics.enableScissor(x, y, x + LIST_WIDTH, y + LIST_HEIGHT);
        int start = rowStart();
        for (int i = 0; i < VISIBLE_ROWS && start + i < rows.size(); i++) {
            Row row = rows.get(start + i);
            int top = y + i * ROW_HEIGHT;
            boolean hovered = !overlayOpen() && hoveredRow(mouseX, mouseY) == row;
            PortableUi.row(graphics, x, top, LIST_WIDTH, ROW_HEIGHT, hovered, row.index == focusedIndex);
            PortableUi.text(graphics, font, row.name, x + 20, top + 1, LIST_WIDTH - 24, PortableUi.TEXT);
            PortableUi.text(graphics, font, active(row) ? row.activeDetail : row.detail,
                    x + 20, top + 10, LIST_WIDTH - 24, PortableUi.MUTED);
        }
        graphics.flush();
        for (int i = 0; i < VISIBLE_ROWS && start + i < rows.size(); i++) {
            Row row = rows.get(start + i);
            int top = y + i * ROW_HEIGHT;
            if (row.fluid != null) {
                FluidItemRender.renderFluid(row.fluid, graphics.pose(), x + 1, top + 1, 0);
            } else {
                graphics.renderItem(row.icon, x + 1, top + 1);
            }
        }
        graphics.disableScissor();
    }

    private boolean transferCarried(@Nullable Row row) {
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty() || carried.is(ModItems.infinity_bucket.get()) || !canOperate()) {
            return false;
        }
        if (carried.getItem() instanceof MobBucketItem) {
            sendAction(InfinityBucketMenu.ACTION_INSERT_CARRIED);
            return true;
        }
        if (creaturesTabActive) {
            if (row != null && row.creature != null && carried.is(Items.BUCKET)) {
                sendAction(InfinityBucketMenu.ACTION_EXTRACT_CREATURE + row.index);
                return true;
            }
            return false;
        }
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(carried.copyWithCount(1)).orElse(null);
        if (handler == null) {
            return false;
        }
        if (row != null && row.fluid != null && handler.fill(row.fluid, IFluidHandler.FluidAction.SIMULATE) > 0) {
            sendAction(InfinityBucketMenu.ACTION_EXTRACT_FLUID + row.index);
            return true;
        }
        if (!handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE).isEmpty()) {
            sendAction(InfinityBucketMenu.ACTION_INSERT_CARRIED);
            return true;
        }
        return false;
    }

    private void openActions(@Nullable Row row, double mouseX, double mouseY) {
        List<OperationMenu.Entry> entries = new ArrayList<>();
        if (row != null) {
            focusedIndex = row.index;
            if (!active(row)) {
                entries.add(OperationMenu.Entry.of("gui.avaritia.infinity_bucket.select", () ->
                        sendAction((row.creature == null ? InfinityBucketMenu.ACTION_SELECT_FLUID
                                : InfinityBucketMenu.ACTION_SELECT_CREATURE) + row.index)));
            }
            entries.add(OperationMenu.Entry.danger("gui.avaritia.infinity_bucket.delete", () -> requestDelete(row)));
        }
        if (!query.isEmpty()) {
            entries.add(OperationMenu.Entry.of("gui.avaritia.infinity_bucket.clear_filter", () -> filter("")));
        }
        if (!cachedFluids.isEmpty() || !cachedCreatures.isEmpty()) {
            entries.add(OperationMenu.Entry.danger("gui.avaritia.infinity_bucket.clear", this::requestClear));
        }
        contextMenu.open((int) mouseX, (int) mouseY, width, height, font, entries);
    }

    private void filter(String value) {
        if (searchBox != null && !searchBox.getValue().equals(value)) {
            searchBox.setValue(value);
            return;
        }
        String next = value.trim();
        if (query.equals(next)) {
            return;
        }
        query = next;
        scroll = 0;
        focusedIndex = -1;
        cacheValid = false;
        ensureCache();
    }

    private void requestDelete(Row row) {
        if (!canOperate()) {
            return;
        }
        deleteTarget = new DeleteTarget(row);
        modal = Modal.DELETE;
        modalTitle = Component.translatable("gui.avaritia.infinity_bucket.delete.confirm");
        modalMessage = Component.translatable("gui.avaritia.infinity_bucket.delete.confirm.message", deleteTarget.name);
        layoutModal();
    }

    private void requestClear() {
        if (!canOperate()) {
            return;
        }
        modal = Modal.CLEAR;
        modalTitle = Component.translatable("gui.avaritia.infinity_bucket.clear.confirm");
        modalMessage = Component.translatable("gui.avaritia.infinity_bucket.clear.confirm.message");
        layoutModal();
    }

    /** Stays inside this container screen so carried items and the server menu remain open. */
    private void layoutModal() {
        modalAccept = null;
        modalCancel = null;
        if (modal == Modal.NONE) {
            return;
        }
        contextMenu.close();
        setFocused(null);
        modalWidth = Math.min(280, width - 16);
        modalHeight = Math.min(height - 16, 76 + font.split(modalMessage, modalWidth - 28).size() * font.lineHeight);
        modalX = (width - modalWidth) / 2;
        modalY = (height - modalHeight) / 2;
        int buttonWidth = (modalWidth - 32) / 2;
        int buttonY = modalY + modalHeight - 30;
        modalCancel = PortableUi.button(modalX + 12, buttonY, buttonWidth, 20,
                CommonComponents.GUI_CANCEL, button -> closeModal());
        modalAccept = PortableUi.dangerButton(modalX + modalWidth - buttonWidth - 12, buttonY, buttonWidth, 20,
                Component.translatable("gui.avaritia.portable.confirm"), button -> acceptModal());
        modalCancel.setFocused(true);
    }

    private void closeModal() {
        modal = Modal.NONE;
        deleteTarget = null;
        modalAccept = null;
        modalCancel = null;
        setFocused(null);
    }

    private void acceptModal() {
        ensureCache();
        if (canOperate()) {
            if (modal == Modal.CLEAR) {
                sendAction(InfinityBucketMenu.ACTION_CLEAR);
            } else if (modal == Modal.DELETE && deleteTarget != null && deleteTarget.stillPresent(menu.getBucket())) {
                sendAction((deleteTarget.creature ? InfinityBucketMenu.ACTION_DELETE_CREATURE
                        : InfinityBucketMenu.ACTION_DELETE_FLUID) + deleteTarget.index);
            }
        }
        closeModal();
    }

    private void renderModal(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (modal == Modal.NONE) {
            return;
        }
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 500);
        graphics.fill(0, 0, width, height, 0xA0000000);
        PortableUi.panel(graphics, modalX, modalY, modalWidth, modalHeight);
        PortableUi.header(graphics, font, modalTitle, modalX + 4, modalY + 4, modalWidth - 8);
        graphics.drawWordWrap(font, modalMessage, modalX + 14, modalY + 29, modalWidth - 28, PortableUi.TEXT);
        modalCancel.render(graphics, mouseX, mouseY, partialTick);
        modalAccept.render(graphics, mouseX, mouseY, partialTick);
        graphics.flush();
        graphics.pose().popPose();
    }

    private boolean overlayOpen() {
        return modal != Modal.NONE || contextMenu.isOpen();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (modal != Modal.NONE) {
            if (!modalCancel.mouseClicked(mouseX, mouseY, button) && modalAccept != null) {
                modalAccept.mouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        if (contextMenu.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (searchBox != null && searchBox.isFocused() && !searchBox.isMouseOver(mouseX, mouseY)) {
            searchBox.setFocused(false);
            if (getFocused() == searchBox) {
                setFocused(null);
            }
        }
        ensureCache();
        Row row = hoveredRow(mouseX, mouseY);
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && insideList(mouseX, mouseY)) {
            if (!transferCarried(row)) {
                openActions(row, mouseX, mouseY);
            }
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && row != null) {
            focusedIndex = row.index;
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && mouseX >= leftPos && mouseX < leftPos + imageWidth
                && mouseY >= topPos && mouseY < topPos + InfinityBucketMenu.PLAYER_INV_Y - 12
                && (searchBox == null || !searchBox.isMouseOver(mouseX, mouseY))) {
            openActions(null, mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseReleased(mouseX, mouseY, button);
        if (scrollBar != null) {
            scrollBar.setScrolling(false);
        }
        return handled || overlayOpen() || insideList(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return overlayOpen() || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return !overlayOpen() && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (modal != Modal.NONE || contextMenu.mouseScrolled(scrollY)) {
            return true;
        }
        if (insideList(mouseX, mouseY)) {
            int extra = Math.max(0, rows.size() - VISIBLE_ROWS);
            if (extra > 0) {
                scroll = Mth.clamp(scroll - Math.signum(scrollY) / extra, 0, 1);
                scrollBar.setScrolledOn(scroll);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (modal != Modal.NONE) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closeModal();
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                if (modalAccept.isFocused()) {
                    acceptModal();
                } else {
                    closeModal();
                }
            } else if (keyCode == GLFW.GLFW_KEY_TAB) {
                boolean acceptFocused = !modalAccept.isFocused();
                modalAccept.setFocused(acceptFocused);
                modalCancel.setFocused(!acceptFocused);
            }
            return true;
        }
        if (contextMenu.isOpen()) {
            contextMenu.keyPressed(keyCode);
            return true;
        }
        if (searchBox != null && searchBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchBox.setFocused(false);
                setFocused(null);
                return true;
            }
            if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (keyCode != GLFW.GLFW_KEY_TAB && searchBox.canConsumeInput()) {
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_MENU || (keyCode == GLFW.GLFW_KEY_F10 && hasShiftDown())) {
            Row row = rows.stream().filter(entry -> entry.index == focusedIndex).findFirst().orElse(null);
            openActions(row, leftPos + LIST_X + 8, topPos + LIST_Y + 8);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (modal != Modal.NONE || contextMenu.isOpen()) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private enum Modal {
        NONE, DELETE, CLEAR
    }

    private static final class Row {
        private final int index;
        private final FluidStack fluid;
        private final InfinityBucketCreature creature;
        private final Component name;
        private final Component detail;
        private final Component activeDetail;
        private final ItemStack icon;
        private final List<Component> tooltip;
        private final List<Component> activeTooltip;

        private Row(int index, @Nullable FluidStack fluid, @Nullable InfinityBucketCreature creature,
                    Component name, Component detail, String id, ItemStack icon) {
            this.index = index;
            this.fluid = fluid;
            this.creature = creature;
            this.name = name;
            this.detail = detail;
            this.activeDetail = detail.copy().append(" · ").append(SELECTED);
            this.icon = icon;
            Component registry = Component.literal(id).withStyle(ChatFormatting.GRAY);
            Component hint = HINT.copy().withStyle(ChatFormatting.GRAY);
            Component selected = SELECTED.copy().withStyle(ChatFormatting.YELLOW);
            this.tooltip = fluid == null ? List.of(name, registry, hint) : List.of(name, registry, detail, hint);
            this.activeTooltip = fluid == null ? List.of(name, registry, selected, hint)
                    : List.of(name, registry, detail, selected, hint);
        }
    }

    private static final class DeleteTarget {
        private final boolean creature;
        private final int index;
        private final FluidStack fluid;
        private final ResourceLocation typeId;
        private final CompoundTag entityData;
        private final Component name;

        private DeleteTarget(Row row) {
            creature = row.creature != null;
            index = row.index;
            fluid = row.fluid == null ? FluidStack.EMPTY : row.fluid.copy();
            typeId = creature ? row.creature.typeId() : null;
            entityData = creature ? row.creature.entityData().copy() : null;
            name = creature ? row.name : row.name.copy().append(" (").append(row.detail).append(")");
        }

        private boolean stillPresent(ItemStack bucket) {
            if (creature) {
                List<InfinityBucketCreature> creatures = InfinityBucketItem.getCreatures(bucket);
                if (index >= creatures.size()) {
                    return false;
                }
                InfinityBucketCreature live = creatures.get(index);
                return live.typeId().equals(typeId) && Objects.equals(live.entityData(), entityData);
            }
            List<FluidStack> fluids = InfinityBucketItem.getFluids(bucket);
            return index < fluids.size() && FluidStack.matches(fluids.get(index), fluid);
        }
    }
}
