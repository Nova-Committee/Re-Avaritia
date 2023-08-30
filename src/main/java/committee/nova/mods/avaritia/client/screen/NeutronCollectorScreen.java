package committee.nova.mods.avaritia.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.client.screen.BaseContainerScreen;
import committee.nova.mods.avaritia.common.menu.NeutronCollectorMenu;
import committee.nova.mods.avaritia.common.tile.NeutronCollectorTile;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
        super(container, inventory, title, BACKGROUND, 176, 166, 256, 256);


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
    public void render(@NotNull PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        if (mouseX > x + 99 && mouseX < x + 104 && mouseY > y + 30 && mouseY < y + 50) {
            List<Component> tooltip = new ArrayList<>();

            if (this.getProgress() > 0) {
                double i = (double) getProgress() / getTimeRequired();
                var text = ModTooltips.PROGRESS.args(fraction(i)).build();
                tooltip.add(text);
            }

            renderComponentTooltip(stack, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(@NotNull PoseStack stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        drawString(stack, font, title, (176 / 2 - this.font.width(title) / 2), 6, 4210752);
        drawString(stack, font, this.playerInventoryTitle, 8, 166 - 94, 4210752);
    }

    @Override
    protected void renderBg(@NotNull PoseStack stack, float pPartialTick, int pMouseX, int pMouseY) {
        int i = this.getGuiLeft();
        int j = this.getGuiTop();
        blit(stack, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.bgImgWidth, this.bgImgHeight);
        if (this.getProgress() > 0) {
            int i2 = this.getProgressBarScaled(18);
            blit(stack, i + 99, j + 49 - i2, 176, 18 - i2, 4, i2);
        }
    }

    public int getProgress() {
        if (this.tile == null)
            return 0;

        return this.menu.getProgress();
    }

    public int getTimeRequired() {
        if (this.tile == null)
            return 0;

        return NeutronCollectorTile.PRODUCTION_TICKS;
    }

    public int getProgressBarScaled(int pixels) {
        int i = Mth.clamp(this.getProgress(), 0, this.getTimeRequired());
        int j = this.getTimeRequired();
        return (int) (j != 0 && i != 0 ? (long) i * pixels / j : 0);
    }


}
