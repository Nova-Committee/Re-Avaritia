package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.container.slot.InfinitySlot;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Screen for the 9x27 local infinity chest inventory.
 */
public class InfinityChestScreen extends AbstractContainerScreen<InfinityChestMenu> {
    private static final ResourceLocation BACKGROUND = Const.rl("textures/gui/chest/infinity_chest_gui.png");
    private static final int TEXTURE_WIDTH = 500;
    private static final int TEXTURE_HEIGHT = 275;

    public InfinityChestScreen(InfinityChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = TEXTURE_WIDTH;
        this.imageHeight = TEXTURE_HEIGHT;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderInfinitySlotCounts(guiGraphics);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 170, this.imageHeight - 94, 4210752, false);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack stack = this.hoveredSlot.getItem();
            List<Component> components = this.getTooltipFromContainerItem(stack);
            if (this.hoveredSlot instanceof InfinitySlot) {
                components.add(Component.translatable("container.infinity_chest", stack.getCount(), this.menu.getSlotMaxStack(this.hoveredSlot)));
            }
            guiGraphics.renderTooltip(this.font, components, stack.getTooltipImage(), stack, mouseX, mouseY);
        }
    }

    @Override
    public void renderSlot(@NotNull GuiGraphics guiGraphics, @NotNull Slot slot) {
        if (!(slot instanceof InfinitySlot) || !slot.hasItem()) {
            super.renderSlot(guiGraphics, slot);
            return;
        }

        ItemStack stack = slot.getItem();
        int count = stack.getCount();
        if (count > 1) {
            stack.setCount(1);
            super.renderSlot(guiGraphics, slot);
            stack.setCount(count);
        } else {
            super.renderSlot(guiGraphics, slot);
        }

    }

    private void renderInfinitySlotCounts(GuiGraphics guiGraphics) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 500.0F);
        for (Slot slot : this.menu.slots) {
            if (slot instanceof InfinitySlot && slot.hasItem()) {
                String countText = getCountText(slot.getItem().getCount());
                int x = this.leftPos + slot.x + 19 - 2 - this.font.width(countText);
                int y = this.topPos + slot.y + 6 + 3;
                guiGraphics.drawString(this.font, countText, x, y, 16777215, true);
            }
        }
        guiGraphics.pose().popPose();
    }

    private static String getCountText(int count) {
        if (count < 1000) {
            return String.valueOf(count);
        }
        if (count >= 1_000_000_000) {
            return count / 1_000_000_000 + "G";
        }
        if (count >= 1_000_000) {
            return count / 1_000_000 + "M";
        }
        return count / 1000 + "K";
    }
}
