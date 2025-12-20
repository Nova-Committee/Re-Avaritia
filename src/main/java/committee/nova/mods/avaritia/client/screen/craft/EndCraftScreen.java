package committee.nova.mods.avaritia.client.screen.craft;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.TierCraftMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 11:40
 * Version: 1.0
 */
public class EndCraftScreen extends BaseContainerScreen<TierCraftMenu> {

    public EndCraftScreen(TierCraftMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.END_CRAFT_TEX, 200, 242);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.drawString(font, this.playerInventoryTitle, 22, 148, 4210752, false);
    }
}
