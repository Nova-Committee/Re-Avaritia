package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.render.FluidItemRender;
import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.common.menu.TesseractMenu;
import committee.nova.mods.avaritia.common.net.channel.C2SChannelFilterPack;
import committee.nova.mods.avaritia.core.channel.ClientChannel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntSupplier;

import static committee.nova.mods.avaritia.client.screen.TesseractScreenLayout.*;

/** 使用 1.20.1 材质图集的超立方体存储与合成界面。 */
public class TesseractScreen extends AbstractContainerScreen<TesseractMenu> {
    private static final ResourceLocation GUI_IMG = Res.BLACK_HOLE_CHANNEL_PANEL;
    private static final int TEXTURE_SIZE = 256;
    private static final int CHANNEL_SLOT_START = 51;

    private final WidgetSprites legacySprites = new WidgetSprites(GUI_IMG, GUI_IMG);
    private final List<Button> craftingButtons = new ArrayList<>();
    private EditBox searchBox;
    private Button craftToChannelButton;
    private Button craftToInventoryButton;
    private Button craftAndDropButton;
    private String previousFilter = "";
    private ScreenRectangle storageViewport;
    private ScreenRectangle craftingStorageViewport;

    public TesseractScreen(TesseractMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        titleLabelX = 8;
        titleLabelY = 5;
        inventoryLabelX = 23;
        inventoryLabelY = 177;
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (width - imageWidth + 4) / 2;
        topPos = Math.max(0, (height - imageHeight) / 2);
        storageViewport = PortableLayout.translate(new ScreenRectangle(5, 17, 210, 154), leftPos, topPos);
        craftingStorageViewport = PortableLayout.translate(new ScreenRectangle(5, 17, 210, 120), leftPos, topPos);
        craftingButtons.clear();

        searchBox = new EditBox(font, leftPos + SEARCH_X, topPos + SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT,
                Component.translatable("gui.avaritia.search"));
        searchBox.setMaxLength(64);
        searchBox.setBordered(false);
        searchBox.setValue(menu.filter);
        previousFilter = menu.filter;
        addRenderableWidget(UiInspector.name(searchBox, "tesseract.search"));

        UiInspector.name(addLegacyButton(CONTROL_X, CRAFTING_TOGGLE_Y, CONTROL_SIZE, CONTROL_SIZE,
                () -> toggleTextureX(menu.craftingMode), () -> CRAFTING_ICON_TEXTURE_Y,
                this::toggleCraftingMode, null), "tesseract.crafting");
        UiInspector.name(addLegacyButton(CONTROL_X, LOCK_Y, CONTROL_SIZE, CONTROL_SIZE,
                () -> toggleTextureX(menu.locked), () -> LOCK_ICON_TEXTURE_Y,
                this::toggleLock, Component.translatable("gui.avaritia.owner", menu.player.getName())), "tesseract.lock");
        UiInspector.name(addLegacyButton(CONTROL_X, CHANNEL_Y, CONTROL_SIZE, CONTROL_SIZE,
                () -> ICON_TEXTURE_X, () -> CHANNEL_ICON_TEXTURE_Y,
                () -> sendMenuButton(5), Component.translatable("gui.avaritia.channel.tip1", menu.channel.getName())), "tesseract.channel");
        UiInspector.name(addLegacyButton(CONTROL_X, SORT_Y, CONTROL_SIZE, CONTROL_SIZE,
                () -> ICON_TEXTURE_X, () -> sortTextureY(menu.sortType),
                this::cycleSort, Component.translatable("gui.avaritia.sort.tip1")), "tesseract.sort");
        UiInspector.name(addLegacyButton(CONTROL_X, VIEW_Y, CONTROL_SIZE, CONTROL_SIZE,
                () -> ICON_TEXTURE_X, () -> viewTextureY(menu.viewType),
                this::changeViewType, Component.translatable("gui.avaritia.view.all")), "tesseract.view");

        craftToChannelButton = addCraftButton(CRAFT_TO_CHANNEL_Y, CRAFT_TO_CHANNEL_TEXTURE_Y, 6,
                Component.translatable("gui.avaritia.craft.channel"));
        craftToInventoryButton = addCraftButton(CRAFT_TO_INVENTORY_Y, CRAFT_TO_INVENTORY_TEXTURE_Y, 10,
                Component.translatable("gui.avaritia.craft.inv"));
        craftAndDropButton = addCraftButton(CRAFT_AND_DROP_Y, CRAFT_AND_DROP_TEXTURE_Y, 14,
                Component.translatable("gui.avaritia.craft.drop"));
        UiInspector.name(craftToChannelButton, "tesseract.craft.channel");
        UiInspector.name(craftToInventoryButton, "tesseract.craft.inventory");
        UiInspector.name(craftAndDropButton, "tesseract.craft.drop");
        updateCraftingButtons();
        menu.dummyChannelContainer.refreshContainer(true);
    }

    private Button addLegacyButton(int x, int y, int buttonWidth, int buttonHeight, IntSupplier textureX,
                                   IntSupplier textureY, Runnable action, Component tooltip) {
        LegacyButton button = new LegacyButton(leftPos + x, topPos + y, buttonWidth, buttonHeight,
                textureX, textureY, action);
        if (tooltip != null) {
            button.setTooltip(Tooltip.create(tooltip));
        }
        return addRenderableWidget(button);
    }

