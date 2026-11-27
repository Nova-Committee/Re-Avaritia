package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.api.client.screen.component.PortableLayout;
import committee.nova.mods.avaritia.api.client.screen.component.PortableUi;
import committee.nova.mods.avaritia.api.client.screen.component.UiInspector;
import committee.nova.mods.avaritia.init.config.ModConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/*
每次添加配置的时候都需要在initConfigEntries添加一个配置项,然后在resetToDefaults中添加一个默认值
关于默认值的自动化我没太搞懂...
 */
public class AvaritiaConfigScreen extends Screen {
    private final Screen parent;
    private final List<ConfigEntry<?>> configEntries = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 40;
    private static final int MARGIN = 20;
    private static final int START_Y = 50;
    private Button resetButton;
    private Button backButton;

    private boolean isDraggingScrollbar = false;
    private ScreenRectangle panel;
    private ScreenRectangle viewport;
    private ScreenRectangle footer;
    private ScreenRectangle scrollbar;
    private ScreenRectangle scrollbarHandle;
    private int maxScrollOffset;
    private double scrollVelocity = 0.0;
    private long lastScrollTime = 0;
    private static final double FRICTION = 0.92;
    private static final double MIN_VELOCITY = 0.1;
    private static final double SCROLL_FACTOR = 10.0;

    public AvaritiaConfigScreen(Screen parent) {
        super(Component.translatable("title.avaritia.config.title"));
        this.parent = parent;
        initConfigEntries();
    }

