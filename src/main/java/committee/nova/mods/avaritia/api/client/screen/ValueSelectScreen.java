package committee.nova.mods.avaritia.api.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.widget.GradatedSlider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/**
 * @Project: Avaritia-1.20
 * @Author: cnlimiter
 * @CreateTime: 2025/6/28 10:49
 * @Note:
 */
public class ValueSelectScreen extends Screen {
    private static final ResourceLocation SCREEN_TEXTURE = new ResourceLocation(Const.MOD_ID, "textures/screens/select_value.png");
    private static final int screenWidth = 245;
    private static final int screenHeight = 123;

    private final Component cached_title;
    private int range, value, left, top;

    protected ValueSelectScreen(Component pTitle, int range, int value) {
        super(pTitle);
        this.cached_title = pTitle;
        this.range = range;
        this.value = value;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        left = (width - screenWidth) / 2;
        top = (height - screenHeight) / 2;

        this.addRenderableWidget(new GradatedSlider(left + 8, top + 20, 205, (double) (value - 1) / (range - 1), range) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.translatable("gui.avaritia.select.value", 100 * ValueSelectScreen.this.value));
            }

            @Override
            protected void applyValue() {
                ValueSelectScreen.this.value = 1 + Math.round(ValueSelectScreen.this.value * (ValueSelectScreen.this.range - 1));
                this.value = (double) (ValueSelectScreen.this.value - 1) / (ValueSelectScreen.this.range - 1);
            }
        });
    }

    @Override
    public void render(GuiGraphics context, int x, int y, float partialTicks) {
        context.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        RenderSystem.setShaderTexture(0, SCREEN_TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        context.blit(SCREEN_TEXTURE, left, top, 0, 0, screenWidth, screenHeight);
        context.drawString(font, cached_title.getVisualOrderText(), (int) ((width - font.width(cached_title)) / 2.0f), top + 6, 4210752, false);
        super.render(context, x, y, partialTicks);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || minecraft.options.keyInventory.matches(keyCode, 0)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        //NetworkUtils.getInstance().c2s_update(blockPos, xRange, zRange, yRange, speed, redstoneMode);
        super.onClose();
    }
}
