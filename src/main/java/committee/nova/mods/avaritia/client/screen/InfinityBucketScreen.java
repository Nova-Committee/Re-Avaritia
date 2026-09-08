package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.client.screen.component.OperationMenu;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.common.component.InfinityBucketControl;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreature;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreatures;
import committee.nova.mods.avaritia.common.component.InfinityBucketFluids;
import committee.nova.mods.avaritia.common.item.misc.InfinityBucketItem;
import committee.nova.mods.avaritia.common.menu.InfinityBucketMenu;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModItems;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private EditBox searchBox;
    private Button fluidsTab;
    private Button creaturesTab;
    private boolean creaturesTabActive;
    private String query = "";
    private double scroll;
    private int focusedIndex = -1;
    private boolean cacheValid;
    private boolean draggingScrollbar;
    private InfinityBucketFluids cachedFluids;
    private InfinityBucketCreatures cachedCreatures;
    private InfinityBucketControl control = InfinityBucketControl.DEFAULT;
    private int sourceCount;
    private ScreenRectangle panel;
    private ScreenRectangle list;
    private ScreenRectangle listFrame;
    private ScreenRectangle listInput;
    private ScreenRectangle search;
    private ScreenRectangle scrollbar;
    private ScreenRectangle actions;

    public InfinityBucketScreen(InfinityBucketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, null, 260, 240, 256, 256);
        inventoryLabelX = InfinityBucketMenu.PLAYER_INV_X;
        inventoryLabelY = InfinityBucketMenu.PLAYER_INV_Y - 12;
    }

    @Override
    protected void subInit() {
        contextMenu.close();
        panel = new ScreenRectangle(getGuiLeft(), getGuiTop(), imageWidth, imageHeight);
        list = PortableLayout.translate(new ScreenRectangle(LIST_X, LIST_Y, LIST_WIDTH, LIST_HEIGHT), panel.left(), panel.top());
        listFrame = PortableLayout.inset(list, -1, -1, -1, -1);
        search = PortableLayout.translate(new ScreenRectangle(LIST_X, SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT), panel.left(), panel.top());
        scrollbar = PortableLayout.translate(new ScreenRectangle(SCROLL_X, LIST_Y, SCROLL_WIDTH, LIST_HEIGHT), panel.left(), panel.top());
        listInput = new ScreenRectangle(list.left(), list.top(), scrollbar.right() - list.left(), list.height());
        actions = PortableLayout.inset(panel, 0, 0, 0, imageHeight - InfinityBucketMenu.PLAYER_INV_Y + 12);
        fluidsTab = addRenderableWidget(PortableUi.button(leftPos + 8, topPos + 24, 114, 16,
                Component.translatable("gui.avaritia.infinity_bucket.tab.fluids"), button -> switchTab(false)));
        creaturesTab = addRenderableWidget(PortableUi.button(leftPos + 126, topPos + 24, 126, 16,
                Component.translatable("gui.avaritia.infinity_bucket.tab.creatures"), button -> switchTab(true)));
        UiInspector.name(fluidsTab, "bucket.tab.fluids");
        UiInspector.name(creaturesTab, "bucket.tab.creatures");
        fluidsTab.active = creaturesTabActive;
        creaturesTab.active = !creaturesTabActive;
        Component searchTitle = Component.translatable("gui.avaritia.infinity_bucket.search");
        searchBox = addRenderableWidget(new EditBox(font, search.left(), search.top(), search.width(), search.height(), searchTitle));
        UiInspector.name(searchBox, "bucket.search");
        searchBox.setMaxLength(64);
        searchBox.setHint(searchTitle);
        searchBox.setValue(query);
        searchBox.setResponder(this::filter);
        cacheValid = false;
        ensureCache();
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
                    EntityType<?> type = creature.entityType();
                    ItemStack icon = type == null ? new ItemStack(Items.WATER_BUCKET)
                            : creatureIcons.computeIfAbsent(type, key ->
                            SpawnEggItem.byId(key).map(holder -> new ItemStack(holder.value()))
                                    .orElse(new ItemStack(Items.WATER_BUCKET)));
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
        if (!PortableLayout.contains(list, mouseX, mouseY)) {
            return null;
        }
        int index = rowStart() + (int) ((mouseY - list.top()) / ROW_HEIGHT);
        return index < rows.size() ? rows.get(index) : null;
    }

    private boolean insideList(double mouseX, double mouseY) {
        return PortableLayout.contains(listInput, mouseX, mouseY);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ensureCache();
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        if (!overlayOpen()) {
            Row row = hoveredRow(mouseX, mouseY);
            if (row != null) {
                graphics.setComponentTooltipForNextFrame(font, active(row) ? row.activeTooltip : row.tooltip, mouseX, mouseY);
            }
        }
        contextMenu.render(graphics, font, mouseX, mouseY);
    }

    @Override
    protected void renderBgs(GuiGraphicsExtractor graphics, float partialTick, int x, int y) {
        PortableUi.panel(graphics, panel);
        UiInspector.region("bucket.panel", panel, null, false);
        PortableUi.header(graphics, font, title, panel.left(), panel.top(), panel.width());
        PortableUi.inset(graphics, listFrame);
        for (Slot slot : menu.slots) {
            if (slot.isActive()) {
                PortableUi.slot(graphics, leftPos + slot.x - 1, topPos + slot.y - 1);
            }
        }
        drawScrollbar(graphics);
    }

    private void drawScrollbar(GuiGraphicsExtractor graphics) {
        int extra = Math.max(0, rows.size() - VISIBLE_ROWS);
        int barH = scrollbar.height();
        int handleH = extra <= 0 ? barH : Math.max(8, LIST_HEIGHT * VISIBLE_ROWS / rows.size());
        int handleY = scrollbar.top() + (extra <= 0 ? 0 : (int) Math.round(scroll * (barH - handleH)));
        graphics.fill(scrollbar.left(), scrollbar.top(), scrollbar.right(), scrollbar.bottom(), 0xAA111111);
        graphics.fill(scrollbar.left(), handleY, scrollbar.right(), handleY + handleH, 0xFFB6B6B6);
        UiInspector.name(fluidsTab, "bucket.tab.fluids");
        UiInspector.region("bucket.scrollbar", scrollbar, null, true);
    }

    @Override
    protected void renderLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        PortableUi.text(graphics, font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 164, PortableUi.MUTED);
    }

    @Override
    protected void renderFg(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = list.left();
        int y = list.top();
        UiInspector.region("bucket.list", list, list, true);
        graphics.enableScissor(list.left(), list.top(), list.right(), list.bottom());
        if (rows.isEmpty()) {
            Component empty = Component.translatable(sourceCount == 0
                    ? "gui.avaritia.infinity_bucket.empty" : "gui.avaritia.infinity_bucket.no_results");
            graphics.centeredText(font, empty, x + LIST_WIDTH / 2, y + 14, PortableUi.TEXT);
            graphics.textWithWordWrap(font, HINT, x + 12, y + 32, LIST_WIDTH - 24, PortableUi.MUTED);
            graphics.disableScissor();
            return;
        }
        int start = rowStart();
        for (int i = 0; i < VISIBLE_ROWS && start + i < rows.size(); i++) {
            Row row = rows.get(start + i);
            int top = y + i * ROW_HEIGHT;
            boolean hovered = !overlayOpen() && hoveredRow(mouseX, mouseY) == row;
            PortableUi.row(graphics, x, top, LIST_WIDTH, ROW_HEIGHT, hovered, row.index == focusedIndex);
            if (UiInspector.enabled()) {
                UiInspector.row("bucket.list", null, row.index, x, top, LIST_WIDTH, ROW_HEIGHT, list, true);
            }
            PortableUi.text(graphics, font, row.name, x + 20, top + 1, LIST_WIDTH - 24, PortableUi.TEXT);
            PortableUi.text(graphics, font, active(row) ? row.activeDetail : row.detail,
                    x + 20, top + 10, LIST_WIDTH - 24, PortableUi.MUTED);
            if (row.fluid != null) {
                renderFluidIcon(graphics, row.fluid, x + 1, top + 1);
            } else {
                graphics.item(row.icon, x + 1, top + 1);
            }
        }
        graphics.disableScissor();
    }

    private void renderFluidIcon(GuiGraphicsExtractor graphics, FluidStack fluid, int x, int y) {
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                .get(fluid.getFluid().defaultFluidState());
        TextureAtlasSprite sprite = model.stillMaterial().sprite();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16);
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
        ItemAccess access = ItemAccess.forStack(carried.copyWithCount(1)).oneByOne();
        ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) {
            return false;
        }
        if (row != null && row.fluid != null) {
            try (Transaction tx = Transaction.openRoot()) {
                int inserted = handler.insert(FluidResource.of(row.fluid), Integer.MAX_VALUE, tx);
                if (inserted > 0) {
                    sendAction(InfinityBucketMenu.ACTION_EXTRACT_FLUID + row.index);
                    return true;
                }
            }
        }
        for (int i = 0; i < handler.size(); i++) {
            if (!handler.getResource(i).isEmpty() && handler.getAmountAsLong(i) > 0) {
                sendAction(InfinityBucketMenu.ACTION_INSERT_CARRIED);
                return true;
            }
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
        DeleteTarget target = new DeleteTarget(row);
        contextMenu.close();
        PortableUi.confirm(this,
                Component.translatable("gui.avaritia.infinity_bucket.delete.confirm"),
                Component.translatable("gui.avaritia.infinity_bucket.delete.confirm.message", target.name), () -> {
                    ensureCache();
                    if (canOperate() && target.stillPresent(menu.getBucket())) {
                        sendAction((target.creature ? InfinityBucketMenu.ACTION_DELETE_CREATURE
                                : InfinityBucketMenu.ACTION_DELETE_FLUID) + target.index);
                    }
                });
    }

    private void requestClear() {
        if (!canOperate()) {
            return;
        }
        contextMenu.close();
        PortableUi.confirm(this,
                Component.translatable("gui.avaritia.infinity_bucket.clear.confirm"),
                Component.translatable("gui.avaritia.infinity_bucket.clear.confirm.message"), () -> {
                    if (canOperate()) {
                        sendAction(InfinityBucketMenu.ACTION_CLEAR);
                    }
                });
    }

    private boolean overlayOpen() {
        return contextMenu.isOpen() || Minecraft.getInstance().screen != this;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (contextMenu.mouseClicked(event.x(), event.y(), event.button())) {
            return true;
        }
        if (searchBox != null && searchBox.isFocused() && !searchBox.isMouseOver(event.x(), event.y())) {
            searchBox.setFocused(false);
            if (getFocused() == searchBox) {
                setFocused(null);
            }
        }
        ensureCache();
        Row row = hoveredRow(event.x(), event.y());
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && insideList(event.x(), event.y())) {
            if (!transferCarried(row)) {
                openActions(row, event.x(), event.y());
            }
            return true;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && PortableLayout.contains(scrollbar, event.x(), event.y())) {
            draggingScrollbar = true;
            updateScrollFromMouse(event.y());
            return true;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && row != null) {
            focusedIndex = row.index;
            return true;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && PortableLayout.contains(actions, event.x(), event.y())
                && (searchBox == null || !searchBox.isMouseOver(event.x(), event.y()))) {
            openActions(null, event.x(), event.y());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void updateScrollFromMouse(double mouseY) {
        int extra = Math.max(0, rows.size() - VISIBLE_ROWS);
        if (extra <= 0) {
            scroll = 0;
            return;
        }
        scroll = Mth.clamp((mouseY - scrollbar.top()) / (double) scrollbar.height(), 0, 1);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScrollbar = false;
        boolean handled = super.mouseReleased(event);
        return handled || overlayOpen() || insideList(event.x(), event.y());
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScrollbar) {
            updateScrollFromMouse(event.y());
            return true;
        }
        return overlayOpen() || super.mouseDragged(event, dragX, dragY);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return !overlayOpen() && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (contextMenu.mouseScrolled(scrollY)) {
            return true;
        }
        if (insideList(mouseX, mouseY)) {
            int extra = Math.max(0, rows.size() - VISIBLE_ROWS);
            if (extra > 0) {
                scroll = Mth.clamp(scroll - Math.signum(scrollY) / extra, 0, 1);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (contextMenu.isOpen()) {
            contextMenu.keyPressed(event.key());
            return true;
        }
        if (searchBox != null && searchBox.isFocused()) {
            if (event.isEscape()) {
                searchBox.setFocused(false);
                setFocused(null);
                return true;
            }
            if (searchBox.keyPressed(event)) {
                return true;
            }
            if (!event.isCycleFocus() && searchBox.canConsumeInput()) {
                return true;
            }
        }
        if (event.key() == InputConstants.KEY_F10 && event.hasShiftDown()) {
            Row row = rows.stream().filter(entry -> entry.index == focusedIndex).findFirst().orElse(null);
            openActions(row, list.left() + 8, list.top() + 8);
            return true;
        }
        return super.keyPressed(event);
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
        private final Identifier typeId;
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