    private Button addCraftButton(int y, int textureY, int baseId, Component tooltip) {
        Button button = addLegacyButton(CRAFT_BUTTON_X, y, CRAFT_BUTTON_WIDTH, CRAFT_BUTTON_HEIGHT,
                () -> ICON_TEXTURE_X, () -> textureY,
                () -> sendMenuButton(baseId + (hasShiftDown() ? 3 : 2)), tooltip);
        craftingButtons.add(button);
        return button;
    }

    private void sendMenuButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void toggleCraftingMode() {
        menu.craftingMode = !menu.craftingMode;
        menu.dummyChannelContainer.refreshContainer(true);
        searchBox.setFocused(false);
        updateCraftingButtons();
        sendMenuButton(1);
    }

    private void toggleLock() {
        if (menu.owner.equals(menu.player.getUUID()) || menu.owner.equals(Const.AVARITIA_FAKE_PLAYER.getId())) {
            menu.locked = !menu.locked;
            searchBox.setFocused(false);
            sendMenuButton(0);
        }
    }

    private void cycleSort() {
        if (hasShiftDown()) {
            menu.reverseSort();
            sendMenuButton(3);
        } else {
            menu.nextSort();
            sendMenuButton(2);
        }
    }

    private void changeViewType() {
        menu.changeViewType();
        sendMenuButton(4);
    }

    private void updateCraftingButtons() {
        craftingButtons.forEach(button -> {
            button.visible = menu.craftingMode;
            button.active = menu.craftingMode;
        });
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        String filter = searchBox.getValue().toLowerCase(Locale.ROOT);
        if (!previousFilter.equals(filter)) {
            previousFilter = filter;
            menu.filter = filter;
            menu.dummyChannelContainer.refreshContainer(true);
        }
        updateCraftingButtons();
    }

    private ScreenRectangle storageBounds() {
        return menu.craftingMode ? craftingStorageViewport : storageViewport;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        for (BackgroundSlice slice : background(menu.craftingMode)) {
            graphics.blit(GUI_IMG, leftPos, topPos + slice.destinationY(), 0, slice.sourceY(),
                    imageWidth, slice.height(), TEXTURE_SIZE, TEXTURE_SIZE);
        }
        UiInspector.region("tesseract.storage", storageBounds(), null, true);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderFluids(graphics);
        renderStoredCounts(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderFluids(GuiGraphics graphics) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(leftPos, topPos, 300.0D);
        menu.dummyChannelContainer.fluidStacks.forEach((index, fluidStack) -> {
            Slot slot = menu.slots.get(CHANNEL_SLOT_START + index);
            if (slot.isActive()) {
                FluidItemRender.renderFluid(fluidStack, poseStack, slot.x, slot.y, 0);
            }
        });
        poseStack.popPose();
    }

    private void renderStoredCounts(GuiGraphics graphics) {
        int count = Math.min(menu.dummyChannelContainer.formatCount.size(), menu.slots.size() - CHANNEL_SLOT_START);
        for (int index = 0; index < count; index++) {
            Slot slot = menu.slots.get(CHANNEL_SLOT_START + index);
            if (!slot.isActive() || slot.getItem().isEmpty()) continue;
            String value = menu.dummyChannelContainer.formatCount.get(index);
            graphics.pose().pushPose();
            graphics.pose().translate(leftPos + slot.x + 16, topPos + slot.y + 11, 300);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            graphics.drawString(font, value, -font.width(value), 0, 0xFFFFFF, true);
            graphics.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            if (searchBox.isMouseOver(mouseX, mouseY)) {
                searchBox.setValue("");
                searchBox.setFocused(true);
                return true;
            }
            if (sendRightClickCraft(craftToChannelButton, 6, mouseX, mouseY)
                    || sendRightClickCraft(craftToInventoryButton, 10, mouseX, mouseY)
                    || sendRightClickCraft(craftAndDropButton, 14, mouseX, mouseY)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean sendRightClickCraft(Button button, int baseId, double mouseX, double mouseY) {
        if (!button.visible || !button.isMouseOver(mouseX, mouseY)) return false;
        sendMenuButton(baseId + (hasShiftDown() ? 0 : 1));
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visibleSlots = menu.craftingMode ? 77 : 99;
        if (PortableLayout.contains(storageBounds(), mouseX, mouseY)
                && menu.dummyChannelContainer.sortedObject.size() > visibleSlots) {
            menu.dummyChannelContainer.onMouseScrolled(scrollY > 0);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        PacketDistributor.sendToServer(new C2SChannelFilterPack(menu.containerId, menu.filter));
        if (menu.channel instanceof ClientChannel channel) {
            channel.removeListener();
        }
        super.onClose();
    }

    private final class LegacyButton extends ImageButton {
        private final IntSupplier textureX;
        private final IntSupplier textureY;

        private LegacyButton(int x, int y, int buttonWidth, int buttonHeight, IntSupplier textureX,
                             IntSupplier textureY, Runnable action) {
            super(x, y, buttonWidth, buttonHeight, legacySprites, ignored -> action.run());
            this.textureX = textureX;
            this.textureY = textureY;
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(GUI_IMG, getX(), getY(), textureX.getAsInt(), textureY.getAsInt(),
                    width, height, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }
}
