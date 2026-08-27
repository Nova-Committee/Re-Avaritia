package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.container.DummyChannelContainer;
import committee.nova.mods.avaritia.common.container.InfinityChestContainer;
import committee.nova.mods.avaritia.common.menu.TesseractMenu;
import committee.nova.mods.avaritia.common.net.channel.C2SChannelFilterPack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;

import static committee.nova.mods.avaritia.client.screen.TesseractScreenLayout.*;

/** 11x9 Tesseract storage view with crafting controls and long-count overlays. */
public final class TesseractScreen extends BaseContainerScreen<TesseractMenu> {
    private static final Identifier TEXTURE = Const.rl("textures/gui/chest/channel_panel.png");
    private static final int TEXTURE_SIZE = 256;
    private EditBox search;
    private final List<Button> craftingButtons = new ArrayList<>();
    private final WidgetSprites legacySprites = new WidgetSprites(TEXTURE, TEXTURE);

    public TesseractScreen(TesseractMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, null, WIDTH, HEIGHT, WIDTH, HEIGHT);
        inventoryLabelX = 23;
        inventoryLabelY = INVENTORY_LABEL_Y;
        titleLabelX = 8;
        titleLabelY = 5;
    }

    @Override
    protected void subInit() {
        leftPos = (width - imageWidth + 4) / 2;
        topPos = Math.max(0, topPos);
        craftingButtons.clear();
        search = new EditBox(font, leftPos + SEARCH_X, topPos + SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT,
                Component.translatable("gui.avaritia.search"));
        search.setBordered(false);
        search.setMaxLength(64);
        search.setValue(menu.filter());
        search.setResponder(value -> menu.setFilterFromClient(value.toLowerCase(Locale.ROOT)));
        addRenderableWidget(search);

        addLegacyButton(CONTROL_X, CRAFTING_TOGGLE_Y, CONTROL_SIZE, CONTROL_SIZE,
                () -> toggleTextureX(menu.isCraftingMode()), () -> CRAFTING_ICON_TEXTURE_Y, 1,
                Component.translatable("gui.avaritia.tesseract.button.craft"));
        addLegacyButton(CONTROL_X, LOCK_Y, CONTROL_SIZE, CONTROL_SIZE,
                () -> toggleTextureX(menu.isLocked()), () -> LOCK_ICON_TEXTURE_Y, 0,
                Component.translatable("gui.avaritia.tesseract.button.lock"));
        addLegacyButton(CONTROL_X, CHANNEL_Y, CONTROL_SIZE, CONTROL_SIZE,
                () -> ICON_TEXTURE_X, () -> CHANNEL_ICON_TEXTURE_Y, 5,
                Component.translatable("gui.avaritia.tesseract.button.channel"));
        addLegacyButton(CONTROL_X, SORT_Y, CONTROL_SIZE, CONTROL_SIZE,
                () -> ICON_TEXTURE_X, () -> sortTextureY(menu.sortType()), -1,
                Component.translatable("gui.avaritia.tesseract.button.sort"));
        addLegacyButton(CONTROL_X, VIEW_Y, CONTROL_SIZE, CONTROL_SIZE,
                () -> ICON_TEXTURE_X, () -> viewTextureY(menu.viewType()), 4,
                Component.translatable("gui.avaritia.tesseract.button.view"));

        craftingButtons.add(addCraftButton(CRAFT_BUTTON_X, CRAFT_TO_CHANNEL_Y,
                CRAFT_TO_CHANNEL_TEXTURE_Y, 6, Component.translatable("gui.avaritia.tesseract.craft.channel_short")));
        craftingButtons.add(addCraftButton(CRAFT_BUTTON_X, CRAFT_TO_INVENTORY_Y,
                CRAFT_TO_INVENTORY_TEXTURE_Y, 10, Component.translatable("gui.avaritia.tesseract.craft.inventory_short")));
        craftingButtons.add(addCraftButton(CRAFT_BUTTON_X, CRAFT_AND_DROP_Y,
                CRAFT_AND_DROP_TEXTURE_Y, 14, Component.translatable("gui.avaritia.tesseract.craft.drop_short")));
        updateCraftingButtons();
    }

    private Button addLegacyButton(int x, int y, int width, int height, IntSupplier textureX,
                                   IntSupplier textureY, int id, Component tooltip) {
        LegacyButton button = new LegacyButton(leftPos + x, topPos + y, width, height, textureX, textureY,
                () -> sendMenuButton(id == -1 && isShiftDown() ? 3 : id == -1 ? 2 : id));
        button.setTooltip(Tooltip.create(tooltip));
        return addRenderableWidget(button);
    }

    private Button addCraftButton(int x, int y, int textureY, int baseId, Component tooltip) {
        LegacyButton button = new LegacyButton(leftPos + x, topPos + y, CRAFT_BUTTON_WIDTH,
                CRAFT_BUTTON_HEIGHT, () -> ICON_TEXTURE_X, () -> textureY, () -> {
                int amountOffset = isControlDown() ? (isShiftDown() ? 3 : 2) : (isShiftDown() ? 1 : 0);
                sendMenuButton(baseId + amountOffset);
            });
        button.setTooltip(Tooltip.create(tooltip));
        return addRenderableWidget(button);
    }

    private void sendMenuButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    private void updateCraftingButtons() {
        boolean visible = menu.isCraftingMode();
        craftingButtons.forEach(button -> {
            button.visible = visible;
            button.active = visible;
        });
    }

    private boolean isShiftDown() {
        var window = minecraft.getWindow();
        return com.mojang.blaze3d.platform.InputConstants.isKeyDown(window,
                com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT)
                || com.mojang.blaze3d.platform.InputConstants.isKeyDown(window,
                com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT);
    }

    private boolean isControlDown() {
        var window = minecraft.getWindow();
        return com.mojang.blaze3d.platform.InputConstants.isKeyDown(window,
                com.mojang.blaze3d.platform.InputConstants.KEY_LCONTROL)
                || com.mojang.blaze3d.platform.InputConstants.isKeyDown(window,
                com.mojang.blaze3d.platform.InputConstants.KEY_RCONTROL);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        updateCraftingButtons();
        for (BackgroundSlice slice : background(menu.isCraftingMode())) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos + slice.destinationY(),
                    0.0F, slice.sourceY(), imageWidth, slice.height(), TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    @Override
    protected void extractFg(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        search.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderSlotContents(GuiGraphicsExtractor graphics, ItemStack itemStack, Slot slot, String itemCount) {
        int menuIndex = menu.slots.indexOf(slot);
        if (menuIndex >= TesseractMenu.CHANNEL_START) {
            long amount = menu.projected().amount(menuIndex - TesseractMenu.CHANNEL_START);
            super.renderSlotContents(graphics, itemStack, slot, formatAmount(amount));
            return;
        }
        super.renderSlotContents(graphics, itemStack, slot, itemCount);
    }

    @Override
    protected void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
        int menuIndex = menu.slots.indexOf(slot);
        if (menuIndex < TesseractMenu.CHANNEL_START) {
            super.extractSlot(graphics, slot, mouseX, mouseY);
            return;
        }
        DummyChannelContainer.Entry entry = menu.projected().entry(menuIndex - TesseractMenu.CHANNEL_START);
        if (entry.kind() != DummyChannelContainer.Kind.FLUID || !slot.isActive()) {
            super.extractSlot(graphics, slot, mouseX, mouseY);
            return;
        }

        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                .get(entry.fluid().getFluid().defaultFluidState());
        TextureAtlasSprite sprite = model.stillMaterial().sprite();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, slot.x, slot.y, 16, 16);
        graphics.itemDecorations(font, new ItemStack(committee.nova.mods.avaritia.init.registry.ModItems.forge_energy.get()),
                slot.x, slot.y, formatAmount(entry.amount()));
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null) {
            int menuIndex = menu.slots.indexOf(hoveredSlot);
            if (menuIndex >= TesseractMenu.CHANNEL_START) {
                DummyChannelContainer.Entry entry = menu.projected().entry(menuIndex - TesseractMenu.CHANNEL_START);
                if (entry.kind() == DummyChannelContainer.Kind.EMPTY) return;
                List<Component> lines = new ArrayList<>();
                if (entry.kind() == DummyChannelContainer.Kind.ITEM) {
                    lines.addAll(getTooltipFromContainerItem(entry.item().toStack(1)));
                } else {
                    lines.add(Component.literal(entry.displayName()));
                    lines.add(Component.literal(entry.identifier()).withColor(0x777777));
                }
                String unit = entry.kind() == DummyChannelContainer.Kind.FLUID ? " mB"
                        : entry.kind() == DummyChannelContainer.Kind.ENERGY ? " FE" : "";
                lines.add(Component.literal(InfinityChestContainer.formatExactAmount(entry.amount()) + unit));
                graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
                return;
            }
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return search.keyPressed(event) || search.canConsumeInput() || super.keyPressed(event);
    }

    @Override
    public void onClose() {
        NetworkHandler.sendToServer(new C2SChannelFilterPack(menu.containerId, search.getValue()));
        super.onClose();
    }

    private static String formatAmount(long value) {
        if (value < 1_000) return Long.toString(value);
        if (value == Long.MAX_VALUE) return "MAX";
        String[] suffixes = {"K", "M", "G", "T", "P", "E"};
        double scaled = value;
        int suffix = -1;
        while (scaled >= 1_000 && suffix + 1 < suffixes.length) {
            scaled /= 1_000;
            suffix++;
        }
        return (scaled >= 100 ? String.format(Locale.ROOT, "%.0f", scaled)
                : String.format(Locale.ROOT, "%.1f", scaled)) + suffixes[suffix];
    }

    private final class LegacyButton extends ImageButton {
        private final IntSupplier textureX;
        private final IntSupplier textureY;

        private LegacyButton(int x, int y, int width, int height, IntSupplier textureX,
                             IntSupplier textureY, Runnable action) {
            super(x, y, width, height, legacySprites, ignored -> action.run());
            this.textureX = textureX;
            this.textureY = textureY;
        }

        @Override
        public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), textureX.getAsInt(),
                    textureY.getAsInt(), width, height, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }
}
