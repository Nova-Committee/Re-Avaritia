package committee.nova.mods.avaritia.api.client.widget;

import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/22 02:09
 * @Description:
 */
@Environment(EnvType.CLIENT)
public abstract class SimpleScrollBar extends AbstractWidget {

    @Getter
    @Setter
    private boolean scrolling = false;
    @Getter
    private double scrollTagSize = 4;
    @Getter
    private double scrolledOn = 0.0D;
    @Setter
    private int scrollBarTagColor = FastColor.ARGB32.color(255, 77, 73, 77);
    @Setter
    private int scrollBarBackgroundColor = FastColor.ARGB32.color(255, 36, 30, 31);


    public SimpleScrollBar(int x, int y, int weight, int height, Component message) {
        super(x, y, weight, height, message);
        if (height < weight * 2) setSize(weight, weight * 2);
    }

    public SimpleScrollBar(int x, int y, int weight, int height) {
        this(x, y, weight, height, CommonComponents.EMPTY);
    }

    public void setPos(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    public void setSize(int weight, int height) {
        this.width = weight;
        this.height = height;
    }

    public void setScrolledOn(double scrolledOn) {
        this.scrolledOn = Math.max(0.0D, Math.min(1.0D, scrolledOn));
        this.draggedTo(this.scrolledOn);
    }

    public void setScrollTagSize(double scrollTagSize) {
        this.scrollTagSize = Math.max(width, Math.min(height, scrollTagSize));
    }

    public double getScrollOn() {
        return this.scrolledOn;
    }

    public boolean canScroll() {
        return scrollTagSize < height;
    }


    @Override
    public void onClick(double pMouseX, double pMouseY) {
        this.scrolling = true;
        this.onDragTo(pMouseY);
    }

    @Override
    public void onRelease(double pMouseX, double pMouseY) {
        this.scrolling = false;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double pDragX, double pDragY) {
        this.onDragTo(mouseY);
    }

    public void onDragTo(double mouseY) {
        if (mouseY <= getY()) {
            scrolledOn = 0.0D;
        } else if (mouseY >= getY() + getHeight()) {
            scrolledOn = 1.0D;
        } else {
            double v = (mouseY - getY() - (scrollTagSize / 2)) / (getHeight() - scrollTagSize);
            setScrolledOn(v);
        }
        this.draggedTo(scrolledOn);
    }

    abstract public void draggedTo(double scrolledOn);

    abstract public void beforeRender();

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.beforeRender();
        if (!this.visible) return;
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, scrollBarBackgroundColor);
        double v = getY() + ((getHeight() - scrollTagSize) * scrolledOn);
        guiGraphics.fill(getX(), (int) Math.floor(v), getX() + width, (int) Math.ceil(v + scrollTagSize), scrollBarTagColor);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        this.defaultButtonNarrationText(pNarrationElementOutput);
    }
}

