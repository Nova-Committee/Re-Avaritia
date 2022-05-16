package nova.committee.avaritia.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.api.client.screen.BaseContainerScreen;
import nova.committee.avaritia.common.menu.ExtremeCraftingMenu;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 11:40
 * Version: 1.0
 */
public class ExtremeCraftingScreen extends BaseContainerScreen<ExtremeCraftingMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Static.MOD_ID, "textures/gui/extreme_crafting_gui.png");

    public ExtremeCraftingScreen(ExtremeCraftingMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, BACKGROUND, 234, 278, 512, 512);
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        this.font.draw(stack, title, 8.0F, 6.0F, 4210752);
        this.font.draw(stack, this.playerInventoryTitle, 39.0F, this.imageHeight - 94.0F, 4210752);
    }

    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        this.renderDefaultBg(stack, partialTicks, mouseX, mouseY);

    }
}