    private void initConfigEntries() {
        addCategoryHeader("config.avaritia.category.tools");

        addBooleanEntry("is_merge_matter_cluster", ModConfig.isMergeMatterCluster,
                Component.translatable("config.avaritia.is_merge_matter_cluster.tooltip"),
                ModConfig.isMergeMatterCluster::set, ModConfig.isMergeMatterCluster);

        addIntEntry("sword_range_damage", ModConfig.swordRangeDamage, 100, 100000,
                Component.translatable("config.avaritia.sword_range_damage.tooltip"),
                ModConfig.swordRangeDamage::set, ModConfig.swordRangeDamage);

        addIntEntry("sword_attack_range", ModConfig.swordAttackRange, 8, 64,
                Component.translatable("config.avaritia.sword_attack_range.tooltip"),
                ModConfig.swordAttackRange::set, ModConfig.swordAttackRange);

        addBooleanEntry("is_sword_attack_item_entity", ModConfig.isSwordAttackItemEntity,
                Component.translatable("config.avaritia.is_sword_attack_item_entity.tooltip"),
                ModConfig.isSwordAttackItemEntity::set, ModConfig.isSwordAttackItemEntity);

        addBooleanEntry("is_sword_attack_projectile", ModConfig.isSwordAttackProjectile,
                Component.translatable("config.avaritia.is_sword_attack_projectile.tooltip"),
                ModConfig.isSwordAttackProjectile::set, ModConfig.isSwordAttackProjectile);

        addBooleanEntry("is_sword_attack_lightning", ModConfig.isSwordAttackLightning,
                Component.translatable("config.avaritia.is_sword_attack_lightning.tooltip"),
                ModConfig.isSwordAttackLightning::set, ModConfig.isSwordAttackLightning);

        addBooleanEntry("is_sword_attack_endless", ModConfig.isSwordAttackEndless,
                Component.translatable("config.avaritia.is_sword_attack_endless.tooltip"),
                ModConfig.isSwordAttackEndless::set, ModConfig.isSwordAttackEndless);

        addIntEntry("sub_arrow_damage", ModConfig.subArrowDamage, 100, 100000,
                Component.translatable("config.avaritia.sub_arrow_damage.tooltip"),
                ModConfig.subArrowDamage::set, ModConfig.subArrowDamage);

        addIntEntry("axe_chain_count", ModConfig.axeChainCount, 16, 128,
                Component.translatable("config.avaritia.axe_chain_count.tooltip"),
                ModConfig.axeChainCount::set, ModConfig.axeChainCount);

        addIntEntry("pickaxe_break_range", ModConfig.pickAxeBreakRange, 2, 32,
                Component.translatable("config.avaritia.pickaxe_break_range.tooltip"),
                ModConfig.pickAxeBreakRange::set, ModConfig.pickAxeBreakRange);

        addIntEntry("shovel_break_range", ModConfig.shovelBreakRange, 2, 32,
                Component.translatable("config.avaritia.shovel_break_range.tooltip"),
                ModConfig.shovelBreakRange::set, ModConfig.shovelBreakRange);

        addIntEntry("singularity_time_required", ModConfig.singularityTimeRequired, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.singularity_time_required.tooltip"),
                ModConfig.singularityTimeRequired::set, ModConfig.singularityTimeRequired);

        addDoubleEntry("growth_soul_farmland", ModConfig.growthSoulFarmland, 0.0, 1.0,
                Component.translatable("config.avaritia.growth_soul_farmland.tooltip"),
                ModConfig.growthSoulFarmland::set, ModConfig.growthSoulFarmland);

        addIntEntry("blade_slash_damage", ModConfig.bladeSlashDamage, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.blade_slash_damage.tooltip"),
                ModConfig.bladeSlashDamage::set, ModConfig.bladeSlashDamage);

        addIntEntry("blade_slash_radius", ModConfig.bladeSlashRadius, 5, 100,
                Component.translatable("config.avaritia.blade_slash_radius.tooltip"),
                ModConfig.bladeSlashRadius::set, ModConfig.bladeSlashRadius);


        addCategoryHeader("config.avaritia.category.emc");

        addBooleanEntry("enable_projecte_singularity_count_boost", ModConfig.enableProjectESingularityCountBoost,
                Component.translatable("config.avaritia.enable_projecte_singularity_count_boost.tooltip"),
                ModConfig.enableProjectESingularityCountBoost::set, ModConfig.enableProjectESingularityCountBoost);

        addIntEntry("neutron_pile_emc", ModConfig.neutronPileEmc, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.neutron_pile_emc.tooltip"),
                ModConfig.neutronPileEmc::set, ModConfig.neutronPileEmc);

        addIntEntry("blaze_cube_emc", ModConfig.blazeCubeEmc, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.blaze_cube_emc.tooltip"),
                ModConfig.blazeCubeEmc::set, ModConfig.blazeCubeEmc);

        addIntEntry("vanilla_totem_emc", ModConfig.vanillaTotemEmc, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.vanilla_totem_emc.tooltip"),
                ModConfig.vanillaTotemEmc::set, ModConfig.vanillaTotemEmc);

        addCategoryHeader("config.avaritia.category.channel");

        addIntEntry("max_channels_pre_player", ModConfig.MAX_CHANNELS_PRE_PLAYER, 4, 64,
                Component.translatable("config.avaritia.max_channels_pre_player.tooltip"),
                val -> ModConfig.MAX_CHANNELS_PRE_PLAYER.set(val), ModConfig.MAX_CHANNELS_PRE_PLAYER);

        addIntEntry("max_public_channels", ModConfig.MAX_PUBLIC_CHANNELS, 32, 1024,
                Component.translatable("config.avaritia.max_public_channels.tooltip"),
                val -> ModConfig.MAX_PUBLIC_CHANNELS.set(val), ModConfig.MAX_PUBLIC_CHANNELS);

        addIntEntry("channel_fast_update_rate", ModConfig.CHANNEL_FAST_UPDATE_RATE, 1, 40,
                Component.translatable("config.avaritia.channel_fast_update_rate.tooltip"),
                val -> ModConfig.CHANNEL_FAST_UPDATE_RATE.set(val), ModConfig.CHANNEL_FAST_UPDATE_RATE);

        addIntEntry("channel_full_update_rate", ModConfig.CHANNEL_FULL_UPDATE_RATE, 20, 1200,
                Component.translatable("config.avaritia.channel_full_update_rate.tooltip"),
                val -> ModConfig.CHANNEL_FULL_UPDATE_RATE.set(val), ModConfig.CHANNEL_FULL_UPDATE_RATE);

        addCategoryHeader("config.avaritia.category.misc");

        addBooleanEntry("use_advance_tooltips", ModConfig.useAdvanceTooltips,
                Component.translatable("config.avaritia.use_advance_tooltips.tooltip"),
                ModConfig.useAdvanceTooltips::set, ModConfig.useAdvanceTooltips);

        addDoubleEntry("endless_item_entity_speed", ModConfig.immortalItemEntitySpeed, 1.0, 50.0,
                Component.translatable("config.avaritia.endless_item_entity_speed.tooltip"),
                ModConfig.immortalItemEntitySpeed::set, ModConfig.immortalItemEntitySpeed);

        addDoubleEntry("endless_item_entity_range", ModConfig.immortalItemEntityRange, 1.0, 10000.0,
                Component.translatable("config.avaritia.endless_item_entity_range.tooltip"),
                ModConfig.immortalItemEntityRange::set, ModConfig.immortalItemEntityRange);

        addDoubleEntry("infinity_elytra_flying_speed", ModConfig.infinityElytraFlyingSpeed, 1.0, 10.0,
                Component.translatable("config.avaritia.infinity_elytra_flying_speed.tooltip"),
                ModConfig.infinityElytraFlyingSpeed::set, ModConfig.infinityElytraFlyingSpeed);

        addDoubleEntry("infinity_elytra_flying_damage_range", ModConfig.infinityElytraFlyingRangeDamage, 0.0, 10000.0,
                Component.translatable("config.avaritia.infinity_elytra_flying_damage_range.tooltip"),
                ModConfig.infinityElytraFlyingRangeDamage::set,ModConfig.infinityElytraFlyingRangeDamage);

        addBooleanEntry("infinity_helmet_night_vision", ModConfig.InfinityArmorNightVision,
                Component.translatable("config.avaritia.infinity_helmet_night_vision.tooltip"),
                ModConfig.InfinityArmorNightVision::set, ModConfig.InfinityArmorNightVision);

        addDoubleEntry("boot_speed_base", ModConfig.bootSpeedBase, 0.01, 1.0,
                Component.translatable("config.avaritia.boot_speed_base.tooltip"),
                ModConfig.bootSpeedBase::set, ModConfig.bootSpeedBase);

        addDoubleEntry("boot_speed_flying_multiplier", ModConfig.bootSpeedFlyingMultiplier, 0.1, 5.0,
                Component.translatable("config.avaritia.boot_speed_flying_multiplier.tooltip"),
                ModConfig.bootSpeedFlyingMultiplier::set, ModConfig.bootSpeedFlyingMultiplier);

        addDoubleEntry("boot_speed_swimming_multiplier", ModConfig.bootSpeedSwimmingMultiplier, 0.1, 5.0,
                Component.translatable("config.avaritia.boot_speed_swimming_multiplier.tooltip"),
                ModConfig.bootSpeedSwimmingMultiplier::set, ModConfig.bootSpeedSwimmingMultiplier);

        addDoubleEntry("boot_speed_sneaking_multiplier", ModConfig.bootSpeedSneakingMultiplier, 0.01, 1.0,
                Component.translatable("config.avaritia.boot_speed_sneaking_multiplier.tooltip"),
                ModConfig.bootSpeedSneakingMultiplier::set, ModConfig.bootSpeedSneakingMultiplier);

        addDoubleEntry("boot_speed_backward_multiplier", ModConfig.bootSpeedBackwardMultiplier, 0.01, 1.0,
                Component.translatable("config.avaritia.boot_speed_backward_multiplier.tooltip"),
                ModConfig.bootSpeedBackwardMultiplier::set, ModConfig.bootSpeedBackwardMultiplier);

        addDoubleEntry("boot_speed_strafing_multiplier", ModConfig.bootSpeedStrafingMultiplier, 0.01, 1.0,
                Component.translatable("config.avaritia.boot_speed_strafing_multiplier.tooltip"),
                ModConfig.bootSpeedStrafingMultiplier::set, ModConfig.bootSpeedStrafingMultiplier);

        addDoubleEntry("boot_speed_sprinting_multiplier", ModConfig.bootSpeedSprintingMultiplier, 0.01, 1.0,
                Component.translatable("config.avaritia.boot_speed_sprinting_multiplier.tooltip"),
                ModConfig.bootSpeedSprintingMultiplier::set, ModConfig.bootSpeedSprintingMultiplier);
    }

