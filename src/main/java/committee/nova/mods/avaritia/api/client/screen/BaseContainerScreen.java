package committee.nova.mods.avaritia.api.client.screen;

import committee.nova.mods.avaritia.api.iface.IDataReceiver;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
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
 * Minecraft 1.21.11 still uses the classic {@code render*} hooks, restored here;
 * the mod's own {@code renderBgs}/{@code renderFg} extension points are now
 * invoked directly from those hooks.
 */
public abstract class BaseContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements ContainerListener, IDataReceiver {
    protected static final int LABEL_COLOR = 0xFF404040;

    protected final Identifier bgTexture;
    protected final int bgImgWidth;
    protected final int bgImgHeight;

    public BaseContainerScreen(T container, Inventory inventory, Component title) {
        this(container, inventory, title, null);
    }

    public BaseContainerScreen(T container, Inventory inventory, Component title, Identifier bgTexture) {
        this(container, inventory, title, bgTexture, 176, 166, 256, 256);
    }

    public BaseContainerScreen(T container, Inventory inventory, Component title, Identifier bgTexture, int bgWidth, int bgHeight) {
        this(container, inventory, title, bgTexture, bgWidth, bgHeight, 256, 256);
    }

    public BaseContainerScreen(T container, Inventory inventory, Component title, Identifier bgTexture, int bgWidth, int bgHeight, int bgImgWidth, int bgImgHeight) {
        super(container, inventory, title);
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
    public void renderContents(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderContents(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderFg(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    protected void renderFg(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL_COLOR, false);
        pGuiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, LABEL_COLOR, false);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (this.bgTexture != null) pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.bgTexture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.bgImgWidth, this.bgImgHeight);
    }

    /** Vanilla's abstract background hook; forwards to the mod's own renderBgs extension point. */
    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        this.renderBgs(pGuiGraphics, pPartialTick, this.leftPos, this.topPos);
    }

    protected void renderBgs(GuiGraphics pGuiGraphics, float pPartialTick, int pX, int pY){
    }

    public void dataChanged(@NotNull AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {
    }

    public void slotChanged(@NotNull AbstractContainerMenu pContainerToSend, int pSlotInd, @NotNull ItemStack pStack) {
    }

    @Override
    public void receive(CompoundTag tag) {
    }
}
