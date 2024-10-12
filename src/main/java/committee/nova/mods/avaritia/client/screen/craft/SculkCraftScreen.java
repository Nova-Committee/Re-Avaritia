package committee.nova.mods.avaritia.client.screen.craft;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.ModCraftMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 11:40
 * Version: 1.0
 */
public class SculkCraftScreen extends BaseContainerScreen<ModCraftMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Static.MOD_ID, "textures/gui/craft/sculk_crafting_table_gui.png");

    public SculkCraftScreen(ModCraftMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, BACKGROUND, 176, 161, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.drawString(font, title, 15, 72, 4210752, false);
        //stack.drawString(font, this.playerInventoryTitle, 39, this.imageHeight - 94, 4210752, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics stack, float partialTicks, int mouseX, int mouseY) {
        this.renderDefaultBg(stack);
    }
}
