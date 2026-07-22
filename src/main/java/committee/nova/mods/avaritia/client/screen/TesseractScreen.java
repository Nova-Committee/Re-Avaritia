package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.container.DummyChannelContainer;
import committee.nova.mods.avaritia.common.menu.TesseractMenu;
import committee.nova.mods.avaritia.common.net.channel.C2SChannelFilterPack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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

/** 11x9 Tesseract storage view with crafting controls and long-count overlays. */
public final class TesseractScreen extends BaseContainerScreen<TesseractMenu> {
    private static final Identifier TEXTURE = Const.rl("textures/gui/chest/channel_panel.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int WIDTH = 218;
    private static final int HEIGHT = 256;
    private EditBox search;
    private final List<Button> craftingButtons = new ArrayList<>();

    public TesseractScreen(TesseractMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, null, WIDTH, HEIGHT, WIDTH, HEIGHT);
        inventoryLabelX = 23;
        inventoryLabelY = 177;
        titleLabelX = 8;
        titleLabelY = 5;
    }

    @Override
    protected void subInit() {
        topPos = Math.max(0, topPos);
        craftingButtons.clear();
        search = new EditBox(font, leftPos + 72, topPos + 4, 122, 12,
                Component.translatable("gui.avaritia.search"));
        search.setBordered(false);
        search.setMaxLength(64);
        search.setValue(menu.filter());
        search.setResponder(value -> menu.setFilterFromClient(value.toLowerCase(Locale.ROOT)));
        addRenderableWidget(search);

        addMenuButton(Component.translatable("gui.avaritia.tesseract.button.lock"), 198, 17, 0, 18);
        addMenuButton(Component.translatable("gui.avaritia.tesseract.button.craft"), 198, 35, 1, 18);
        addMenuButton(Component.translatable("gui.avaritia.tesseract.button.sort"), 198, 53, 2, 18);
        addMenuButton(Component.translatable("gui.avaritia.tesseract.button.reverse"), 198, 71, 3, 18);
        addMenuButton(Component.translatable("gui.avaritia.tesseract.button.view"), 198, 89, 4, 18);
        addMenuButton(Component.translatable("gui.avaritia.tesseract.button.channel"), 198, 107, 5, 18);

        craftingButtons.add(addCraftButton(Component.translatable("gui.avaritia.tesseract.craft.channel_short"), 145, 136, 6));
        craftingButtons.add(addCraftButton(Component.translatable("gui.avaritia.tesseract.craft.inventory_short"), 145, 154, 10));
        craftingButtons.add(addCraftButton(Component.translatable("gui.avaritia.tesseract.craft.drop_short"), 145, 172, 14));
        updateCraftingButtons();
    }

    private Button addMenuButton(Component label, int x, int y, int id, int width) {
        return addRenderableWidget(Button.builder(label, ignored -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            }
        }).bounds(leftPos + x, topPos + y, width, 16).build());
    }

    private Button addCraftButton(Component label, int x, int y, int baseId) {
        return addRenderableWidget(Button.builder(label, ignored -> {
            if (minecraft != null && minecraft.gameMode != null) {
                int amountOffset = isControlDown() ? (isShiftDown() ? 3 : 2) : (isShiftDown() ? 1 : 0);
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, baseId + amountOffset);
            }
        }).bounds(leftPos + x, topPos + y, 68, 16).build());
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
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F,
                imageWidth, imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    protected void extractFg(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        search.extractRenderState(graphics, mouseX, mouseY, partialTick);
        String state = (menu.isLocked() ? "🔒" : "🔓") + "  "
                + Component.translatable("gui.avaritia.tesseract.view." + menu.viewType()).getString();
        graphics.text(font, state, leftPos + 43, topPos + 105, 0xFF404040, false);
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
                lines.add(Component.literal(String.format(Locale.ROOT, "%,d", entry.amount()) + unit));
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
}
