package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.container.chest.InfinityBoxContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class InfinityChestScreen extends BaseContainerScreen<InfinityBoxContainer> {


    @Nullable
    private Slot clickedSlot;
    private ItemStack draggingItem = ItemStack.EMPTY;
    private boolean isSplittingStack;
    private int quickCraftingType;

    public InfinityChestScreen(InfinityBoxContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 500;
        this.imageHeight = 275;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = this.imageWidth - 330;
        this.titleLabelX = this.imageWidth / 2 - 240;
    }

    @Override
    protected void renderBg(GuiGraphics matrixStack, float partialTicks, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Res.INFINITY_CHEST_TEX);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        matrixStack.blit(Res.INFINITY_CHEST_TEX, i, j, 0, 0, this.imageWidth, this.imageHeight, 500, 275);
    }


    @Override
    protected void renderLabels(GuiGraphics graphics, int i, int i1) {
        super.renderLabels(graphics, i, i1);
    }


    @Override
    public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderSlotCount(matrixStack);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    public void renderSlot(@NotNull GuiGraphics graphics, Slot slot) {
        int i = slot.x;
        int j = slot.y;
        ItemStack itemstack = slot.getItem();
        boolean flag = false;
        boolean flag1 = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;

        if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemstack.isEmpty()) {
            itemstack = itemstack.copyWithCount(itemstack.getCount() / 2);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 100.0F);
        if (itemstack.isEmpty() && slot.isActive()) {
            Pair<ResourceLocation, ResourceLocation> pair = slot.getNoItemIcon();
            if (pair != null) {
                TextureAtlasSprite textureatlassprite;
                if (this.minecraft != null) {
                    textureatlassprite = (TextureAtlasSprite)this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                    graphics.blit(i, j, 0, 16, 16, textureatlassprite);
                }
                flag1 = true;
            }
        }

        if (!flag1) {
            if (flag) {
                graphics.fill(i, j, i + 16, j + 16, -2130706433);
            }

            graphics.renderItem(itemstack, i, j, slot.x + slot.y * this.imageWidth);
        }

        graphics.pose().popPose();
    }

    public void renderSlotCount(GuiGraphics graphics) {
        PoseStack poseStack = graphics.pose();
        for (int i = 0; i < menu.slots.size(); i++) {
            int count = menu.slots.get(i).getItem().getCount();
            float fontSize = 0.5F;
            if (count != 0L) {
                String stringCount = String.valueOf(count);
                if (count >= 1_000 && count < 1_000_000)
                    stringCount = count / 1_000 + "K";
                else if (count >= 1_000_000 && count < 1_000_000_000)
                    stringCount = count / 1_000_000 + "M";
                else if (count >= 1_000_000_000)
                    stringCount = count / 1_000_000_000 + "B";
                RenderSystem.enableDepthTest();
                poseStack.pushPose();
                poseStack.translate(leftPos + menu.getSlot(i).x, topPos + menu.getSlot(i).y, 300.0D);
                poseStack.scale(fontSize, fontSize, 1.0F);
                graphics.drawString(this.font, stringCount,
                        (int) ((16 - this.font.width(stringCount) * fontSize) / fontSize),
                        (int) ((16 - this.font.lineHeight * fontSize) / fontSize),
                        16777215);
                poseStack.popPose();
            }
        }
    }
}