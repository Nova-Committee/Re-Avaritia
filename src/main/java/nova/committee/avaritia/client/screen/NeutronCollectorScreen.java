package nova.committee.avaritia.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.api.client.screen.BaseContainerScreen;
import nova.committee.avaritia.common.menu.NeutronCollectorMenu;
import nova.committee.avaritia.common.tile.NeutronCollectorTile;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 15:12
 * Version: 1.0
 */
public class NeutronCollectorScreen extends BaseContainerScreen<NeutronCollectorMenu> {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(Static.MOD_ID, "textures/gui/neutron_collector_gui.png");
    private NeutronCollectorTile tile;

    public NeutronCollectorScreen(NeutronCollectorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, BACKGROUND, 175, 166, 255, 255);
    }

    @Override
    protected void init() {
        int x = this.getGuiLeft();
        int y = this.getGuiTop();
        var pos = this.getMenu().getPos();
        this.tile = this.getTileEntity();
    }

    private NeutronCollectorTile getTileEntity() {
        var level = this.getMinecraft().level;

        if (level != null) {
            var tile = level.getBlockEntity(this.getMenu().getPos());

            if (tile instanceof NeutronCollectorTile compressor)
                return compressor;
        }

        return null;
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        this.font.draw(stack, title, (float) (this.imageWidth / 2 - this.font.width(title) / 2), 6.0F, 4210752);
        this.font.draw(stack, this.playerInventoryTitle, 8.0F, this.imageHeight - 94.0F, 4210752);
    }

    @Override
    protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY) {
        this.renderDefaultBg(matrix, partialTicks, mouseX, mouseY);
    }
}
