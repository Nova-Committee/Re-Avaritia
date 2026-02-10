package committee.nova.mods.avaritia.client.screen;

import java.text.DecimalFormat;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;

import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.common.container.slot.InfinitySlot;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InfinityChestScreen extends AbstractContainerScreen<InfinityChestMenu> {

    public static final ResourceLocation BACKGROUND_LOCATION = Res.INFINITY_CHEST_TEX;

    public InfinityChestScreen(InfinityChestMenu menu, Inventory inventory, Component title) {
        super(menu,inventory,title);
        this.imageWidth = 500;
        this.imageHeight = 275;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND_LOCATION, this.getGuiLeft(), this.getGuiTop(), 0.0F, 0.0F, this.imageWidth, this.imageHeight, 500, 275);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 170, this.imageHeight - 94, 4210752, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack itemStack = this.hoveredSlot.getItem();
            List<Component> components = this.getTooltipFromContainerItem(itemStack);
            if (this.hoveredSlot instanceof InfinitySlot) {
                components.add(Component.translatable("container.infinity_chest", itemStack.getCount(), this.menu.getSlotMaxStack(this.hoveredSlot)));
            }
            guiGraphics.renderTooltip(this.font, components, itemStack.getTooltipImage(), itemStack, mouseX, mouseY);
        }
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        int i = slot.x;
        int j = slot.y;
        ItemStack itemStack = slot.getItem();
        boolean flag = false;
        boolean flag1 = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack itemStack1 = this.menu.getCarried();
        if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemStack.isEmpty()) {
            itemStack = itemStack.copyWithCount(itemStack.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !itemStack1.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }

            if (InfinityChestMenu.canItemQuickReplace(this.menu.getTile().chest, slot, itemStack1, true, this.menu.getSlotMaxStack(slot)) && this.menu.canDragTo(slot)) {
                flag = true;
                int k = Math.min(this.menu.getTile().chest.getMaxStackSize(itemStack1), this.menu.getSlotMaxStack(slot));
                int l = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
                int i1 = InfinityChestMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemStack1) + l;
                if (i1 > k) {
                    i1 = k;
                }

                itemStack = itemStack1.copyWithCount(i1);
            } else {
                this.quickCraftSlots.remove(slot);
                this.recalculateQuickCraftRemaining();
            }
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
        if (itemStack.isEmpty() && slot.isActive()) {
            Pair<ResourceLocation, ResourceLocation> pair = slot.getNoItemIcon();
            if (pair != null) {
                TextureAtlasSprite textureatlassprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                guiGraphics.blit(i, j, 0, 16, 16, textureatlassprite);
                flag1 = true;
            }
        }

        if (!flag1) {
            if (flag) {
                guiGraphics.fill(i, j, i + 16, j + 16, -2130706433);
            }

            int j1 = slot.x + slot.y * this.imageWidth;
            if (slot.isFake()) {
                guiGraphics.renderFakeItem(itemStack, i, j, j1);
            } else {
                guiGraphics.renderItem(itemStack, i, j, j1);
            }

            String text = null;
            int count = itemStack.getCount();

            if (count >= 1000) {
                text = new DecimalFormat("#").format(count / 1000) + "K";
            }
            if (count >= 10000) {
                text = new DecimalFormat("#").format(count / 10000) + "W";
            }
            if (count >= 1000000) {
                text = new DecimalFormat("#").format(count / 1000000) + "M";
            }
            if (count >= 1000000000) {
                text = new DecimalFormat("#").format(count / 1000000000) + "G";
            }

            guiGraphics.renderItemDecorations(this.font, itemStack, i, j, text);
        }

        guiGraphics.pose().popPose();
    }
