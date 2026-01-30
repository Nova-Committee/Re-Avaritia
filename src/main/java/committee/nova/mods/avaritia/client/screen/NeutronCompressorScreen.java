package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.client.screen.side.SideConfigButton;
import committee.nova.mods.avaritia.common.menu.NeutronCompressorMenu;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 18:16
 * Version: 1.0
 */
public class NeutronCompressorScreen extends BaseContainerScreen<NeutronCompressorMenu> {
    private Button lockButton;
    private Button ejectButton;
    private Button configButton;

    public NeutronCompressorScreen(NeutronCompressorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.NEUTRON_COMPRESSOR_TEX);
    }

    @Override
    protected void init() {
        super.init();
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        // 添加锁定按钮
        this.lockButton =  new LockButton(x + 40, y + 55);

        // 添加弹出按钮
        this.ejectButton = new EjectButton(x + 40, y + 20);

        // 添加配置按钮
        this.configButton = new SideConfigButton(this, x - 20, y);

        this.addRenderableWidget(this.lockButton);
        this.addRenderableWidget(this.ejectButton);
        this.addRenderableWidget(this.configButton);

        // 更新按钮初始状态
        this.lockButton.setMessage(Component.literal(this.isRecipeLocked() ? "🔒" : "🔓"));
    }


    private void lockRecipe() {
        if (this.minecraft != null && this.minecraft.player != null) {
            NetworkHandler.sendCompressorLockPacket(this.menu.getBlockPos(), !isRecipeLocked());
            this.lockButton.setMessage(Component.literal(!isRecipeLocked() ? "🔒" : "🔓"));
        }
    }
    private void ejectMaterials() {
        if (this.minecraft != null && this.minecraft.player != null) {
            NetworkHandler.sendCompressorEjectPacket(this.menu.getBlockPos());
        }
    }



    private class LockButton extends ImageButton {
        private final List<FormattedCharSequence> tips = new ArrayList<>();

        public LockButton(int pX, int pY) {
            super(pX, pY, 14, 13, new WidgetSprites(Res.NEUTRON_COMPRESSOR_TEX, Res.NEUTRON_COMPRESSOR_TEX), pButton -> lockRecipe());
            if (isRecipeLocked()) {
                tips.add(Component.translatable("button.avaritia.lock_button_1").withStyle(ChatFormatting.GREEN).getVisualOrderText());
                tips.add(Component.translatable("button.avaritia.lock_button_2").withStyle(ChatFormatting.GRAY).getVisualOrderText());
            } else {
                tips.add(Component.translatable("button.avaritia.lock_button_3").withStyle(ChatFormatting.YELLOW).getVisualOrderText());
                tips.add(Component.translatable("button.avaritia.lock_button_4").withStyle(ChatFormatting.GRAY).getVisualOrderText());
            }
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            var resourceLocation = Res.NEUTRON_COMPRESSOR_TEX;
            var xTexStart = 177;
            var yTexStart = 47;
            if (this.isHovered) {
                if (isRecipeLocked()) pPoseStack.blit(resourceLocation, this.getX(), this.getY(), xTexStart + 14 + 1, yTexStart + 13 + 1, this.width, this.height, 256, 256);
                else pPoseStack.blit(resourceLocation, this.getX(), this.getY(), xTexStart + 14 + 1, yTexStart, this.width, this.height, 256, 256);
                setTooltipForNextRenderPass(tips);
            } else {
                if (isRecipeLocked()) pPoseStack.blit(resourceLocation, this.getX(), this.getY(), xTexStart, yTexStart + 13 + 1, this.width, this.height, 256, 256);
                else pPoseStack.blit(resourceLocation, this.getX(), this.getY(), xTexStart, yTexStart, this.width, this.height, 256, 256);
            }


        }
    }

    private class EjectButton extends ImageButton {
        private final List<FormattedCharSequence> tips = new ArrayList<>();

        public EjectButton(int pX, int pY) {
            super(pX, pY, 14, 11, new WidgetSprites(Res.NEUTRON_COMPRESSOR_TEX, Res.NEUTRON_COMPRESSOR_TEX), pButton -> ejectMaterials());
            tips.add(Component.translatable("button.avaritia.eject_button_1").withStyle(ChatFormatting.AQUA).getVisualOrderText());
            tips.add(Component.translatable("button.avaritia.eject_button_2").withStyle(ChatFormatting.GRAY).getVisualOrderText());
        }

        @Override
        @ParametersAreNonnullByDefault
        public void renderWidget(GuiGraphics pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            var resourceLocation = Res.NEUTRON_COMPRESSOR_TEX;
            var xTexStart = 177;
            var yTexStart = 35;
            if (this.isHovered) {
                setTooltipForNextRenderPass(tips);
                pPoseStack.blit(resourceLocation, this.getX(), this.getY(), xTexStart + 14 + 1, yTexStart, this.width, this.height, 256, 256);
            } else {
                pPoseStack.blit(resourceLocation, this.getX(), this.getY(), xTexStart, yTexStart, this.width, this.height, 256, 256);
            }
        }
    }

    @Override
    protected void renderFg(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        // 原有的材料提示
        if (pMouseX > x + 63 && pMouseX < x + 79 && pMouseY > y + 35 && pMouseY < y + 51) {
            List<Component> tooltip = new ArrayList<>();

            if (this.getMaterialCount() < 1) {
                tooltip.add(ModTooltips.EMPTY.color(ChatFormatting.WHITE).build());
            } else {
                if (this.hasMaterialStack()) {
                    tooltip.add(this.getMaterialStackDisplayName());
                }

                var text = Component.literal(number(this.getMaterialCount()) + " / " + number(this.getMaterialsRequired()));

                tooltip.add(text);
            }

            pGuiGraphics.renderComponentTooltip(font, tooltip, pMouseX, pMouseY);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();
        stack.drawString(font, title, (this.imageWidth / 2 - this.font.width(title) / 2), 6, 4210752, false);
        stack.drawString(font, this.playerInventoryTitle, 8, this.imageHeight - 94, 4210752, false);
    }


    @Override
    protected void renderBgs(GuiGraphics pGuiGraphics, float pPartialTick, int pX, int pY) {
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        if (this.hasRecipe()) {
            if (this.getMaterialCount() > 0 && this.getMaterialsRequired() > 0) {
                int i2 = this.getMaterialBarScaled(16);
                pGuiGraphics.blit(Res.NEUTRON_COMPRESSOR_TEX, x + 63, y + 35, 176, 18, i2 + 1, 16);
            }

            if (this.getProgress() > 0 && this.getMaterialCount() >= this.getMaterialsRequired()) {
                int i2 = this.getProgressBarScaled(22);
                pGuiGraphics.blit(Res.NEUTRON_COMPRESSOR_TEX, x + 89, y + 35, 176, 0, i2 + 1, 16);
            }
        }
    }

    private Component getMaterialStackDisplayName() {
        var level = this.getMinecraft().level;

        if (level != null) {
            var container = this.getMenu();
            var tile = level.getBlockEntity(container.getBlockPos());

            if (tile instanceof NeutronCompressorTile compressor) {
                var materialStack = compressor.getMaterialStack();

                return materialStack.getHoverName();
            }
        }

        return Component.literal("");
    }

    public boolean hasRecipe() {
        if (this.menu.getTileEntity() == null)
            return false;

        return this.menu.getTileEntity().hasRecipe();
    }

    public boolean hasMaterialStack() {
        if (this.menu.getTileEntity() == null)
            return false;

        return this.menu.getTileEntity().hasMaterialStack();
    }

    public int getProgress() {
        if (this.menu.getTileEntity() == null)
            return 0;

        return this.menu.getProgress();
    }

    public int getMaterialCount() {
        if (this.menu.getTileEntity() == null)
            return 0;

        return this.menu.getTileEntity().getMaterialCount();
    }

    public int getMaterialsRequired() {
        if (this.menu.getTileEntity() == null)
            return 0;

        return this.menu.getTileEntity().getMaterialsRequired();
    }

    public int getTimeRequired() {
        if (this.menu.getTileEntity() == null)
            return 0;

        return this.menu.getTileEntity().getTimeRequired();
    }

    public int getMaterialBarScaled(int pixels) {
        int i = Mth.clamp(this.getMaterialCount(), 0, this.getMaterialsRequired());
        int j = this.getMaterialsRequired();
        return j != 0 && i != 0 ? i * pixels / j : 0;
    }

    public int getProgressBarScaled(int pixels) {
        int i = Mth.clamp(this.getProgress(), 0, this.getTimeRequired());
        int j = this.getTimeRequired();
        return j != 0 && i != 0 ? i * pixels / j : 0;
    }

    public boolean isRecipeLocked() {
        if (this.menu.getTileEntity() == null)
            return false;

        return this.menu.getTileEntity().isRecipeLocked();
    }

    public boolean canEjectMaterials() {
        if (this.menu.getTileEntity() == null)
            return false;

        return this.menu.getTileEntity().getMaterialCount() > 0;
    }


}
