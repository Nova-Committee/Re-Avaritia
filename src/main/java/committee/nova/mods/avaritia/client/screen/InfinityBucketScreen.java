package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.render.FluidItemRender;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.api.client.widget.SimpleScrollBar;
import committee.nova.mods.avaritia.common.component.InfinityBucketControl;
import committee.nova.mods.avaritia.common.component.InfinityBucketCreature;
import committee.nova.mods.avaritia.common.item.misc.InfinityBucketItem;
import committee.nova.mods.avaritia.common.menu.InfinityBucketMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InfinityBucketScreen extends BaseContainerScreen<InfinityBucketMenu> {
    private static final ResourceLocation INVENTORY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int LIST_X = 8;
    private static final int LIST_Y = 40;
    private static final int LIST_WIDTH = 152;
    private static final int ROW_HEIGHT = 18;
    private static final int VISIBLE_ROWS = 3;
    private static final int LIST_HEIGHT = VISIBLE_ROWS * ROW_HEIGHT;
    private static final int SELECTED_Y = 96;
    private static final int ACTION_Y = 112;
    private static final int DELETE_Y = 128;
    private static final int TRANSFER_SLOT_X = 152;
    private static final int TRANSFER_SLOT_Y = 128;

    private EditBox searchBox;
    private SimpleScrollBar scrollBar;
    private Button fluidsTab;
    private Button creaturesTab;
    private Button forceButton;
    private Button deleteButton;
    private Button clearButton;
    private Button transferInButton;
    private Button transferOutButton;
    private boolean creaturesTabActive;
    private double scroll;
    private int pendingConfirm;
    private String lastQuery = "";

    public InfinityBucketScreen(InfinityBucketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, null, 176, 240, 256, 256);
        this.inventoryLabelY = 147;
        this.titleLabelY = 5;
    }

    @Override
    protected void subInit() {
        searchBox = new EditBox(font, leftPos + 8, topPos + 28, 160, 10, Component.translatable("gui.avaritia.infinity_bucket.search"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(true);
        addRenderableWidget(searchBox);

        fluidsTab = addRenderableWidget(Button.builder(Component.translatable("gui.avaritia.infinity_bucket.tab.fluids"), button -> {
            creaturesTabActive = false;
            pendingConfirm = 0;
            scroll = 0;
        }).bounds(leftPos + 8, topPos + 15, 70, 12).build());
        creaturesTab = addRenderableWidget(Button.builder(Component.translatable("gui.avaritia.infinity_bucket.tab.creatures"), button -> {
            creaturesTabActive = true;
            pendingConfirm = 0;
            scroll = 0;
        }).bounds(leftPos + 80, topPos + 15, 88, 12).build());

        forceButton = addRenderableWidget(Button.builder(Component.translatable("gui.avaritia.infinity_bucket.force"),
                button -> sendAction(InfinityBucketMenu.ACTION_TOGGLE_FORCE)).bounds(leftPos + 8, topPos + ACTION_Y, 76, 14).build());
        deleteButton = addRenderableWidget(Button.builder(Component.translatable("gui.avaritia.infinity_bucket.delete"),
                button -> confirmOrSend(InfinityBucketMenu.ACTION_DELETE)).bounds(leftPos + 8, topPos + DELETE_Y, 52, 14).build());
        clearButton = addRenderableWidget(Button.builder(Component.translatable("gui.avaritia.infinity_bucket.clear"),
                button -> confirmOrSend(InfinityBucketMenu.ACTION_CLEAR)).bounds(leftPos + 62, topPos + DELETE_Y, 52, 14).build());
        transferInButton = addRenderableWidget(Button.builder(Component.literal("<<"),
                button -> sendAction(InfinityBucketMenu.ACTION_TRANSFER_IN)).bounds(leftPos + 116, topPos + DELETE_Y, 14, 14).build());
        transferInButton.setTooltip(Tooltip.create(Component.translatable("gui.avaritia.infinity_bucket.transfer.in")));
        transferOutButton = addRenderableWidget(Button.builder(Component.literal(">>"),
                button -> sendAction(InfinityBucketMenu.ACTION_TRANSFER_OUT)).bounds(leftPos + 132, topPos + DELETE_Y, 14, 14).build());
        transferOutButton.setTooltip(Tooltip.create(Component.translatable("gui.avaritia.infinity_bucket.transfer.out")));

        scrollBar = new SimpleScrollBar(leftPos + 162, topPos + LIST_Y, 6, LIST_HEIGHT) {
            @Override
            public void draggedTo(double scrolledOn) {
                scroll = scrolledOn;
            }

            @Override
            public void beforeRender() {
            }
        };
        addRenderableWidget(scrollBar);
        updateButtons();
    }

    private void confirmOrSend(int action) {
        if (pendingConfirm == action) {
            pendingConfirm = 0;
            sendAction(action);
            updateButtons();
            return;
        }
        pendingConfirm = action;
        updateButtons();
    }

    private void sendAction(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
        pendingConfirm = 0;
        updateButtons();
    }

    private ItemStack bucket() {
        return menu.getBucket();
    }

    private List<FluidStack> filteredFluids() {
        String query = searchBox == null ? "" : searchBox.getValue().toLowerCase(Locale.ROOT);
        List<FluidStack> fluids = InfinityBucketItem.getFluids(bucket());
        if (query.isEmpty()) {
            return fluids;
        }
        List<FluidStack> filtered = new ArrayList<>();
        for (FluidStack fluid : fluids) {
            String name = fluid.getHoverName().getString().toLowerCase(Locale.ROOT);
            String id = InfinityBucketItem.getFluidName(fluid).toLowerCase(Locale.ROOT);
            if (name.contains(query) || id.contains(query)) {
                filtered.add(fluid);
            }
        }
        return filtered;
    }

    private List<IndexedCreature> filteredCreatures() {
        String query = searchBox == null ? "" : searchBox.getValue().toLowerCase(Locale.ROOT);
        List<InfinityBucketCreature> creatures = InfinityBucketItem.getCreatures(bucket());
        List<IndexedCreature> filtered = new ArrayList<>();
        for (int i = 0; i < creatures.size(); i++) {
            InfinityBucketCreature creature = creatures.get(i);
            String name = creature.displayName().getString().toLowerCase(Locale.ROOT);
            String id = creature.typeId().toString().toLowerCase(Locale.ROOT);
            if (query.isEmpty() || name.contains(query) || id.contains(query)) {
                filtered.add(new IndexedCreature(i, creature));
            }
        }
        return filtered;
    }

    private int filteredCount() {
        return creaturesTabActive ? filteredCreatures().size() : filteredFluids().size();
    }

    private int rowStart() {
        int extra = Math.max(0, filteredCount() - VISIBLE_ROWS);
        return extra == 0 ? 0 : (int) Math.round(scroll * extra);
    }

    private void updateButtons() {
        ItemStack bucket = bucket();
        InfinityBucketControl control = InfinityBucketItem.getControl(bucket);
        if (forceButton != null) {
            forceButton.setTooltip(Tooltip.create(Component.translatable(control.forcePlacement()
                    ? "gui.avaritia.infinity_bucket.force.on"
                    : "gui.avaritia.infinity_bucket.force.off")));
            fluidsTab.active = creaturesTabActive;
            creaturesTab.active = !creaturesTabActive;
            deleteButton.setMessage(Component.translatable(pendingConfirm == InfinityBucketMenu.ACTION_DELETE
                    ? "gui.avaritia.infinity_bucket.delete.confirm"
                    : "gui.avaritia.infinity_bucket.delete"));
            clearButton.setMessage(Component.translatable(pendingConfirm == InfinityBucketMenu.ACTION_CLEAR
                    ? "gui.avaritia.infinity_bucket.clear.confirm"
                    : "gui.avaritia.infinity_bucket.clear"));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        String query = searchBox.getValue();
        if (!query.equals(lastQuery)) {
            lastQuery = query;
            scroll = 0;
        }
        updateButtons();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 154, 0xFF2B2B2B);
        graphics.fill(leftPos + LIST_X - 1, topPos + LIST_Y - 1, leftPos + LIST_X + LIST_WIDTH + 1, topPos + LIST_Y + LIST_HEIGHT + 1, 0xFF101010);
        graphics.fill(leftPos + TRANSFER_SLOT_X - 1, topPos + TRANSFER_SLOT_Y - 1,
                leftPos + TRANSFER_SLOT_X + 17, topPos + TRANSFER_SLOT_Y + 17, 0xFF8B8B8B);
        graphics.fill(leftPos + TRANSFER_SLOT_X, topPos + TRANSFER_SLOT_Y,
                leftPos + TRANSFER_SLOT_X + 16, topPos + TRANSFER_SLOT_Y + 16, 0xFF373737);
        graphics.blit(INVENTORY_TEXTURE, leftPos, topPos + 144, 0, 126, 176, 96, 256, 256);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    protected void renderFg(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ItemStack bucket = bucket();
        InfinityBucketControl control = InfinityBucketItem.getControl(bucket);
        DecimalFormat format = new DecimalFormat();
        int start = rowStart();
        if (creaturesTabActive) {
            List<IndexedCreature> creatures = filteredCreatures();
            for (int row = 0; row < VISIBLE_ROWS; row++) {
                int index = start + row;
                if (index >= creatures.size()) {
                    break;
                }
                IndexedCreature entry = creatures.get(index);
                int y = topPos + LIST_Y + row * ROW_HEIGHT;
                boolean selected = control.creatureSelected() && control.selectedIndex() == entry.index;
                if (selected) {
                    graphics.fill(leftPos + LIST_X, y, leftPos + LIST_X + LIST_WIDTH, y + ROW_HEIGHT, 0x6655FFFF);
                }
                renderCreatureIcon(graphics, entry.creature, leftPos + LIST_X, y);
                graphics.drawString(font, font.plainSubstrByWidth(entry.creature.displayName().getString(), 90), leftPos + LIST_X + 18, y + 1, 0xFFFFFF, false);
                graphics.drawString(font, font.plainSubstrByWidth(entry.creature.typeId().toString(), 130), leftPos + LIST_X + 18, y + 10, 0xAAAAAA, false);
            }
        } else {
            List<FluidStack> fluids = filteredFluids();
            List<FluidStack> all = InfinityBucketItem.getFluids(bucket);
            for (int row = 0; row < VISIBLE_ROWS; row++) {
                int index = start + row;
                if (index >= fluids.size()) {
                    break;
                }
                FluidStack fluid = fluids.get(index);
                int y = topPos + LIST_Y + row * ROW_HEIGHT;
                int realIndex = indexOfFluid(all, fluid);
                boolean selected = !control.creatureSelected() && realIndex == control.selectedIndex();
                if (selected) {
                    graphics.fill(leftPos + LIST_X, y, leftPos + LIST_X + LIST_WIDTH, y + ROW_HEIGHT, 0x6655FFFF);
                }
                FluidItemRender.renderFluid(fluid, graphics.pose(), leftPos + LIST_X, y, 0);
                graphics.drawString(font, font.plainSubstrByWidth(fluid.getHoverName().getString(), 80), leftPos + LIST_X + 18, y + 1, 0xFFFFFF, false);
                graphics.drawString(font, format.format(fluid.getAmount()) + " mB", leftPos + LIST_X + 18, y + 10, 0xAAAAAA, false);
            }
        }

        Component selectedLabel = Component.translatable("gui.avaritia.infinity_bucket.selected");
        graphics.drawString(font, selectedLabel, leftPos + 8, topPos + SELECTED_Y + 4, 0xFFFFFF, false);
        if (control.creatureSelected()) {
            InfinityBucketCreature creature = InfinityBucketItem.getSelectedCreature(bucket);
            if (creature != null) {
                renderCreatureIcon(graphics, creature, leftPos + 70, topPos + SELECTED_Y);
                graphics.drawString(font, font.plainSubstrByWidth(creature.displayName().getString(), 80), leftPos + 88, topPos + SELECTED_Y + 4, 0xFFFFFF, false);
            } else {
                graphics.drawString(font, Component.translatable("gui.avaritia.infinity_bucket.empty"), leftPos + 70, topPos + SELECTED_Y + 4, 0xAAAAAA, false);
            }
        } else {
            FluidStack selected = InfinityBucketItem.getSelectedFluid(bucket);
            if (!selected.isEmpty()) {
                FluidItemRender.renderFluid(selected, graphics.pose(), leftPos + 70, topPos + SELECTED_Y, 0);
                graphics.drawString(font, font.plainSubstrByWidth(selected.getHoverName().getString() + " " + format.format(selected.getAmount()) + " mB", 90),
                        leftPos + 88, topPos + SELECTED_Y + 4, 0xFFFFFF, false);
            } else {
                graphics.drawString(font, Component.translatable("gui.avaritia.infinity_bucket.empty"), leftPos + 70, topPos + SELECTED_Y + 4, 0xAAAAAA, false);
            }
        }
        int extra = Math.max(0, filteredCount() - VISIBLE_ROWS);
        scrollBar.setScrollTagSize(extra <= 0 ? LIST_HEIGHT : Math.max(8, LIST_HEIGHT * VISIBLE_ROWS / (double) filteredCount()));
        scrollBar.setScrolledOn(scroll);
    }

    private int indexOfFluid(List<FluidStack> all, FluidStack fluid) {
        for (int i = 0; i < all.size(); i++) {
            if (FluidStack.matches(all.get(i), fluid)) {
                return i;
            }
        }
        return -1;
    }

    private void renderCreatureIcon(GuiGraphics graphics, InfinityBucketCreature creature, int x, int y) {
        EntityType<?> type = creature.entityType();
        SpawnEggItem egg = SpawnEggItem.byId(type);
        ItemStack icon = egg != null ? new ItemStack(egg) : new ItemStack(Items.WATER_BUCKET);
        graphics.renderItem(icon, x, y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= leftPos + LIST_X && mouseX < leftPos + LIST_X + LIST_WIDTH
                && mouseY >= topPos + LIST_Y && mouseY < topPos + LIST_Y + LIST_HEIGHT) {
            int row = (int) ((mouseY - (topPos + LIST_Y)) / ROW_HEIGHT);
            int index = rowStart() + row;
            if (creaturesTabActive) {
                List<IndexedCreature> creatures = filteredCreatures();
                if (index >= 0 && index < creatures.size()) {
                    sendAction(InfinityBucketMenu.ACTION_SELECT_CREATURE + creatures.get(index).index);
                    return true;
                }
            } else {
                List<FluidStack> fluids = filteredFluids();
                List<FluidStack> all = InfinityBucketItem.getFluids(bucket());
                if (index >= 0 && index < fluids.size()) {
                    int realIndex = indexOfFluid(all, fluids.get(index));
                    if (realIndex >= 0) {
                        sendAction(InfinityBucketMenu.ACTION_SELECT_FLUID + realIndex);
                        return true;
                    }
                }
            }
        } else if (pendingConfirm != 0
                && (mouseX < deleteButton.getX() || mouseX > clearButton.getX() + clearButton.getWidth()
                || mouseY < deleteButton.getY() || mouseY > deleteButton.getY() + deleteButton.getHeight())) {
            pendingConfirm = 0;
            updateButtons();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= leftPos + LIST_X && mouseX < leftPos + LIST_X + LIST_WIDTH + 8
                && mouseY >= topPos + LIST_Y && mouseY < topPos + LIST_Y + LIST_HEIGHT) {
            int extra = Math.max(0, filteredCount() - VISIBLE_ROWS);
            if (extra > 0) {
                scroll = Mth.clamp(scroll - Math.signum(scrollY) / extra, 0.0D, 1.0D);
                scrollBar.setScrolledOn(scroll);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox != null && searchBox.isFocused()) {
            if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
            return searchBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private record IndexedCreature(int index, InfinityBucketCreature creature) {
    }
}