@Override
    public void recalculateQuickCraftRemaining() {
        ItemStack itemStack = this.menu.getCarried();
        if (!itemStack.isEmpty() && this.isQuickCrafting) {
            if (this.quickCraftingType == 2) {
                this.quickCraftingRemainder = itemStack.getMaxStackSize();
            } else {
                this.quickCraftingRemainder = itemStack.getCount();

                for (Slot slot : this.quickCraftSlots) {
                    ItemStack itemStack1 = slot.getItem();
                    int i = itemStack1.isEmpty() ? 0 : itemStack1.getCount();
                    int j = Math.min(this.menu.getTile().chest.getMaxStackSize(itemStack), this.menu.getSlotMaxStack(slot));
                    int k = Math.min(InfinityChestMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemStack) + i, j);
                    this.quickCraftingRemainder -= k - i;
                }
            }
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        Slot slot = this.findSlot(mouseX, mouseY);
        ItemStack itemStack = this.menu.getCarried();
        if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
            if (button == 0 || button == 1) {
                if (this.draggingItem.isEmpty()) {
                    if (slot != this.clickedSlot && !this.clickedSlot.getItem().isEmpty()) {
                        this.draggingItem = this.clickedSlot.getItem().copy();
                    }
                } else if (this.draggingItem.getCount() > 1 && slot != null && InfinityChestMenu.canItemQuickReplace(this.menu.getTile().chest, slot, this.draggingItem, false, this.menu.getSlotMaxStack(slot))) {
                    long i = net.minecraft.Util.getMillis();
                    if (this.quickdropSlot == slot) {
                        if (i - this.quickdropTime > 500L) {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                            this.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                            this.quickdropTime = i + 750L;
                            this.draggingItem.shrink(1);
                        }
                    } else {
                        this.quickdropSlot = slot;
                        this.quickdropTime = i;
                    }
                }
            }
        } else if (this.isQuickCrafting && slot != null && !itemStack.isEmpty() && (itemStack.getCount() > this.quickCraftSlots.size() || this.quickCraftingType == 2) && InfinityChestMenu.canItemQuickReplace(this.menu.getTile().chest, slot, itemStack, true, this.menu.getSlotMaxStack(slot)) && slot.mayPlace(itemStack) && this.menu.canDragTo(slot)) {
            this.quickCraftSlots.add(slot);
            this.recalculateQuickCraftRemaining();
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isDragging()) {
            this.setDragging(false);
            if (this.getFocused() != null) {
                return this.getFocused().mouseReleased(mouseX, mouseY, button);
            }
        }

        this.getChildAt(mouseX, mouseY).filter(listener -> listener.mouseReleased(mouseX, mouseY, button));

        Slot slot = this.findSlot(mouseX, mouseY);
        int i = this.leftPos;
        int j = this.topPos;
        boolean flag = this.hasClickedOutside(mouseX, mouseY, i, j, button);
        if (slot != null) {
            flag = false; // Forge, prevent dropping of items through slots outside of GUI boundaries
        }
        InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(button);
        int k = -1;
        if (slot != null) {
            k = slot.index;
        }

        if (flag) {
            k = -999;
        }

        if (this.doubleclick && slot != null && button == 0 && this.menu.canTakeItemForPickAll(ItemStack.EMPTY, slot)) {
            if (hasShiftDown()) {
                if (!this.lastQuickMoved.isEmpty()) {
                    for (Slot slot2 : this.menu.slots) {
                        if (slot2 != null && slot2.mayPickup(this.minecraft.player) && slot2.hasItem() && slot2.isSameInventory(slot) && InfinityChestMenu.canItemQuickReplace(this.menu.getTile().chest, slot2, this.lastQuickMoved, true, this.menu.getSlotMaxStack(slot2))) {
                            this.slotClicked(slot2, slot2.index, button, ClickType.QUICK_MOVE);
                        }
                    }
                }
            } else {
                this.slotClicked(slot, k, button, ClickType.PICKUP_ALL);
            }

            this.doubleclick = false;
            this.lastClickTime = 0L;
        } else {
            if (this.isQuickCrafting && this.quickCraftingButton != button) {
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                this.skipNextRelease = true;
                return true;
            }

            if (this.skipNextRelease) {
                this.skipNextRelease = false;
                return true;
            }

            if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
                if (button == 0 || button == 1) {
                    if (this.draggingItem.isEmpty() && slot != this.clickedSlot) {
                        this.draggingItem = this.clickedSlot.getItem();
                    }

                    boolean flag2 = InfinityChestMenu.canItemQuickReplace(this.menu.getTile().chest, slot, this.draggingItem, false, this.menu.getSlotMaxStack(slot));
                    if (k != -1 && !this.draggingItem.isEmpty() && flag2) {
                        this.slotClicked(this.clickedSlot, this.clickedSlot.index, button, ClickType.PICKUP);
                        this.slotClicked(slot, k, 0, ClickType.PICKUP);
                        if (this.menu.getCarried().isEmpty()) {
                            this.snapbackItem = ItemStack.EMPTY;
                        } else {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, button, ClickType.PICKUP);
                            this.snapbackStartX = Mth.floor(mouseX - (double) i);
                            this.snapbackStartY = Mth.floor(mouseY - (double) j);
                            this.snapbackEnd = this.clickedSlot;
                            this.snapbackItem = this.draggingItem;
                            this.snapbackTime = Util.getMillis();
                        }
                    } else if (!this.draggingItem.isEmpty()) {
                        this.snapbackStartX = Mth.floor(mouseX - (double) i);
                        this.snapbackStartY = Mth.floor(mouseY - (double) j);
                        this.snapbackEnd = this.clickedSlot;
                        this.snapbackItem = this.draggingItem;
                        this.snapbackTime = Util.getMillis();
                    }

                    this.clearDraggingState();
                }
            } else if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty()) {
                this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(0, this.quickCraftingType), ClickType.QUICK_CRAFT);

                for (Slot slot1 : this.quickCraftSlots) {
                    this.slotClicked(slot1, slot1.index, AbstractContainerMenu.getQuickcraftMask(1, this.quickCraftingType), ClickType.QUICK_CRAFT);
                }

                this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(2, this.quickCraftingType), ClickType.QUICK_CRAFT);
            } else if (!this.menu.getCarried().isEmpty()) {
                if (this.minecraft.options.keyPickItem.isActiveAndMatches(mouseKey)) {
                    this.slotClicked(slot, k, button, ClickType.CLONE);
                } else {
                    boolean flag1 = k != -999 && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
                    if (flag1) {
                        this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                    }

                    this.slotClicked(slot, k, button, flag1 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
                }
            }
        }

        if (this.menu.getCarried().isEmpty()) {
            this.lastClickTime = 0L;
        }

        this.isQuickCrafting = false;
        return true;
    }
}