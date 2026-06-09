package committee.nova.mods.avaritia.client.screen.craft;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.TierCraftMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SculkCraftScreen extends BaseContainerScreen<TierCraftMenu> {
    public SculkCraftScreen(TierCraftMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.SCULK_CRAFT_TEX, 176, 167);
    }

    @Override
    protected void renderLabels(GuiGraphicsExtractor stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.text(font, title, 8, 6, 4210752, false);
        stack.text(font, this.playerInventoryTitle, 10, 72, 4210752, false);
    }
}
