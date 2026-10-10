package committee.nova.mods.avaritia.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * 1.21.11-compatible base screen for Avaritia container GUIs.
 *
 * <p>The 26.1 sources used the {@code GuiGraphicsExtractor} hook family
 * ({@code renderBackground}/{@code renderContents}/{@code renderLabels}).
 * Minecraft 1.21.11 still uses the classic {@code render*} hooks, so those are
 * restored here and the mod's own {@code renderBgs}/{@code renderFg} extension
 * points are invoked directly from them.
 */
public abstract class BaseContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements ContainerListener {
    protected static final int LABEL_COLOR = 0xFF404040;

    protected final Identifier bgTexture;
    protected final int bgImgWidth;
    protected final int bgImgHeight;

    public BaseContainerScreen(T menu, Inventory inventory, Component title, Identifier bgTexture) {
        this(menu, inventory, title, bgTexture, 176, 166, 256, 256);
    }

    public BaseContainerScreen(T menu, Inventory inventory, Component title, Identifier bgTexture, int bgWidth, int bgHeight) {
        this(menu, inventory, title, bgTexture, bgWidth, bgHeight, 256, 256);
    }

    public BaseContainerScreen(T menu, Inventory inventory, Component title, Identifier bgTexture, int bgWidth, int bgHeight, int bgImgWidth, int bgImgHeight) {
        super(menu, inventory, title);
        // 1.21.11 has only the three-argument AbstractContainerScreen constructor; the
        // image size is carried by the protected fields instead.
        this.imageWidth = bgWidth;
        this.imageHeight = bgHeight;
        this.bgTexture = bgTexture;
        this.bgImgWidth = bgImgWidth;
        this.bgImgHeight = bgImgHeight;
    }

    protected static String number(Object number) {
        return NumberFormat.getInstance().format(number);
    }

    protected static String fraction(Object number) {
        DecimalFormat df = new DecimalFormat("0.00%");
        return df.format(number);
    }

    protected void subInit() {
    }

    @Override
    protected void init() {
        super.init();
        this.subInit();
        this.menu.addSlotListener(this);
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        if (this.bgTexture != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, this.bgTexture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.bgImgWidth, this.bgImgHeight);
        }
    }

    /** Vanilla's abstract background hook; forwards to the mod's own renderBgs extension point. */
    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        this.renderBgs(graphics, partialTick, this.leftPos, this.topPos);
    }

    protected void renderBgs(GuiGraphics graphics, float partialTick, int x, int y) {
    }

    protected void renderFg(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderContents(graphics, mouseX, mouseY, partialTick);
        this.renderFg(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL_COLOR, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, LABEL_COLOR, false);
    }

    @Override
    public void dataChanged(@NotNull AbstractContainerMenu container, int dataSlotIndex, int value) {
    }

    @Override
    public void slotChanged(@NotNull AbstractContainerMenu container, int slotIndex, @NotNull ItemStack stack) {
    }
}
