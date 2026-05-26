package com.avaritia.common.item.resources;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/20 00:42
 * @Description:
 */
public class UpgradeSmithingTemplateItem extends SmithingTemplateItem {
    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
    private static final Identifier EMPTY_SLOT_HOE = Identifier.tryParse("item/empty_slot_hoe");
    private static final Identifier EMPTY_SLOT_AXE = Identifier.tryParse("item/empty_slot_axe");
    private static final Identifier EMPTY_SLOT_SWORD = Identifier.tryParse("item/empty_slot_sword");
    private static final Identifier EMPTY_SLOT_SHOVEL = Identifier.tryParse("item/empty_slot_shovel");
    private static final Identifier EMPTY_SLOT_PICKAXE = Identifier.tryParse("item/empty_slot_pickaxe");
    private static final Identifier EMPTY_SLOT_BLOCK = Identifier.tryParse("item/empty_slot_block");

    public UpgradeSmithingTemplateItem() {
        super(Component.translatable("item.avaritia.upgrade_smithing_template"),
                Component.translatable("item.avaritia.upgrade_smithing_template")
                        .withStyle(DESCRIPTION_FORMAT),
                Component.translatable("item.avaritia.upgrade_smithing_template")
                        .withStyle(TITLE_FORMAT),
                Component.translatable("item.avaritia.upgrade_smithing_template"),
                Component.translatable("item.avaritia.upgrade_smithing_template"),
                List.of(EMPTY_SLOT_SWORD, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_AXE, EMPTY_SLOT_HOE, EMPTY_SLOT_SHOVEL),
                List.of(EMPTY_SLOT_BLOCK));
    }
}