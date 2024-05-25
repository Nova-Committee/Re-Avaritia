package committee.nova.mods.avaritia.init.compact.rei.category;

import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.init.compact.rei.ServerREIPlugin;
import committee.nova.mods.avaritia.init.compact.rei.display.ExtremeTableDisplay;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ExtremeTableCategory implements DisplayCategory<ExtremeTableDisplay<?>>{
    private static final ResourceLocation TEXTURE = new ResourceLocation(Static.MOD_ID, "textures/gui/jei/extreme_jei.png");

    @Override
    public CategoryIdentifier<? extends ExtremeTableDisplay<?>> getCategoryIdentifier() {
        return ServerREIPlugin.EXTREME_TABLE;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(ModBlocks.extreme_crafting_table.get());
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.avaritia.extreme_crafting_table");
    }

    @Override
    public List<Widget> setupDisplay(ExtremeTableDisplay<?> display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 81, bounds.getCenterY() - 35);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        List<InputIngredient<EntryStack<?>>> input = display.getInputIngredients(9, 9);
        List<Slot> slots = Lists.newArrayList();
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++)
                slots.add(Widgets.createSlot(new Point(startPoint.x + 1 + x * 18, startPoint.y - 64 + y * 18)).markInput());
        for (InputIngredient<EntryStack<?>> ingredient : input) {
            slots.get(ingredient.getIndex()).entries(ingredient.get());
        }
        widgets.add(Widgets.createDrawableWidget((context, mouseX, mouseY, delta) -> {
                    context.blit(TEXTURE, startPoint.x + 69, startPoint.y + 108, 167, 73, 78, 30);
                }));
        widgets.addAll(slots);
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 73, startPoint.y + 109)).entries(display.getOutputEntries().get(0)).disableBackground().markOutput());
        if (display.isShapeless()) {
            widgets.add(Widgets.createShapelessIcon(new Point(startPoint.x + 8, startPoint.y + 127)));
        }
        return widgets;
    }

    @Override
    public int getMaximumDisplaysPerPage() {
        return 1;
    }

    @Override
    public int getDisplayHeight() {
        return 210;
    }

    @Override
    public int getDisplayWidth(ExtremeTableDisplay<?> display) {
        return 172;
    }
}