    private void resetToDefaults() {
        // Tools 配置项
        ModConfig.isMergeMatterCluster.set(ModConfig.isMergeMatterCluster.getDefault());
        ModConfig.swordRangeDamage.set(ModConfig.swordRangeDamage.getDefault());
        ModConfig.swordAttackRange.set(ModConfig.swordAttackRange.getDefault());
        ModConfig.isSwordAttackItemEntity.set(ModConfig.isSwordAttackItemEntity.getDefault());
        ModConfig.isSwordAttackProjectile.set(ModConfig.isSwordAttackProjectile.getDefault());
        ModConfig.isSwordAttackLightning.set(ModConfig.isSwordAttackLightning.getDefault());
        ModConfig.isSwordAttackEndless.set(ModConfig.isSwordAttackEndless.getDefault());
        ModConfig.subArrowDamage.set(ModConfig.subArrowDamage.getDefault());
        ModConfig.pickAxeBreakRange.set(ModConfig.pickAxeBreakRange.getDefault());
        ModConfig.shovelBreakRange.set(ModConfig.shovelBreakRange.getDefault());
        ModConfig.singularityTimeRequired.set(ModConfig.singularityTimeRequired.getDefault());
        ModConfig.growthSoulFarmland.set(ModConfig.growthSoulFarmland.getDefault());
        ModConfig.bladeSlashDamage.set(ModConfig.bladeSlashDamage.getDefault());
        ModConfig.bladeSlashRadius.set(ModConfig.bladeSlashRadius.getDefault());

        // EMC 配置项
        ModConfig.enableProjectESingularityCountBoost.set(ModConfig.enableProjectESingularityCountBoost.getDefault());
        ModConfig.neutronPileEmc.set(ModConfig.neutronPileEmc.getDefault());
        ModConfig.blazeCubeEmc.set(ModConfig.blazeCubeEmc.getDefault());
        ModConfig.vanillaTotemEmc.set(ModConfig.vanillaTotemEmc.getDefault());

        // Channel 配置项
        ModConfig.MAX_CHANNELS_PRE_PLAYER.set(ModConfig.MAX_CHANNELS_PRE_PLAYER.getDefault());
        ModConfig.MAX_PUBLIC_CHANNELS.set(ModConfig.MAX_PUBLIC_CHANNELS.getDefault());
        ModConfig.CHANNEL_FAST_UPDATE_RATE.set(ModConfig.CHANNEL_FAST_UPDATE_RATE.getDefault());
        ModConfig.CHANNEL_FULL_UPDATE_RATE.set(ModConfig.CHANNEL_FULL_UPDATE_RATE.getDefault());

        // Misc 配置项
        ModConfig.useAdvanceTooltips.set(ModConfig.useAdvanceTooltips.getDefault());
        ModConfig.immortalItemEntitySpeed.set(ModConfig.immortalItemEntitySpeed.getDefault());
        ModConfig.immortalItemEntityRange.set(ModConfig.immortalItemEntityRange.getDefault());
        ModConfig.infinityElytraFlyingSpeed.set(ModConfig.infinityElytraFlyingSpeed.getDefault());
        ModConfig.infinityElytraFlyingRangeDamage.set(ModConfig.infinityElytraFlyingRangeDamage.getDefault());
        ModConfig.InfinityArmorNightVision.set(ModConfig.InfinityArmorNightVision.getDefault());
        ModConfig.bootSpeedSwimmingMultiplier.set(ModConfig.bootSpeedSwimmingMultiplier.getDefault());
        ModConfig.bootSpeedBackwardMultiplier.set(ModConfig.bootSpeedBackwardMultiplier.getDefault());
        ModConfig.bootSpeedSprintingMultiplier.set(ModConfig.bootSpeedSprintingMultiplier.getDefault());
        ModConfig.bootSpeedSneakingMultiplier.set(ModConfig.bootSpeedSneakingMultiplier.getDefault());
        ModConfig.bootSpeedStrafingMultiplier.set(ModConfig.bootSpeedStrafingMultiplier.getDefault());
        ModConfig.bootSpeedBase.set(ModConfig.bootSpeedBase.getDefault());
        ModConfig.bootSpeedFlyingMultiplier.set(ModConfig.bootSpeedFlyingMultiplier.getDefault());
    }


