package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.common.container.slot.InfinitySlot;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.util.List;

public class InfinityChestScreen extends AbstractContainerScreen<InfinityChestMenu> {
    public InfinityChestScreen(InfinityChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 500, 275);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ScreenTextures.INFINITY_CHEST, this.getLeftPos(), this.getTopPos(), 0.0F, 0.0F, this.imageWidth, this.imageHeight, 500, 275);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        graphics.text(this.font, this.playerInventoryTitle, 170, this.imageHeight - 94, 4210752, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack itemStack = this.hoveredSlot.getItem();
            List<Component> components = this.getTooltipFromContainerItem(itemStack);
            if (this.hoveredSlot instanceof InfinitySlot) {
                components.add(Component.translatable("container.infinity_chest", itemStack.getCount(), this.menu.getSlotMaxStack(this.hoveredSlot)));
            }
            graphics.setTooltipForNextFrame(this.font, components, itemStack.getTooltipImage(), itemStack, mouseX, mouseY, itemStack.get(DataComponents.TOOLTIP_STYLE));
        }
    }

    @Override
    protected void renderSlotContents(GuiGraphicsExtractor graphics, ItemStack itemStack, Slot slot, String itemCount) {
        String text = itemCount;
        int count = itemStack.getCount();

        if (count >= 1000) {
            text = new DecimalFormat("#").format(count / 1000) + "K";
        }
        if (count >= 10000) {
            text = new DecimalFormat("#").format(count / 10000) + "W";
        }
        if (count >= 1000000) {
            text = new DecimalFormat("#").format(count / 1000000) + "M";
        }
        if (count >= 1000000000) {
            text = new DecimalFormat("#").format(count / 1000000000) + "G";
        }

        super.renderSlotContents(graphics, itemStack, slot, text);
    }
}
