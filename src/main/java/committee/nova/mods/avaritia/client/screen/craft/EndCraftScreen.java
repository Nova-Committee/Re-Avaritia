package committee.nova.mods.avaritia.client.screen.craft;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.TierCraftMenu;
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
public class EndCraftScreen extends BaseContainerScreen<TierCraftMenu> {
    private static final ResourceLocation BACKGROUND = Const.rl( "textures/gui/craft/end_crafting_table_gui.png");

    public EndCraftScreen(TierCraftMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, BACKGROUND, 200, 242);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.drawString(font, title, 27, 148, 4210752, false);
    }

    @Override
    protected void renderBgOthers(GuiGraphics pGuiGraphics, int pX, int pY) {

    }
}