    private void addBooleanEntry(String titleKey, ForgeConfigSpec.BooleanValue configValue,
                                 Component description, Consumer<Boolean> onValueChange, Supplier<Boolean> valueSupplier) {
        configEntries.add(new BooleanConfigEntry(
                Component.translatable("config.avaritia." + titleKey),
                description,
                configValue.get(),
                onValueChange,
                valueSupplier
        ));
    }

    private void addIntEntry(String titleKey, ForgeConfigSpec.IntValue configValue, int min, int max,
                             Component description, Consumer<Integer> onValueChange, Supplier<Integer> valueSupplier) {
        configEntries.add(new IntConfigEntry(
                Component.translatable("config.avaritia." + titleKey),
                description,
                configValue.get(),
                min,
                max,
                onValueChange,
                valueSupplier
        ));
    }

    private void addDoubleEntry(String titleKey, ForgeConfigSpec.DoubleValue configValue, double min, double max,
                                Component description, Consumer<Double> onValueChange, Supplier<Double> valueSupplier) {
        configEntries.add(new DoubleConfigEntry(
                Component.translatable("config.avaritia." + titleKey),
                description,
                configValue.get(),
                min,
                max,
                onValueChange,
                valueSupplier
        ));
    }

    private void addLongEntry(String titleKey, ForgeConfigSpec.LongValue configValue, long min, long max,
                              Component description, Consumer<Long> onValueChange, Supplier<Long> valueSupplier) {
        configEntries.add(new LongConfigEntry(
                Component.translatable("config.avaritia." + titleKey),
                description,
                configValue.get(),
                min,
                max,
                onValueChange,
                valueSupplier
        ));
    }

