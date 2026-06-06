package com.avaritia.mixin;

/**
 * The old 1.21 item render injection is intentionally inactive.
 * Avaritia special item layers now go through RegisterItemModelsEvent and
 * AvaritiaItemModels, which append 26.x ItemStackRenderState special geometry.
 */
