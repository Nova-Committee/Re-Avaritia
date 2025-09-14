package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.resources.UpgradeSmithingTemplateItem;
import committee.nova.mods.avaritia.common.menu.ExtremeSmithingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/19 13:14
 * @Description:
 */
public class ExtremeSmithingScreen extends ItemCombinerScreen<ExtremeSmithingMenu>  {
    private static final ResourceLocation SMITHING_LOCATION = Const.rl("textures/gui/extreme_smithing_table_gui.png");
    private final CyclingSlotBackground templateIcon = new CyclingSlotBackground(0);
    private final CyclingSlotBackground baseIcon = new CyclingSlotBackground(1);
    private final CyclingSlotBackground additionalIcon1 = new CyclingSlotBackground(2);
    private final CyclingSlotBackground additionalIcon2 = new CyclingSlotBackground(3);
    private final CyclingSlotBackground additionalIcon3 = new CyclingSlotBackground(4);
    private static final ResourceLocation EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE = new ResourceLocation("item/empty_slot_smithing_template_netherite_upgrade");
    private static final List<ResourceLocation> EMPTY_SLOT_SMITHING_TEMPLATES = List.of(EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE);
    public ExtremeSmithingScreen(ExtremeSmithingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, SMITHING_LOCATION);
        this.titleLabelX = 74;
        this.titleLabelY = 6;
    }

    private boolean hasRecipeError() {
        return this.menu.getSlot(0).hasItem() && this.menu.getSlot(1).hasItem()
                && this.menu.getSlot(2).hasItem() && this.menu.getSlot(3).hasItem()
                && this.menu.getSlot(4).hasItem() && !this.menu.getSlot(this.menu.getResultSlot()).hasItem();
    }

    @Override
    protected void renderErrorIcon(@NotNull GuiGraphics pGuiGraphics, int pX, int pY) {
        if (this.hasRecipeError()) {
            pGuiGraphics.blit(SMITHING_LOCATION, pX + 65, pY + 46, this.imageWidth, 0, 28, 21);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        Optional<SmithingTemplateItem> optional = this.getTemplateItem();
        this.templateIcon.tick(EMPTY_SLOT_SMITHING_TEMPLATES);
        //this.baseIcon.tick(optional.map(InfinitySmithingTemplateItem::getBaseSlotEmptyIcons).orElse(List.of()));
        //this.additionalIcon1.tick(optional.map(InfinitySmithingTemplateItem::getAdditionalSlotEmptyIcons).orElse(List.of()));
    }

    private Optional<SmithingTemplateItem> getTemplateItem() {
        ItemStack itemstack = this.menu.getSlot(0).getItem();
        if (!itemstack.isEmpty()) {
            Item item = itemstack.getItem();
            if (item instanceof UpgradeSmithingTemplateItem smithingTemplateItem) {
                return Optional.of(smithingTemplateItem);
            }
        }

        return Optional.empty();
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderOnboardingTooltips(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        super.renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY);
        this.templateIcon.render(this.menu, pGuiGraphics, pPartialTick, this.leftPos, this.topPos);
        this.baseIcon.render(this.menu, pGuiGraphics, pPartialTick, this.leftPos, this.topPos);
        this.additionalIcon1.render(this.menu, pGuiGraphics, pPartialTick, this.leftPos, this.topPos);
        this.additionalIcon2.render(this.menu, pGuiGraphics, pPartialTick, this.leftPos, this.topPos);
        this.additionalIcon3.render(this.menu, pGuiGraphics, pPartialTick, this.leftPos, this.topPos);
    }

    private static final Component MISSING_TEMPLATE_TOOLTIP = Component.translatable("container.upgrade.missing_template_tooltip");
    private static final Component ERROR_TOOLTIP = Component.translatable("container.upgrade.error_tooltip");

    private void renderOnboardingTooltips(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        Optional<Component> optional = Optional.empty();
        if (this.hasRecipeError() && this.isHovering(65, 46, 28, 21, pMouseX, pMouseY)) {
            optional = Optional.of(ERROR_TOOLTIP);
        }

        if (this.hoveredSlot != null) {
            ItemStack itemstack = this.menu.getSlot(0).getItem();
            if (itemstack.isEmpty()) {
                if (this.hoveredSlot.index == 0) {
                    optional = Optional.of(MISSING_TEMPLATE_TOOLTIP);
                }
            }
        }

        optional.ifPresent((p_280863_) -> {
            pGuiGraphics.renderTooltip(this.font, this.font.split(p_280863_, 115), pMouseX, pMouseY);
        });
    }
}