    private void addCategoryHeader(String translationKey) {
        configEntries.add(new CategoryHeaderEntry(
                Component.translatable(translationKey)
        ));
    }

    @Override
    protected void init() {
        super.init();
        updateLayout();
        resetButton = addRenderableWidget(UiInspector.name(PortableUi.button(0, 0, 100, 20,
                Component.translatable("controls.reset"), btn -> {
                    resetToDefaults();
                    updateWidgetValues();
                }), "config.reset"));
        backButton = addRenderableWidget(UiInspector.name(PortableUi.button(0, 0, 100, 20,
                Component.translatable("gui.back"), btn -> onClose()), "config.back"));
        for (int i = 0; i < configEntries.size(); i++) {
            ConfigEntry<?> entry = configEntries.get(i);
            if (entry.control() == null) {
                entry.initWidgets(this, viewport.left(), viewport.top() + i * ENTRY_HEIGHT - scrollOffset, viewport.width());
            } else {
                addRenderableWidget(entry.control());
            }
            if (UiInspector.enabled() && entry.control() != null && entry.title.getContents() instanceof TranslatableContents translated) {
                UiInspector.name(entry.control(), translated.getKey());
            }
        }
        updateFooter();
        updateControlPositions();
    }

    @Override
    protected void repositionElements() {
        updateLayout();
    }

    private void updateLayout() {
        panel = PortableLayout.centered(width, height, width, height, 8);
        viewport = new ScreenRectangle(MARGIN, START_Y, Math.max(0, width - 2 * MARGIN), Math.max(0, height - START_Y - 40));
        footer = new ScreenRectangle(MARGIN, height - 30, Math.max(0, width - 2 * MARGIN), 20);
        scrollbar = new ScreenRectangle(viewport.right() + 4, viewport.top(), 4, viewport.height());
        maxScrollOffset = Math.max(0, configEntries.size() * ENTRY_HEIGHT - viewport.height());
        scrollOffset = Math.max(0, Math.min(maxScrollOffset, scrollOffset));
        isDraggingScrollbar = false;
        for (ConfigEntry<?> entry : configEntries) {
            entry.descriptionLines = font.split(entry.description, Math.max(1, viewport.width() - 120));
        }
        updateFooter();
        updateControlPositions();
    }

    private void updateFooter() {
        if (resetButton != null && backButton != null) {
            int buttonWidth = Math.min(100, Math.max(0, (footer.width() - 4) / 2));
            int center = footer.left() + footer.width() / 2;
            resetButton.setWidth(buttonWidth);
            resetButton.setHeight(footer.height());
            resetButton.setX(center - buttonWidth - 2);
            resetButton.setY(footer.top());
            backButton.setWidth(buttonWidth);
            backButton.setHeight(footer.height());
            backButton.setX(center + 2);
            backButton.setY(footer.top());
        }
    }

    private void updateControlPositions() {
        if (viewport == null) {
            return;
        }
        for (int i = 0; i < configEntries.size(); i++) {
            AbstractWidget control = configEntries.get(i).control();
            if (control == null) {
                continue;
            }
            int x = viewport.right() - control.getWidth();
            int y = viewport.top() + i * ENTRY_HEIGHT - scrollOffset + 10;
            boolean visible = viewport.width() > 0 && viewport.height() > 0
                    && x >= viewport.left() && y >= viewport.top() && y + control.getHeight() <= viewport.bottom();
            if (!visible && control.isFocused()) {
                control.setFocused(false);
                if (getFocused() == control) {
                    setFocused(null);
                }
            }
            control.setX(x);
            control.setY(y);
            control.visible = visible;
        }
        int totalHeight = configEntries.size() * ENTRY_HEIGHT;
        int handleHeight = totalHeight == 0 ? viewport.height()
                : Math.min(viewport.height(), Math.max(20, viewport.height() * viewport.height() / Math.max(1, totalHeight)));
        int handleOffset = maxScrollOffset == 0 ? 0
                : (int) ((double) scrollOffset / maxScrollOffset * (viewport.height() - handleHeight));
        scrollbarHandle = new ScreenRectangle(scrollbar.left(), scrollbar.top() + handleOffset, scrollbar.width(), handleHeight);
    }

