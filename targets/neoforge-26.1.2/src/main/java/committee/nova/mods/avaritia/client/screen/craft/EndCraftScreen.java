package committee.nova.mods.avaritia.client.screen.craft;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.TierCraftMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class EndCraftScreen extends BaseContainerScreen<TierCraftMenu> {

    public EndCraftScreen(TierCraftMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, Res.END_CRAFT_TEX, 200, 242);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphicsExtractor stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        stack.text(font, title, 8, 6, LABEL_COLOR, false);
        stack.text(font, this.playerInventoryTitle, 22, 148, LABEL_COLOR, false);
    }
}
