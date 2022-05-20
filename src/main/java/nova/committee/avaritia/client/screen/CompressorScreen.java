package nova.committee.avaritia.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.api.client.screen.BaseContainerScreen;
import nova.committee.avaritia.common.menu.CompressorMenu;
import nova.committee.avaritia.common.tile.CompressorTileEntity;
import nova.committee.avaritia.init.ModTooltips;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 18:16
 * Version: 1.0
 */
public class CompressorScreen extends BaseContainerScreen<CompressorMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Static.MOD_ID, "textures/gui/compressor.png");
    private CompressorTileEntity tile;

    public CompressorScreen(CompressorMenu container, Inventory inventory, Component title) {
        super(container, inventory, title, BACKGROUND, 175, 166, 255, 255);
    }

    @Override
    public void init() {
        super.init();

        int x = this.getGuiLeft();
        int y = this.getGuiTop();
        var pos = this.getMenu().getPos();

        this.tile = this.getTileEntity();
    }

    @Override
    public void render(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        super.render(matrix, mouseX, mouseY, partialTicks);


        if (mouseX > x + 63 && mouseX < x + 79 && mouseY > y + 35 && mouseY < y + 51) {
            List<Component> tooltip = new ArrayList<>();

            if (this.getMaterialCount() < 1) {
                tooltip.add(ModTooltips.EMPTY.color(ChatFormatting.WHITE).build());
            } else {
                if (this.hasMaterialStack()) {
                    tooltip.add(this.getMaterialStackDisplayName());
                }

                var text = new TextComponent(number(this.getMaterialCount()) + " / " + number(this.getMaterialsRequired()));

                tooltip.add(text);
            }

            this.renderComponentTooltip(matrix, tooltip, mouseX, mouseY);
        }

//        if (mouseX > x + 89 && mouseX < x + 110 && mouseY > y + 35 && mouseY < y + 51) {
//            List<Component> tooltip = new ArrayList<>();
//
//            if (this.getProgress() < 1) {
//                tooltip.add(ModTooltips.PROGRESS_EMPTY.color(ChatFormatting.WHITE).build());
//            } else {
//
//                var text = new TextComponent(number(this.getProgress()) + " / " + number(this.getTimeRequired()));
//
//                tooltip.add(text);
//            }
//
//            this.renderComponentTooltip(matrix, tooltip, mouseX, mouseY);
//        }

//        if (mouseX > x + 68 && mouseX < x + 79 && mouseY > y + 28 && mouseY < y + 39) {
//            if (this.isEjecting()) {
//                this.renderTooltip(matrix, ModTooltips.EJECTING.color(ChatFormatting.WHITE).build(), mouseX, mouseY);
//            } else {
//                this.renderTooltip(matrix, ModTooltips.EJECT.color(ChatFormatting.WHITE).build(), mouseX, mouseY);
//            }
//        }

    }

    @Override
    protected void renderLabels(@NotNull PoseStack stack, int mouseX, int mouseY) {
        var title = this.getTitle().getString();

        this.font.draw(stack, title, (float) (this.imageWidth / 2 - this.font.width(title) / 2), 6.0F, 4210752);
        this.font.draw(stack, this.playerInventoryTitle, 8.0F, this.imageHeight - 94.0F, 4210752);
    }

    @Override
    protected void renderBg(@NotNull PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        this.renderDefaultBg(stack, partialTicks, mouseX, mouseY);

        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        if (this.hasRecipe()) {
            if (this.getMaterialCount() > 0 && this.getMaterialsRequired() > 0) {
                int i2 = this.getMaterialBarScaled(16);
                this.blit(stack, x + 63, y + 35, 176, 18, i2 + 1, 16);
            }

            if (this.getProgress() > 0) {
                int i2 = this.getProgressBarScaled(22);
                this.blit(stack, x + 89, y + 35, 176, 0, i2 + 1, 16);
            }

        }


    }

    private Component getMaterialStackDisplayName() {
        var level = this.getMinecraft().level;

        if (level != null) {
            var container = this.getMenu();
            var tile = level.getBlockEntity(container.getPos());

            if (tile instanceof CompressorTileEntity compressor) {
                var materialStack = compressor.getMaterialStack();

                return materialStack.getHoverName();
            }
        }

        return new TextComponent("");
    }

    private CompressorTileEntity getTileEntity() {
        var level = this.getMinecraft().level;

        if (level != null) {
            var tile = level.getBlockEntity(this.getMenu().getPos());

            if (tile instanceof CompressorTileEntity compressor)
                return compressor;
        }

        return null;
    }

    public boolean isEjecting() {
        if (this.tile == null)
            return false;

        return this.tile.isEjecting();
    }


    public boolean hasRecipe() {
        if (this.tile == null)
            return false;

        return this.tile.hasRecipe();
    }

    public boolean hasMaterialStack() {
        if (this.tile == null)
            return false;

        return this.tile.hasMaterialStack();
    }

    public int getProgress() {
        if (this.tile == null)
            return 0;

        return this.menu.getProgress();
    }

    public int getMaterialCount() {
        if (this.tile == null)
            return 0;

        return this.tile.getMaterialCount();
    }

    public int getMaterialsRequired() {
        if (this.tile == null)
            return 0;

        return this.tile.getMaterialsRequired();
    }

    public int getTimeRequired() {
        if (this.tile == null)
            return 0;

        return this.tile.getTimeRequired();
    }

    public int getMaterialBarScaled(int pixels) {
        int i = Mth.clamp(this.getMaterialCount(), 0, this.getMaterialsRequired());
        int j = this.getMaterialsRequired();
        return j != 0 && i != 0 ? i * pixels / j : 0;
    }

    public int getProgressBarScaled(int pixels) {
        int i = this.getProgress();
        int j = this.getTimeRequired();
        return j != 0 && i != 0 ? i * pixels / j : 0;
    }

}