    private void setScrollOffset(int offset) {
        scrollOffset = Math.max(0, Math.min(maxScrollOffset, offset));
        updateControlPositions();
    }

    private void updateWidgetValues() {
        for (ConfigEntry<?> entry : configEntries) {
            entry.updateWidgetValue();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (Math.abs(scrollVelocity) > MIN_VELOCITY) {
            setScrollOffset((int) (scrollOffset + scrollVelocity));
            scrollVelocity *= FRICTION;
            if (Math.abs(scrollVelocity) < MIN_VELOCITY) {
                scrollVelocity = 0;
            }
        }
        renderBackground(guiGraphics);
        PortableUi.panel(guiGraphics, panel);
        PortableUi.header(guiGraphics, font, title, panel.left() + 4, panel.top() + 4, panel.width() - 8);
        PortableUi.inset(guiGraphics, viewport);
        UiInspector.region("config.panel", panel, null, false);
        UiInspector.region("config.viewport", viewport, viewport, true);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (viewport.width() > 0 && viewport.height() > 0) {
            guiGraphics.enableScissor(viewport.left(), viewport.top(), viewport.right(), viewport.bottom());
            for (int i = 0; i < configEntries.size(); i++) {
                ConfigEntry<?> entry = configEntries.get(i);
                int y = viewport.top() + i * ENTRY_HEIGHT - scrollOffset;
                if (y + ENTRY_HEIGHT > viewport.top() && y < viewport.bottom()) {
                    entry.render(guiGraphics, mouseX, mouseY, viewport.left(), y, viewport.width(), ENTRY_HEIGHT, font);
                    if (entry.descriptionLines.size() > (ENTRY_HEIGHT - 20) / font.lineHeight
                            && PortableLayout.contains(viewport, mouseX, mouseY)
                            && mouseX < viewport.right() - 120 && mouseY >= y + 20 && mouseY < y + ENTRY_HEIGHT) {
                        setTooltipForNextRenderPass(entry.description);
                    }
                }
            }
            guiGraphics.disableScissor();
        }
        if (maxScrollOffset > 0) {
            guiGraphics.fill(scrollbar.left(), scrollbar.top(), scrollbar.right(), scrollbar.bottom(), PortableUi.MUTED);
            guiGraphics.fill(scrollbarHandle.left(), scrollbarHandle.top(), scrollbarHandle.right(), scrollbarHandle.bottom(), 0xFFAAAAAA);
            UiInspector.region("config.scrollbar", scrollbar, null, true);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        long currentTime = System.currentTimeMillis();
        double scrollDelta = -delta * ENTRY_HEIGHT / 8.0;
        if (currentTime - lastScrollTime < 200) {
            scrollVelocity += scrollDelta * 0.5;
        } else {
            scrollVelocity = scrollDelta;
        }
        lastScrollTime = currentTime;
        setScrollOffset((int) (scrollOffset + scrollDelta));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && maxScrollOffset > 0 && scrollbar != null && PortableLayout.contains(scrollbar, mouseX, mouseY)) {
            if (scrollbarHandle != null && PortableLayout.contains(scrollbarHandle, mouseX, mouseY)) {
                isDraggingScrollbar = true;
                return true;
            }
            double clickPosition = (mouseY - scrollbar.top()) / Math.max(1, scrollbar.height());
            setScrollOffset((int) (clickPosition * maxScrollOffset));
            isDraggingScrollbar = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingScrollbar && maxScrollOffset > 0) {
            scrollVelocity = 0;
            double positionRatio = (mouseY - scrollbar.top() - scrollbarHandle.height() / 2.0)
                    / Math.max(1, scrollbar.height() - scrollbarHandle.height());
            setScrollOffset((int) (positionRatio * maxScrollOffset));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDraggingScrollbar = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private abstract static class ConfigEntry<T> {
        final Component title;
        final Component description;
        T currentValue;
        final Consumer<T> onValueChange;
        final Supplier<T> valueSupplier;
        List<FormattedCharSequence> descriptionLines = List.of();

        ConfigEntry(Component title, Component description, T initialValue, Consumer<T> onValueChange, Supplier<T> valueSupplier) {
            this.title = title;
            this.description = description;
            this.currentValue = initialValue;
            this.onValueChange = onValueChange;
            this.valueSupplier = valueSupplier;
        }

        abstract void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width);

        @Nullable
        abstract AbstractWidget control();

        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int y, int width, int height, Font font) {
            PortableUi.text(gui, font, title, x, y + 5, Math.max(0, width - 120), PortableUi.TEXT);
            int rows = Math.min(descriptionLines.size(), Math.max(0, (height - 20) / font.lineHeight));
            for (int i = 0; i < rows; i++) {
                gui.drawString(font, descriptionLines.get(i), x, y + 20 + i * font.lineHeight, PortableUi.MUTED, false);
            }
        }

        abstract void updateWidgetValue();

        void updateValue(T newValue) {
            this.currentValue = newValue;
            if (this.onValueChange != null) {
                this.onValueChange.accept(newValue);
            }
        }
    }
    private static class CategoryHeaderEntry extends ConfigEntry<Void> {
        CategoryHeaderEntry(Component title) {
            super(title, Component.empty(), null, null, null);
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
        }

        @Override
        @Nullable
        AbstractWidget control() {
            return null;
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int y, int width, int height, Font font) {
            gui.drawString(font, title, x, y + 5, 0xFFFFA0);
            gui.fill(x, y + 20, x + width, y + 22, 0xFFA0A0A0);
        }

        @Override
        void updateWidgetValue() {
        }
    }

    private static class BooleanConfigEntry extends ConfigEntry<Boolean> {
        private Button checkBox;

        BooleanConfigEntry(Component title, Component description, Boolean initialValue, Consumer<Boolean> onValueChange, Supplier<Boolean> valueSupplier) {
            super(title, description, initialValue, onValueChange, valueSupplier);
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            checkBox = Button.builder(
                    getButtonText(),
                    btn -> {
                        boolean newValue = !currentValue;
                        updateValue(newValue);
                        btn.setMessage(getButtonText());
                    }
            ).bounds(x + width - 100, y + 10, 100, 20).build();
            screen.addRenderableWidget(checkBox);
        }

        @Override
        AbstractWidget control() {
            return checkBox;
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int y, int width, int height, Font font) {
            gui.drawString(font, title, x, y + 5, 0xFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                gui.drawString(font, wrappedDesc.get(i), x, y + 20 + i * 10, 0xAAAAAA);
            }
        }

        private Component getButtonText() {
            return currentValue ?
                    Component.translatable("gui.yes") :
                    Component.translatable("gui.no");
        }


        @Override
        void updateWidgetValue() {
            if (checkBox != null) {
                currentValue = valueSupplier.get();
                checkBox.setMessage(getButtonText());
            }
        }
    }


    private static class IntConfigEntry extends ConfigEntry<Integer> {
        private RangedEditBox editBox;
        private final int min;
        private final int max;

        IntConfigEntry(Component title, Component description, Integer initialValue,
                       int min, int max, Consumer<Integer> onValueChange, Supplier<Integer> valueSupplier) {
            super(title, description, initialValue, onValueChange, valueSupplier);
            this.min = min;
            this.max = max;
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            editBox = new RangedEditBox(screen.font, x + width - 100, y + 10, 100, 20, Component.empty(), min, max, true);
            editBox.setMaxLength(10);
            editBox.setValue(String.valueOf(currentValue));
            editBox.setFilter(text -> {
                if (text.isEmpty()) return true;
                try {
                    Integer.parseInt(text);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            editBox.setResponder(text -> {
                if (!text.isEmpty()) {
                    try {
                        updateValue(Integer.parseInt(text));
                    } catch (NumberFormatException ignored) {
                    }
                }
            });
            screen.addRenderableWidget(editBox);
        }

        @Override
        AbstractWidget control() {
            return editBox;
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int yPos, int width, int height, Font font) {
            gui.drawString(font, title, x, yPos + 5, 0xFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                gui.drawString(font, wrappedDesc.get(i), x, yPos + 20 + i * 10, 0xAAAAAA);
            }
        }


        @Override
        void updateWidgetValue() {
            if (editBox != null) {
                currentValue = valueSupplier.get();
                editBox.setValue(String.valueOf(currentValue));
            }
        }
    }

    private static class DoubleConfigEntry extends ConfigEntry<Double> {
        private RangedEditBox editBox;
        private final double min;
        private final double max;

        DoubleConfigEntry(Component title, Component description, Double initialValue,
                          double min, double max, Consumer<Double> onValueChange, Supplier<Double> valueSupplier) {
            super(title, description, initialValue, onValueChange, valueSupplier);
            this.min = min;
            this.max = max;
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            editBox = new RangedEditBox(screen.font, x + width - 100, y + 10, 100, 20, Component.empty(), min, max, false);
            editBox.setMaxLength(10);
            editBox.setValue(String.valueOf(currentValue));
            editBox.setFilter(text -> {
                if (text.isEmpty()) return true;
                if (text.contains(".") && text.indexOf(".") != text.lastIndexOf(".")) return false;
                try {
                    Double.parseDouble(text);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            editBox.setResponder(text -> {
                if (!text.isEmpty()) {
                    try {
                        updateValue(Double.parseDouble(text));
                    } catch (NumberFormatException ignored) {
                    }
                }
            });
            screen.addRenderableWidget(editBox);
        }

        @Override
        AbstractWidget control() {
            return editBox;
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int yPos, int width, int height, Font font) {
            gui.drawString(font, title, x, yPos + 5, 0xFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                gui.drawString(font, wrappedDesc.get(i), x, yPos + 20 + i * 10, 0xAAAAAA);
            }
        }

        @Override
        void updateWidgetValue() {
            if (editBox != null) {
                currentValue = valueSupplier.get();
                editBox.setValue(String.valueOf(currentValue));
            }
        }
    }

    private static class LongConfigEntry extends ConfigEntry<Long> {
        private RangedEditBox editBox;
        private final long min;
        private final long max;

        LongConfigEntry(Component title, Component description, Long initialValue,
                        long min, long max, Consumer<Long> onValueChange, Supplier<Long> valueSupplier) {
            super(title, description, initialValue, onValueChange, valueSupplier);
            this.min = min;
            this.max = max;
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            editBox = new RangedEditBox(screen.font, x + width - 100, y + 10, 100, 20, Component.empty(), min, max, false);
            editBox.setMaxLength(15);
            editBox.setValue(String.valueOf(currentValue));
            editBox.setFilter(text -> {
                if (text.isEmpty()) return true;
                try {
                    Long.parseLong(text);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            editBox.setResponder(text -> {
                if (!text.isEmpty()) {
                    try {
                        updateValue(Long.parseLong(text));
                    } catch (NumberFormatException ignored) {
                    }
                }
            });
            screen.addRenderableWidget(editBox);
        }

        @Override
        AbstractWidget control() {
            return editBox;
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int yPos, int width, int height, Font font) {
            gui.drawString(font, title, x, yPos + 5, 0xFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                gui.drawString(font, wrappedDesc.get(i), x, yPos + 20 + i * 10, 0xAAAAAA);
            }
        }

        @Override
        void updateWidgetValue() {
            if (editBox != null) {
                currentValue = valueSupplier.get();
                editBox.setValue(String.valueOf(currentValue));
            }
        }
    }


    private static class RangedEditBox extends EditBox {
        private final double min;
        private final double max;
        private final boolean isInteger;
        private String lastValue = "";

        public RangedEditBox(Font font, int x, int y, int width, int height, Component component, double min, double max, boolean isInteger) {
            super(font, x, y, width, height, component);
            this.min = min;
            this.max = max;
            this.isInteger = isInteger;
        }

        @Override
        public void tick() {
            super.tick();

            String currentValue = getValue();
            if (!currentValue.equals(lastValue)) {
                lastValue = currentValue;
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            boolean result = super.keyPressed(keyCode, scanCode, modifiers);

            if (keyCode == 257 || keyCode == 335) {
                validateAndCorrectValue();
            }
            return result;
        }

        @Override
        public void setFocused(boolean focused) {
            if (!focused && this.isFocused()) {
                validateAndCorrectValue();
            }
            super.setFocused(focused);
        }

        private void validateAndCorrectValue() {
            String text = getValue();
            if (!text.isEmpty()) {
                try {
                    if (isInteger) {
                        long val = Long.parseLong(text);
                        if (val > max) {
                            setValue(String.valueOf((long) max));
                            moveCursorTo(0);
                        } else if (val < min) {
                            setValue(String.valueOf((long) min));
                            moveCursorTo(0);
                        }
                    } else {
                        double val = Double.parseDouble(text);
                        if (val > max) {
                            setValue(String.valueOf(max));
                            moveCursorTo(0);
                        } else if (val < min) {
                            setValue(String.valueOf(min));
                            moveCursorTo(0);
                        }
                    }
                } catch (NumberFormatException ignored) {

                }
            }
        }
    }
}
