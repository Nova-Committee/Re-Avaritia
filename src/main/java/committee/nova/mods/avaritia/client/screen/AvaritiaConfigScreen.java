package committee.nova.mods.avaritia.client.screen;

import committee.nova.mods.avaritia.init.config.ModConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AvaritiaConfigScreen extends Screen {
    private final Screen parent;
    private final List<ConfigEntry<?>> configEntries = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 40;
    private static final int MARGIN = 20;
    private static final int START_Y = 50;
    private Button saveButton;
    private Button backButton;

    public AvaritiaConfigScreen(Screen parent) {
        super(Component.translatable("title.avaritia.config.title"));
        this.parent = parent;
        initConfigEntries();
    }

    private void initConfigEntries() {
        addCategoryHeader("config.avaritia.category.tools");

        addBooleanEntry("is_keep_stone", ModConfig.isKeepStone,
                Component.translatable("config.avaritia.is_keep_stone.tooltip"),
                val -> ModConfig.isKeepStone.set(val));

        addBooleanEntry("is_merge_matter_cluster", ModConfig.isMergeMatterCluster,
                Component.translatable("config.avaritia.is_merge_matter_cluster.tooltip"),
                val -> ModConfig.isMergeMatterCluster.set(val));

        addIntEntry("sword_range_damage", ModConfig.swordRangeDamage, 100, 100000,
                Component.translatable("config.avaritia.sword_range_damage.tooltip"),
                val -> ModConfig.swordRangeDamage.set(val));

        addIntEntry("sword_attack_range", ModConfig.swordAttackRange, 8, 64,
                Component.translatable("config.avaritia.sword_attack_range.tooltip"),
                val -> ModConfig.swordAttackRange.set(val));

        addBooleanEntry("is_sword_attack_item_entity", ModConfig.isSwordAttackItemEntity,
                Component.translatable("config.avaritia.is_sword_attack_item_entity.tooltip"),
                val -> ModConfig.isSwordAttackItemEntity.set(val));

        addBooleanEntry("is_sword_attack_lightning", ModConfig.isSwordAttackLightning,
                Component.translatable("config.avaritia.is_sword_attack_lightning.tooltip"),
                val -> ModConfig.isSwordAttackLightning.set(val));

        addBooleanEntry("is_sword_attack_endless", ModConfig.isSwordAttackEndless,
                Component.translatable("config.avaritia.is_sword_attack_endless.tooltip"),
                val -> ModConfig.isSwordAttackEndless.set(val));

        addIntEntry("sub_arrow_damage", ModConfig.subArrowDamage, 100, 100000,
                Component.translatable("config.avaritia.sub_arrow_damage.tooltip"),
                val -> ModConfig.subArrowDamage.set(val));

        addIntEntry("axe_chain_count", ModConfig.axeChainCount, 16, 128,
                Component.translatable("config.avaritia.axe_chain_count.tooltip"),
                val -> ModConfig.axeChainCount.set(val));

        addDoubleEntry("food_time", ModConfig.foodTime, 0.1, 5.0,
                Component.translatable("config.avaritia.food_time.tooltip"),
                val -> ModConfig.foodTime.set(val));

        addIntEntry("pickaxe_break_range", ModConfig.pickAxeBreakRange, 2, 32,
                Component.translatable("config.avaritia.pickaxe_break_range.tooltip"),
                val -> ModConfig.pickAxeBreakRange.set(val));

        addIntEntry("shovel_break_range", ModConfig.shovelBreakRange, 2, 32,
                Component.translatable("config.avaritia.shovel_break_range.tooltip"),
                val -> ModConfig.shovelBreakRange.set(val));

        addIntEntry("neutron_collector_product_tick", ModConfig.neutronCollectorProductTick, 1200, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.neutron_collector_product_tick.tooltip"),
                val -> ModConfig.neutronCollectorProductTick.set(val));

        addIntEntry("singularity_time_required", ModConfig.singularityTimeRequired, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.singularity_time_required.tooltip"),
                val -> ModConfig.singularityTimeRequired.set(val));

        addDoubleEntry("growth_soul_farmland", ModConfig.growthSoulFarmland, 0.0, 1.0,
                Component.translatable("config.avaritia.growth_soul_farmland.tooltip"),
                val -> ModConfig.growthSoulFarmland.set(val));

        addIntEntry("blade_slash_damage", ModConfig.bladeSlashDamage, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.blade_slash_damage.tooltip"),
                val -> ModConfig.bladeSlashDamage.set(val));

        addIntEntry("blade_slash_radius", ModConfig.bladeSlashRadius, 5, 100,
                Component.translatable("config.avaritia.blade_slash_radius.tooltip"),
                val -> ModConfig.bladeSlashRadius.set(val));

        addBooleanEntry("internal_infinity_catalyst_craft", ModConfig.internalInfinityCatalystCraft,
                Component.translatable("config.avaritia.internal_infinity_catalyst_craft.tooltip"),
                val -> ModConfig.internalInfinityCatalystCraft.set(val));

        addCategoryHeader("config.avaritia.category.emc");

        addIntEntry("neutron_pile_emc", ModConfig.neutronPileEmc, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.neutron_pile_emc.tooltip"),
                val -> ModConfig.neutronPileEmc.set(val));

        addIntEntry("vanilla_totem_emc", ModConfig.vanillaTotemEmc, 0, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.vanilla_totem_emc.tooltip"),
                val -> ModConfig.vanillaTotemEmc.set(val));

        addCategoryHeader("config.avaritia.category.storage");

        addIntEntry("chest_max_item_size", ModConfig.chestMaxItemSize, 2048, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.chest_max_item_size.tooltip"),
                val -> ModConfig.chestMaxItemSize.set(val));

        addBooleanEntry("use_single_page_mode", ModConfig.useSinglePageMode,
                Component.translatable("config.avaritia.use_single_page_mode.tooltip"),
                val -> ModConfig.useSinglePageMode.set(val));

        addLongEntry("slot_stack_limit", ModConfig.slotStackLimit, 64L, 4294967295L,
                Component.translatable("config.avaritia.slot_stack_limit.tooltip"),
                val -> ModConfig.slotStackLimit.set(val));

        addIntEntry("max_page_limit", ModConfig.maxPageLimit, 2, 79536431,
                Component.translatable("config.avaritia.max_page_limit.tooltip"),
                val -> ModConfig.maxPageLimit.set(val));

        addIntEntry("reset_max_page", ModConfig.resetMaxPage, 1, 79536431,
                Component.translatable("config.avaritia.reset_max_page.tooltip"),
                val -> ModConfig.resetMaxPage.set(val));

        addIntEntry("inventory_rows", ModConfig.inventoryRows, 1, 6,
                Component.translatable("config.avaritia.inventory_rows.tooltip"),
                val -> ModConfig.inventoryRows.set(val));

        addCategoryHeader("config.avaritia.category.channel");

        addIntEntry("max_size_pre_channel", ModConfig.MAX_SIZE_PRE_CHANNEL, 2048, Integer.MAX_VALUE,
                Component.translatable("config.avaritia.max_size_pre_channel.tooltip"),
                val -> ModConfig.MAX_SIZE_PRE_CHANNEL.set(val));

        addIntEntry("max_channels_pre_player", ModConfig.MAX_CHANNELS_PRE_PLAYER, 4, 64,
                Component.translatable("config.avaritia.max_channels_pre_player.tooltip"),
                val -> ModConfig.MAX_CHANNELS_PRE_PLAYER.set(val));

        addIntEntry("max_public_channels", ModConfig.MAX_PUBLIC_CHANNELS, 32, 1024,
                Component.translatable("config.avaritia.max_public_channels.tooltip"),
                val -> ModConfig.MAX_PUBLIC_CHANNELS.set(val));

        addIntEntry("channel_fast_update_rate", ModConfig.CHANNEL_FAST_UPDATE_RATE, 1, 40,
                Component.translatable("config.avaritia.channel_fast_update_rate.tooltip"),
                val -> ModConfig.CHANNEL_FAST_UPDATE_RATE.set(val));

        addIntEntry("channel_full_update_rate", ModConfig.CHANNEL_FULL_UPDATE_RATE, 20, 1200,
                Component.translatable("config.avaritia.channel_full_update_rate.tooltip"),
                val -> ModConfig.CHANNEL_FULL_UPDATE_RATE.set(val));

        addCategoryHeader("config.avaritia.category.misc");

        addBooleanEntry("use_advance_tooltips", ModConfig.useAdvanceTooltips,
                Component.translatable("config.avaritia.use_advance_tooltips.tooltip"),
                val -> ModConfig.useAdvanceTooltips.set(val));

        addDoubleEntry("endless_item_entity_speed", ModConfig.endlessItemEntitySpeed, 1.0, 50.0,
                Component.translatable("config.avaritia.endless_item_entity_speed.tooltip"),
                val -> ModConfig.endlessItemEntitySpeed.set(val));

        addDoubleEntry("endless_item_entity_range", ModConfig.endlessItemEntityRange, 1.0, 10000.0,
                Component.translatable("config.avaritia.endless_item_entity_range.tooltip"),
                val -> ModConfig.endlessItemEntityRange.set(val));

        addDoubleEntry("infinity_elytra_flying_speed", ModConfig.infinityElytraFlyingSpeed, 1.0, 10.0,
                Component.translatable("config.avaritia.infinity_elytra_flying_speed.tooltip"),
                val -> ModConfig.infinityElytraFlyingSpeed.set(val));
    }

    private void addBooleanEntry(String titleKey, ForgeConfigSpec.BooleanValue configValue,
                                 Component description, Consumer<Boolean> onValueChange) {
        configEntries.add(new BooleanConfigEntry(
                Component.translatable("config.avaritia." + titleKey),
                description,
                configValue.get(),
                onValueChange
        ));
    }

    private void addIntEntry(String titleKey, ForgeConfigSpec.IntValue configValue, int min, int max,
                             Component description, Consumer<Integer> onValueChange) {
        configEntries.add(new IntConfigEntry(
                Component.translatable("config.avaritia." + titleKey),
                description,
                configValue.get(),
                min,
                max,
                onValueChange
        ));
    }

    private void addDoubleEntry(String titleKey, ForgeConfigSpec.DoubleValue configValue, double min, double max,
                                Component description, Consumer<Double> onValueChange) {
        configEntries.add(new DoubleConfigEntry(
                Component.translatable("config.avaritia." + titleKey),
                description,
                configValue.get(),
                min,
                max,
                onValueChange
        ));
    }

    private void addLongEntry(String titleKey, ForgeConfigSpec.LongValue configValue, long min, long max,
                              Component description, Consumer<Long> onValueChange) {
        configEntries.add(new LongConfigEntry(
                Component.translatable("config.avaritia." + titleKey),
                description,
                configValue.get(),
                min,
                max,
                onValueChange
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
        clearWidgets();

        saveButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.save"),
                btn -> {
                    ModConfig.COMMON.save();
                }
        ).bounds(width / 2 - 102, height - 30, 100, 20).build());

        backButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.back"),
                btn -> minecraft.setScreen(parent)
        ).bounds(width / 2 + 2, height - 30, 100, 20).build());

        for (int i = 0; i < configEntries.size(); i++) {
            ConfigEntry<?> entry = configEntries.get(i);
            int x = MARGIN;
            int y = START_Y + i * ENTRY_HEIGHT - scrollOffset;
            if (y + ENTRY_HEIGHT > START_Y && y < height - 40) {
                entry.initWidgets(this, x, y, width - 2 * MARGIN);
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);

        for (int i = 0; i < configEntries.size(); i++) {
            ConfigEntry<?> entry = configEntries.get(i);
            int y = START_Y + i * ENTRY_HEIGHT - scrollOffset;
            if (y + ENTRY_HEIGHT > START_Y && y < height - 40) {
                entry.render(guiGraphics, mouseX, mouseY, MARGIN, y, width - 2 * MARGIN, ENTRY_HEIGHT, font);
            }
        }

        if (configEntries.size() * ENTRY_HEIGHT > height - START_Y - 40) {
            int scrollBarHeight = (height - START_Y - 40) * (height - START_Y - 40) / (configEntries.size() * ENTRY_HEIGHT);
            scrollBarHeight = Math.max(20, scrollBarHeight);
            int scrollBarY = START_Y + (scrollOffset * (height - START_Y - 40 - scrollBarHeight)) / (configEntries.size() * ENTRY_HEIGHT - (height - START_Y - 40));
            guiGraphics.fill(width - 8, scrollBarY, width - 4, scrollBarY + scrollBarHeight, 0x88888888);
        }

        // 重新渲染按钮以确保它们在最上层
        saveButton.render(guiGraphics, mouseX, mouseY, partialTick);
        backButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxOffset = Math.max(0, configEntries.size() * ENTRY_HEIGHT - (height - START_Y - 40));
        scrollOffset = (int) Math.max(0, Math.min(maxOffset, scrollOffset - delta * 20));
        init();
        return true;
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

        ConfigEntry(Component title, Component description, T initialValue, Consumer<T> onValueChange) {
            this.title = title;
            this.description = description;
            this.currentValue = initialValue;
            this.onValueChange = onValueChange;
        }

        abstract void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width);

        abstract void render(GuiGraphics gui, int mouseX, int mouseY, int x, int y, int width, int height, Font font);

        void updateValue(T newValue) {
            this.currentValue = newValue;
            if (this.onValueChange != null) {
                this.onValueChange.accept(newValue);
            }
        }
    }

    private static class CategoryHeaderEntry extends ConfigEntry<Void> {
        CategoryHeaderEntry(Component title) {
            super(title, Component.empty(), null, null);
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int y, int width, int height, Font font) {
            gui.drawString(font, title, x, y + 5, 0xFFFFA0);
            gui.fill(x, y + 20, x + width, y + 22, 0xFFA0A0A0);
        }
    }

    private static class BooleanConfigEntry extends ConfigEntry<Boolean> {
        private Button checkBox;

        BooleanConfigEntry(Component title, Component description, Boolean initialValue, Consumer<Boolean> onValueChange) {
            super(title, description, initialValue, onValueChange);
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
    }

    private static class IntConfigEntry extends ConfigEntry<Integer> {
        private EditBox editBox;
        private final int min;
        private final int max;

        IntConfigEntry(Component title, Component description, Integer initialValue,
                       int min, int max, Consumer<Integer> onValueChange) {
            super(title, description, initialValue, onValueChange);
            this.min = min;
            this.max = max;
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            editBox = new EditBox(screen.font, x + width - 100, y + 10, 100, 20, Component.empty());
            editBox.setMaxLength(10);
            editBox.setValue(String.valueOf(currentValue));
            editBox.setFilter(text -> {
                if (text.isEmpty()) return true;
                try {
                    int val = Integer.parseInt(text);
                    return val >= min && val <= max;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            editBox.setResponder(text -> {
                if (!text.isEmpty()) {
                    try {
                        updateValue(Integer.parseInt(text));
                    } catch (NumberFormatException ignored) {}
                }
            });
            screen.addRenderableWidget(editBox);
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int yPos, int width, int height, Font font) {
            gui.drawString(font, title, x, yPos + 5, 0xFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                gui.drawString(font, wrappedDesc.get(i), x, yPos + 20 + i * 10, 0xAAAAAA);
            }
        }
    }

    private static class DoubleConfigEntry extends ConfigEntry<Double> {
        private EditBox editBox;
        private final double min;
        private final double max;

        DoubleConfigEntry(Component title, Component description, Double initialValue,
                          double min, double max, Consumer<Double> onValueChange) {
            super(title, description, initialValue, onValueChange);
            this.min = min;
            this.max = max;
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            editBox = new EditBox(screen.font, x + width - 100, y + 10, 100, 20, Component.empty());
            editBox.setMaxLength(10);
            editBox.setValue(String.valueOf(currentValue));
            editBox.setFilter(text -> {
                if (text.isEmpty()) return true;
                if (text.contains(".") && text.indexOf(".") != text.lastIndexOf(".")) return false;
                try {
                    double val = Double.parseDouble(text);
                    return val >= min && val <= max;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            editBox.setResponder(text -> {
                if (!text.isEmpty()) {
                    try {
                        updateValue(Double.parseDouble(text));
                    } catch (NumberFormatException ignored) {}
                }
            });
            screen.addRenderableWidget(editBox);
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int yPos, int width, int height, Font font) {
            gui.drawString(font, title, x, yPos + 5, 0xFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                gui.drawString(font, wrappedDesc.get(i), x, yPos + 20 + i * 10, 0xAAAAAA);
            }
        }
    }

    private static class LongConfigEntry extends ConfigEntry<Long> {
        private EditBox editBox;
        private final long min;
        private final long max;

        LongConfigEntry(Component title, Component description, Long initialValue,
                        long min, long max, Consumer<Long> onValueChange) {
            super(title, description, initialValue, onValueChange);
            this.min = min;
            this.max = max;
        }

        @Override
        void initWidgets(AvaritiaConfigScreen screen, int x, int y, int width) {
            editBox = new EditBox(screen.font, x + width - 100, y + 10, 100, 20, Component.empty());
            editBox.setMaxLength(15);
            editBox.setValue(String.valueOf(currentValue));
            editBox.setFilter(text -> {
                if (text.isEmpty()) return true;
                try {
                    long val = Long.parseLong(text);
                    return val >= min && val <= max;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            editBox.setResponder(text -> {
                if (!text.isEmpty()) {
                    try {
                        updateValue(Long.parseLong(text));
                    } catch (NumberFormatException ignored) {}
                }
            });
            screen.addRenderableWidget(editBox);
        }

        @Override
        void render(GuiGraphics gui, int mouseX, int mouseY, int x, int yPos, int width, int height, Font font) {
            gui.drawString(font, title, x, yPos + 5, 0xFFFFFF);
            List<FormattedCharSequence> wrappedDesc = font.split(description, width - 120);
            for (int i = 0; i < wrappedDesc.size(); i++) {
                gui.drawString(font, wrappedDesc.get(i), x, yPos + 20 + i * 10, 0xAAAAAA);
            }
        }
    }
}
