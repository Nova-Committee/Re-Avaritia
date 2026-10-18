package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.PortableItemGrid;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.client.AvaritiaClient;
import committee.nova.mods.avaritia.common.net.C2SItemFilterPack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Held IFilterItem registry-keyed filter list with a shared 9×5 item grid. */
public class ItemFilterScreen extends Screen {
    private static final int COLUMNS = 9;
    private static final int ROWS = 5;
    private static final int GRID_WIDTH = COLUMNS * 19 + 12;
    private static final int GRID_HEIGHT = ROWS * 19 + 3;
    private static final int MARGIN = 8;
    private static final int HEADER_H = 20;
    private static final int FOOTER_H = 24;
    private static final int BUTTON_H = 20;
    private static final int GAP = 8;

    private final List<ItemStack> itemList = new ArrayList<>();
    private ItemStack selectedItem = ItemStack.EMPTY;
    private ScreenRectangle panel = ScreenRectangle.empty();
    private PortableItemGrid itemGrid;

    public ItemFilterScreen() {
        super(Component.translatable("title.avaritia.item_filter"));
        this.loadFilters();
    }

    @Override
    protected void init() {
        int previousScroll = this.itemGrid == null ? 0 : this.itemGrid.getScrollOffset();
        ItemStack previousSelected = this.selectedItem;
        this.panel = PortableLayout.centered(this.width, this.height,
                MARGIN + GRID_WIDTH + MARGIN,
                HEADER_H + 4 + GRID_HEIGHT + GAP + FOOTER_H,
                MARGIN);
        int innerX = this.panel.left() + MARGIN;
        int innerW = Math.max(0, this.panel.width() - MARGIN * 2);
        int gridY = this.panel.top() + HEADER_H + 4;
        int footerY = this.panel.bottom() - FOOTER_H;
        int buttonWidth = Math.max(0, (innerW - GAP) / 2);

        this.itemGrid = this.addRenderableWidget(UiInspector.name(
                new PortableItemGrid(this.minecraft, innerX, gridY, COLUMNS, ROWS, "filter", this::selectItem),
                "filter.grid"));
        this.itemGrid.setTooltipProvider(this::itemTooltip);
        this.itemGrid.setItems(this.itemList);
        this.itemGrid.setScrollOffset(previousScroll);
        this.itemGrid.setSelected(previousSelected);

        this.addRenderableWidget(UiInspector.name(PortableUi.button(
                innerX, footerY, buttonWidth, BUTTON_H,
                Component.translatable("gui.avaritia.remove"), button -> this.removeSelected()),
                "filter.remove"));
        this.addRenderableWidget(UiInspector.name(PortableUi.button(
                innerX + innerW - buttonWidth, footerY, buttonWidth, BUTTON_H,
                Component.translatable("gui.avaritia.add"), button -> this.openSelector()),
                "filter.add"));
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(graphics);
        if (this.panel.width() > 0 && this.panel.height() > 0) {
            PortableUi.panel(graphics, this.panel);
            PortableUi.header(graphics, this.font, this.title, this.panel.left(), this.panel.top(), this.panel.width());
            UiInspector.region("filter.panel", this.panel, null, false);
        }
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (this.itemGrid != null) {
            this.itemGrid.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled = super.mouseReleased(event);
        if (itemGrid != null) {
            itemGrid.cancelInteraction();
        }
        return handled;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (AvaritiaClient.FILTER_KEY.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void openSelector() {
        Minecraft.getInstance().gui.setScreen(new ItemSelectScreen(this, this::addItem, Items.DIRT.getDefaultInstance()));
    }

    private void addItem(ItemStack input) {
        sendFilterUpdate(input, 0);
    }

    private void selectItem(ItemStack stack) {
        this.selectedItem = stack;
    }

    private void removeSelected() {
        if (this.selectedItem.isEmpty()) {
            return;
        }
        sendFilterUpdate(this.selectedItem, 1);
        this.selectedItem = ItemStack.EMPTY;
        if (this.itemGrid != null) {
            this.itemGrid.setSelected(ItemStack.EMPTY);
        }
    }

    private void sendFilterUpdate(ItemStack stack, int action) {
        ItemStack filterStack = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        NetworkHandler.sendToServer(new C2SItemFilterPack(action, filterStack));
        if (minecraft.player != null) {
            ItemStack toolStack = minecraft.player.getMainHandItem();
            CustomData custom = toolStack.get(DataComponents.CUSTOM_DATA);
            CompoundTag tag = custom == null ? new CompoundTag() : custom.copyTag();
            CompoundTag filters = tag.getCompoundOrEmpty("filters");
            String key = BuiltInRegistries.ITEM.getKey(filterStack.getItem()).toString();
            if (action == 0 && !filterStack.isEmpty() && !filters.contains(key)) {
                filters.put(key, encodeStack(filterStack));
            } else if (action == 1) {
                filters.remove(key);
            }
            tag.put("filters", filters);
            toolStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        loadFilters();
        if (this.itemGrid != null) {
            this.itemGrid.setItems(this.itemList);
        }
    }

    private Tag encodeStack(ItemStack stack) {
        RegistryOps<Tag> ops = minecraft.player.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.OPTIONAL_CODEC.encodeStart(ops, stack).getOrThrow(IllegalArgumentException::new);
    }

    private void loadFilters() {
        this.itemList.clear();
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        CustomData custom = player.getMainHandItem().get(DataComponents.CUSTOM_DATA);
        CompoundTag filters = custom == null ? new CompoundTag() : custom.copyTag().getCompoundOrEmpty("filters");
        filters.keySet().stream().sorted().forEach(key -> {
            Identifier id = Identifier.tryParse(key);
            if (id == null) {
                return;
            }
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
            if (item != Items.AIR) {
                ItemStack stack = new ItemStack(item);
                CompoundTag data = filters.getCompoundOrEmpty(key);
                if (!data.isEmpty()) {
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
                }
                this.itemList.add(stack);
            }
        });
    }

    private List<Component> itemTooltip(ItemStack stack) {
        Minecraft minecraft = this.minecraft;
        if (minecraft == null || stack.isEmpty()) {
            return List.of();
        }
        List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(minecraft, stack));
        int insert = Math.min(1, lines.size());
        stack.typeHolder().tags()
                .sorted(Comparator.comparing(tag -> tag.location().toString()))
                .forEach(tag -> lines.add(insert, Component.literal("#" + tag.location()).withStyle(ChatFormatting.DARK_PURPLE)));
        return lines;
    }
}
