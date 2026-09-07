package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.avaritia.api.client.screen.ItemSelectScreen;
import committee.nova.mods.avaritia.api.client.screen.component.PortableItemGrid;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.common.net.C2SItemFilterPack;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static committee.nova.mods.avaritia.client.AvaritiaForgeClient.FILTER_KEY;

/**
 * Held IFilterItem registry-keyed filter list with a shared 9×5 item grid.
 */
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
    private ScreenRectangle panel = new ScreenRectangle(0, 0, 0, 0);
    private PortableItemGrid itemGrid;

    public ItemFilterScreen() {
        super(Component.translatable("key.avaritia.filter"));
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
                Component.translatable("删除"), button -> this.removeSelected()),
                "filter.remove"));
        this.addRenderableWidget(UiInspector.name(PortableUi.button(
                innerX + innerW - buttonWidth, footerY, buttonWidth, BUTTON_H,
                Component.translatable("添加"), button -> this.openSelector()),
                "filter.add"));
    }

    @Override
    protected void renderMenuBackground(@NotNull GuiGraphics graphics) {
        if (this.panel.width() > 0 && this.panel.height() > 0) {
            PortableUi.panel(graphics, this.panel);
            PortableUi.header(graphics, this.font, this.title, this.panel.left(), this.panel.top(), this.panel.width());
            UiInspector.region("filter.panel", this.panel, null, false);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.itemGrid != null) {
            this.itemGrid.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseReleased(mouseX, mouseY, button);
        itemGrid.cancelInteraction();
        return handled;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (FILTER_KEY.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void openSelector() {
        Minecraft.getInstance().setScreen(new ItemSelectScreen(this, this::addItem,
                Blocks.DIRT.asItem().getDefaultInstance()));
    }

    private void addItem(ItemStack input) {
        PacketDistributor.sendToServer(new C2SItemFilterPack(input, 0));
        if (this.itemList.stream().noneMatch(existing -> existing.is(input.getItem()))) {
            this.itemList.add(input.copyWithCount(1));
        }
        if (this.itemGrid != null) {
            this.itemGrid.setItems(this.itemList);
        }
    }

    private void selectItem(ItemStack stack) {
        this.selectedItem = stack;
    }

    private void removeSelected() {
        if (this.selectedItem.isEmpty()) {
            return;
        }
        Item item = this.selectedItem.getItem();
        this.itemList.removeIf(stack -> stack.is(item));
        PacketDistributor.sendToServer(new C2SItemFilterPack(this.selectedItem, 1));
        this.selectedItem = ItemStack.EMPTY;
        if (this.itemGrid != null) {
            this.itemGrid.setItems(this.itemList);
            this.itemGrid.setSelected(ItemStack.EMPTY);
        }
        onClose();
    }

    private void loadFilters() {
        this.itemList.clear();
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        CompoundTag filters = player.getMainHandItem().get(ModDataComponents.TOOL_FILTERS.get());
        if (filters == null) {
            return;
        }
        for (String key : filters.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id == null) {
                continue;
            }
            BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> {
                if (item != Items.AIR) {
                    ItemStack stack = new ItemStack(item);
                    CompoundTag data = filters.getCompound(key);
                    if (!data.isEmpty()) {
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
                    }
                    this.itemList.add(stack);
                }
            });
        }
    }

    private List<Component> itemTooltip(ItemStack stack) {
        Minecraft minecraft = this.minecraft;
        if (minecraft == null || stack.isEmpty()) {
            return List.of();
        }
        TooltipFlag flag = minecraft.options.advancedItemTooltips
                ? TooltipFlag.Default.ADVANCED
                : TooltipFlag.Default.NORMAL;
        List<Component> lines = new ArrayList<>(stack.getTooltipLines(
                Item.TooltipContext.of(minecraft.level), minecraft.player, flag));
        int insert = Math.min(1, lines.size());
        stack.getItem().builtInRegistryHolder().tags().forEach(tag ->
                lines.add(insert, Component.literal("#" + tag.location()).withStyle(ChatFormatting.DARK_PURPLE)));
        return lines;
    }
}
