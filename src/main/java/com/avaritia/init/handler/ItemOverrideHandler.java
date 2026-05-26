package com.avaritia.init.handler;

/**
 * FIXME: MC 26.1.2 removed ItemProperties and ItemPropertyFunction APIs.
 * The item model property/override system was completely restructured;
 * ItemProperties.register() no longer exists.
 * Need to migrate to the new ItemModelResolver / ConditionalItemModelProperty system.
 * See https://docs.neoforged.net/ for migration guide.
 *
 * Original code registered model property overrides for:
 * - infinity_pickaxe, infinity_shovel, infinity_clock mode toggles
 * - infinity_bow, crystal_bow, blaze_bow pull/pulling/tracer states
 * - infinity_crossbow pull/charged states
 * - infinity_sword kill mode, infinity_shield blocking
 * - matter_cluster capacity indicator, infinity_umbrella mode
 */
public class ItemOverrideHandler {
}
