package com.avaritia;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

/**
 * 客户端渲染资源定位符。
 */
public class Res {
    public static final Identifier VOID = Avaritia.rl("textures/entity/void.png");
    public static final Identifier VOID_HALO = Avaritia.rl("textures/entity/void_halo.png");
    public static final Identifier BLADE_SLASH = Avaritia.rl("textures/entity/blade_slash.png");
    public static final Identifier HEAVEN_ARROW = Avaritia.rl("textures/entity/heaven_arrow.png");
    public static final Identifier TRIDENT_TEX = Avaritia.rl("textures/entity/infinity_trident_model.png");
    public static final Identifier ARC_TEX = Avaritia.rl("textures/effect/line_segment.png");
    public static final Identifier WING_TEX = Avaritia.rl("textures/models/armor/infinity_armor_wing.png");
    public static final Identifier DRAGON_FIREBALL = Identifier.fromNamespaceAndPath("minecraft", "textures/entity/enderdragon/dragon_fireball.png");

    // GUI - Crafting tables
    public static final Identifier END_CRAFT_TEX = Avaritia.rl("textures/gui/craft/end_crafting_table_gui.png");
    public static final Identifier NETHER_CRAFT_TEX = Avaritia.rl("textures/gui/craft/nether_crafting_table_gui.png");
    public static final Identifier SCULK_CRAFT_TEX = Avaritia.rl("textures/gui/craft/sculk_crafting_table_gui.png");
    public static final Identifier EXTREME_CRAFT_TEX = Avaritia.rl("textures/gui/craft/extreme_crafting_table_gui.png");

    // GUI - Machines
    public static final Identifier INFINITY_CLOCK_TIME_TEX = Avaritia.rl("textures/gui/machine/infinity_clock_time.png");
    public static final Identifier NEUTRON_COLLECTOR_TEX = Avaritia.rl("textures/gui/machine/neutron_collector.png");
    public static final Identifier NEUTRON_COMPRESSOR_TEX = Avaritia.rl("textures/gui/machine/neutron_compressor.png");
    public static final Identifier SIDE_CONFIG_TEX = Avaritia.rl("textures/gui/machine/side_config.png");
    public static final Identifier EXTREME_ANVIL_TEX = Avaritia.rl("textures/gui/machine/extreme_anvil_gui.png");
    public static final Identifier EXTREME_SMITHING_TEX = Avaritia.rl("textures/gui/machine/extreme_smithing_table_gui.png");

    // GUI - Chests
    public static final Identifier GENERIC_243_TEX = Avaritia.rl("textures/gui/chest/generic_243.png");
    public static final Identifier NEUTRON_RING_TEX = Avaritia.rl("textures/gui/chest/neutron_ring.png");
    public static final Identifier BLACK_HOLE_CHANNEL_PANEL = Avaritia.rl("textures/gui/chest/channel_panel.png");
    public static final Identifier BLACK_HOLE_CHANNEL_SELECT = Avaritia.rl("textures/gui/chest/channel_select.png");
    public static final Identifier INFINITY_CHEST_TEX = Avaritia.rl("textures/gui/chest/infinity_chest_gui.png");

    public static TextureAtlasSprite ARMOR_MASK;
    public static TextureAtlasSprite ARMOR_MASK_INV;
    public static TextureAtlasSprite ARMOR_WING_MASK;
}
