package nova.committee.avaritia.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
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
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Static.MOD_ID, "textures/gui/neutron_collector.png");
    private NeutronCollectorTile tile;

    public NeutronCollectorScreen(NeutronCollectorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, BACKGROUND, 175, 165, 255, 255);


    }

    @Override
    protected void init() {
        super.init();
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
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        this.font.draw(stack, title, (float) (175 / 2 - this.font.width(title) / 2), 6.0F, 4210752);
        this.font.draw(stack, this.playerInventoryTitle, 8.0F, 165 - 94.0F, 4210752);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        int i = this.getGuiLeft();
        int j = this.getGuiTop();
        blit(pPoseStack, i, j, 0.0F, 0.0F, 175, 165, 255, 255);
        int k = getMenu().getTimer();
        this.blit(pPoseStack, i + 100, j + 48 - k, 176, 16 - k, 1, k);
    }


}
